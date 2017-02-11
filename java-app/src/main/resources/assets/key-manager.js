var _spki = null;

var VueKeyImporter = function(encryptionPassword, callback, userProfile) {

    var vueStore = stallionClubhouseApp.store;
    var self = this;
    self.encryptionPassword = encryptionPassword;
    self.callback = callback;
    self.userProfile = userProfile || vueStore.state.userProfile;
    
    
    if (self.userProfile === null || self.userProfile === undefined) {
        throw new Error('stallionClubhouseApp.state.userProfile is null and userProfile not passed in.');
    }

    self.importPublicAndPrivate = function() {
        step1ImportPublic();
    };
    
    function step1ImportPublic() {
        var spkiBytes = hexToArray(self.userProfile.publicKeyHex);
        crypto.subtle.importKey(
            'spki',
            spkiBytes.buffer,
            {
                name: "RSA-OAEP",
                modulusLength: 2048,
                publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
                hash: {name: "SHA-256"}
            },
            true,
            ["encrypt"]
        ).then(function(result) {
            console.log('v1. got publick key ', result);
            vueStore.commit('publicKey', result);
            step2DecryptPrivate();
        }).catch(function(err) {
            console.error(err);
        });  
    };

    function step2DecryptPrivate() {
        new PrivateKeyFetcher().fetch(
            self.userProfile.encryptedPrivateKeyHex,
            self.userProfile.encryptedPrivateKeyInitializationVectorHex,
            self.encryptionPassword,
            function(result) {
                console.log('decrypted private key!!! ', result);
                vueStore.commit('privateKey', result);
                step3TestEncrypt();
            }
        );
    }

    function step3TestEncrypt() {
        var message = 'a-test-message-' + new Date().getTime();
        var vector = crypto.getRandomValues(new Uint8Array(16));
        crypto.subtle.encrypt(
            {
                name: "RSA-OAEP",
                iv: vector
            },
            vueStore.state.publicKey,
            stringToArrayBuffer(message)
        ).then(
            function(result){
                var encryptedBytes = new Uint8Array(result);
                console.log('encrypted test phrase');
                step4TestDecrypt(message, vector, encryptedBytes);
            }, 
            function(e){
                console.error(e);
            }
        );              
        
    }

    function step4TestDecrypt(message, vector, encryptedBytes) {
        crypto.subtle.decrypt(
            {
                name: "RSA-OAEP",
                iv: vector
            },
            vueStore.state.privateKey,
            encryptedBytes.buffer
        ).then(
            function(result){
                var decryptedMessage = convertArrayBufferViewtoString(result);
                if (message === decryptedMessage) {
                    console.log('encrypt/decrypt test passed!');
                    self.callback();
                } else {
                    throw new Error('The message was ' + message + ' but after decryption was ' + decryptedMessage);
                }
            },
            function(e){
                console.error(e);
                
            }
        );     
    }
    

};

(function() {

    var km = {};

    window.KeyManager = km;

    km.generateIfNotExists = function(user, password, callback) {
        if (localStorage['st_clubhouse_public_key_' + user]) {
            console.log('public key exists for ' + user);
            callback();
        }
        new KeyGenerator().generate(user, password, function() {
            console.log('key generated');
            callback();
        });
    };

    

    km.getPublicKey = function(user, callback) {
        var data = JSON.parse(localStorage['st_clubhouse_public_key_' + user]);
        var spkiBytes = hexToArray(data.spki);
        crypto.subtle.importKey(
            'spki',
            //window._spki,
            spkiBytes.buffer,
            //            "RSA-OAEP",
            {
                name: "RSA-OAEP",
                modulusLength: 2048,
                publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
                hash: {name: "SHA-256"}
            },
            true,
            ["encrypt"]
        ).then(function(result) {
            console.log('got publick key ', result);
            callback(result);
        }).catch(function(err) {
            console.error(err);
        });  
    };

    km.getMyPrivateKey = function(user, password, callback) {
        console.log('get private key ' + user);
        new PrivateKeyFetcher().fetchFromLocalStorage(user, password, callback);
    };


})();

var PrivateKeyFetcher = function() {
    var kf = this;
    var self = this;

    self.fetchFromLocalStorage = function(user, password, callback) {
        var privateKeyEncryptedHex = localStorage['st_clubhouse_private_key_' + user];
        console.log('vector json ' + localStorage['st_clubhouse_private_key_vector_' + user]);
        var privateKeyEncryptedVector = localStorage['st_clubhouse_private_key_vector_' + user];
        self.fetch(privateKeyEncryptedHex, privateKeyEncryptedVector, password, callback);
    };

    self.fetch = function(privateKeyHex, privateKeyVectorHex, password, callback) {
        console.log('PrivateKeyFetcher.fetch ');
        self.password = password;
        self.callback = callback;
        self.privateKeyEncryptedHex = privateKeyHex;
        self.privateKeyEncryptedVector = hexToArray(privateKeyVectorHex);
        self.passwordDerivedKey = null;
        self.privateKeyPkcs8 = null;
        step1LoadEncryptedPrivateKey();
    };

    function step1LoadEncryptedPrivateKey() {
        console.log('f1. load private key');
        step2DerivePrivateKeyEncryptionKey();
    }

    function step2DerivePrivateKeyEncryptionKey() {
        console.log('f2. derive encryption key for ' + self.password);
        crypto.subtle.digest(
            {
                name: "SHA-256"
            },
            convertStringToArrayBufferView(self.password)
        ).then(function(result){
            window.crypto.subtle.importKey(
                "raw",
                result,
                {
                    name: "AES-CBC"
                },
                false,
                ["encrypt", "decrypt"]
            ).then(function(key){
                console.log('f2. derived key ', key);
                self.passwordDerivedKey = key;
                step3DecryptPrivateKey();
            }, function(e){
                console.error(e);
            });
        });
        
    }

    function step3DecryptPrivateKey() {
        console.log('f3. decrypt private key hex ' + self.privateKeyEncryptedHex);
        var keyBytes = hexToArray(self.privateKeyEncryptedHex);
        console.log('f3. decrypt private key bytes ', keyBytes);
        console.log('f3. decrypt private key vector ', self.privateKeyEncryptedVector);
        console.log('f3. password derived key ', self.passwordDerivedKey);
        crypto.subtle.decrypt(
            {
                name: "AES-CBC",
                iv: self.privateKeyEncryptedVector
            },
            self.passwordDerivedKey,
            keyBytes.buffer
        ).then(
            function(privateKeyPkcs8) {
                console.log('f3. decrypte ', new Uint8Array(privateKeyPkcs8));
                self.privateKeyPkcs8 = privateKeyPkcs8;
                step4ImportDecryptedPrivateKey();
            }, 
            function(e){
                console.error(e);
            }
        );        
    }

    function step4ImportDecryptedPrivateKey() {
        console.log('f4. try importing pfivate key');
        window.crypto.subtle.importKey(
            'pkcs8',
            self.privateKeyPkcs8,
            {
                name: "RSA-OAEP",
                modulusLength: 2048,
                publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
                hash: {name: "SHA-256"}
            },
            false,
            ["decrypt"]
        ).then(function(privateKey) {
            console.log('f4. private key imported ', privateKey);
            self.callback(privateKey);
        });
    }
};


var KeyGenerator = function() {
    var kg = this;
    var self = this;

    self.generate = function(user, password, callback) {
        self.user = user;
        self.password = password;
        self.publicKey = null;
        self.passwordDerivedKey = null;
        self.privateKey = null;
        self.callback = callback;
        self.privateKeySpki = null;
        self.privateKeyEncryptionVector = null;
        step1GenerateMyPublicPrivate(self.user, password);
    };

    step1GenerateMyPublicPrivate = function(user, password) {
        console.log('g1. generate public/private keys for ' + user);
        var algorithmName = "RSA-OAEP";
        var usages = ["encrypt", "decrypt"];
        window.crypto.subtle.generateKey(
            {
                name: "RSA-OAEP",
                modulusLength: 2048,
                publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
                hash: {name: "SHA-256"}
            },
            true,  // Cannot extract new key
            usages
        ).then(function(keyPair) {
            self.publicKey = keyPair.publicKey;
            self.privateKey = keyPair.privateKey;
            step2ExportPublicKey();
        }).catch(function(err) {
            alert("Could not create and save new key pair: " + err.message);
        });          
    };

    function step2ExportPublicKey() {
        console.log('g2. export public key');
        window.crypto.subtle.exportKey('spki', self.publicKey).then(function(spki) {
            window._spki = spki;
            var savedObject = {
                name:       name,
                spki:       arrayBufferToHexString(spki)
            };
            localStorage['st_clubhouse_public_key_' + self.user] = JSON.stringify(savedObject);
            self.publicKeyHex = arrayBufferToHexString(spki);
            step3ExportPrivateKey();
        });
    };

    function step3ExportPrivateKey() {
        window.crypto.subtle.exportKey('pkcs8', self.privateKey).then(function(pkcs8) {
            console.log('g3. privatekey pkcs8 ', new Uint8Array(pkcs8));
            var hexPkcs8 = arrayBufferToHexString(pkcs8);
            console.log('g3. privateKey after pkcs8 ', hexToArray(arrayBufferToHexString(pkcs8)));
            console.log('g3. privateKey hex pkcs8 ', hexPkcs8);
            self.privateKeyPkcs8 = pkcs8;
            step4DerivePrivateKeyEncryptionKey();
        });
    };

    function step4DerivePrivateKeyEncryptionKey() {
        console.log('g4. derive key to encrypt private key');
        crypto.subtle.digest(
            {
                name: "SHA-256"
            },
            convertStringToArrayBufferView(self.password)
        ).then(function(result){
            window.crypto.subtle.importKey(
                "raw",
                result,
                {
                    name: "AES-CBC"
                },
                false,
                ["encrypt", "decrypt"]
            ).then(function(key){
                console.log('g4. derived ', key);
                self.passwordDerivedKey = key;
                step5EncryptPrivateKey();
            }, function(e){
                console.log(e);
            });
        });

    };

    function step5EncryptPrivateKey() {
        var vector = crypto.getRandomValues(new Uint8Array(16));
        self.privateKeyEncryptionVector = vector;
        self.privateKeyEncryptionVectorHex = arrayToHex(vector);
        var encryptPromise = crypto.subtle.encrypt(
            {
                name: "AES-CBC",
                iv: vector
            },
            self.passwordDerivedKey,
            self.privateKeyPkcs8
        );
        encryptPromise.then(
            function(result) {
                var encryptedBytes = new Uint8Array(result);
                console.log('g5. encrypt vector ', vector);
                console.log('g5. encryptedPrivateKeyBytes ', encryptedBytes);                
                self.privateKeyEncryptedHex = arrayBufferToHexString(result);
                //enc.encryptedMessageBytes = result;
                //info.encryptedMessageHex = arrayBufferToHexString(encrypted_data);
                //enc.encryptedMessageHex = result;
                //console.log('e2. encryptedMessageHex ', enc.encryptedMessageHex);
                console.log('g5. encryptedPrivateKeyHex ', self.privateKeyEncryptedHex);
                localStorage['st_clubhouse_private_key_' + self.user] = self.privateKeyEncryptedHex;
                localStorage['st_clubhouse_private_key_vector_' + self.user] = arrayToHex(vector);
                self.callback(self);
            }, 
            function(e){
                console.error(e);
            }
        );

    }

    

};
