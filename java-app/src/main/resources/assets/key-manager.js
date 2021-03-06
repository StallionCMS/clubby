
isDebug = window.location.search.indexOf('cbDebug=1') > -1;
isDebug = true;

if (isDebug) var debug = console.log.bind(window.console)
else var debug = function(){}

function clubhouseImportPrivateKey(passphrase, privateKeyJwkEncryptedHex, privateKeyVectorHex) {
    var userProfile = {
        privateKeyJwkEncryptedHex: privateKeyJwkEncryptedHex,
        privateKeyVectorHex: privateKeyVectorHex
    };
    return new Promise(function(resolve, reject) {
        new KeyImporter(passphrase, resolve, reject, userProfile).importPrivate();
    });        
}

function clubhouseImportPublicAndPrivateKey(encpassphrase, userProfile) {
    return new Promise(function(resolve, reject) {
        new KeyImporter(encpassphrase, resolve, reject, userProfile).importPublicAndPrivate();
    });
}

function clubhouseGeneratePrivateAndPublicKey(encpassphrase) {
    return new Promise(function(resolve, reject) {
        new KeyGenerator().generate(encpassphrase, resolve, reject);
    });
}




var KeyImporter = function(encpassphrase, resolve, reject, userProfile) {

    var vueStore = stallionClubhouseApp.store;
    var self = this;
    self.encpassphrase = encpassphrase;
    self.resolve = resolve;
    self.userProfile = userProfile || vueStore.state.userProfile;
    
    
    if (self.userProfile === null || self.userProfile === undefined) {
        throw new Error('stallionClubhouseApp.state.userProfile is null and userProfile not passed in.');
    }

    self.importPublicAndPrivate = function() {
        step1ImportPublic();
    };

    self.importPrivate = function() {
        FetchPrivateKey(
            self.userProfile.privateKeyJwkEncryptedHex,
            self.userProfile.privateKeyVectorHex,
            self.encpassphrase
        ).then(
            function(result) {
                console.debug('decrypted private key!!! ', result);
                resolve(result);
            },
            function(e) {
                console.error(e);
                reject(e)
            }
        );
    }
    
    function step1ImportPublic() {

        var jwk = JSON.parse(self.userProfile.publicKeyJwkJson);
        crypto.subtle.importKey(
            'jwk',
            jwk,
            {
                name: "RSA-OAEP",
                modulusLength: 2048,
                publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
                hash: {name: "SHA-1"}
            },
            true,
            ["encrypt"]
        ).then(function(result) {
            console.debug('v1. got publick key ', result);
            vueStore.commit('publicKey', result);
            step2DecryptPrivate();
        }).catch(function(err) {
            console.error(err);
            reject(err);
        });  
    };

    function step2DecryptPrivate() {

        FetchPrivateKey(
            self.userProfile.privateKeyJwkEncryptedHex,
            self.userProfile.privateKeyVectorHex,
            self.encpassphrase
        ).then(
            function(result) {
                console.debug('decrypted private key!!! ', result);
                vueStore.commit('privateKey', result);
                step3TestEncrypt();
            },
            function(e) {
                console.error(e);
                reject(e)
            }
        );
    }

    function step3TestEncrypt() {
        var message = 'a-test-message-' + new Date().getTime();
        var vector = crypto.getRandomValues(new Uint8Array(16));
        crypto.subtle.encrypt(
            {
                name: "RSA-OAEP",
                iv: vector,
                hash: {name: "SHA-1"}
            },
            vueStore.state.publicKey,
            stringToArrayBuffer(message)
        ).then(
            function(result){
                var encryptedBytes = new Uint8Array(result);
                console.debug('encrypted test phrase');
                step4TestDecrypt(message, vector, encryptedBytes);
            }, 
            function(e){
                console.error(e);
                reject(err);
            }
        );              
        
    }

    function step4TestDecrypt(message, vector, encryptedBytes) {
        crypto.subtle.decrypt(
            {
                name: "RSA-OAEP",
                iv: vector,
                hash: {name: "SHA-1"}
            },
            vueStore.state.privateKey,
            encryptedBytes.buffer
        ).then(
            function(result){
                var decryptedMessage = convertArrayBufferViewtoString(result);
                if (message === decryptedMessage) {
                    console.debug('encrypt/decrypt test passed!');
                    self.resolve({privateKey: vueStore.state.privateKey, publicKey: vueStore.state.publicKey});
                } else {
                    throw new Error('The message was ' + message + ' but after decryption was ' + decryptedMessage);
                }
            },
            function(e){
                console.error(e);
                reject(err);
            }
        );     
    }
    

};

/*
(function() {

    var km = {};

    window.KeyManager = km;

    km.generateIfNotExists = function(user, password, callback) {
        if (localStorage['st_clubhouse_public_key_' + user]) {
            console.debug('public key exists for ' + user);
            callback();
        }
        new KeyGenerator().generate(user, password, function() {
            console.debug('key generated');
            callback();
        });
    };

    

    km.getPublicKey = function(user, callback) {
        var data = JSON.parse(localStorage['st_clubhouse_public_key_' + user]);
        var spkiBytes = hexToArray(data.spki);
        crypto.subtle.importKey(
            'jwk',
            //window._spki,
            spkiBytes.buffer,
            //            "RSA-OAEP",
            {
                name: "RSA-OAEP",
                modulusLength: 2048,
                publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
                hash: {name: "SHA-1"}
            },
            true,
            ["encrypt"]
        ).then(function(result) {
            console.debug('got publick key ', result);
            callback(result);
        }).catch(function(err) {
            console.error(err);
        });  
    };

    km.getMyPrivateKey = function(user, password, callback) {
        console.debug('get private key ' + user);
        new PrivateKeyFetcher().fetchFromLocalStorage(user, password, callback);
    };


})();
*/

var FetchPrivateKey = function(privateKeyJwkEncryptedHex, privateKeyVectorHex, password) {
    return new Promise(function(resolve, reject) {
        new PrivateKeyFetcher().fetch(privateKeyJwkEncryptedHex, privateKeyVectorHex, password, resolve, reject);
    });
};

var PrivateKeyFetcher = function() {
    var kf = this;
    var self = this;

    self.fetchFromLocalStorage = function(user, password, resolve, reject) {
        var privateKeyEncryptedHex = localStorage['st_clubhouse_private_key_' + user];
        console.debug('vector json ' + localStorage['st_clubhouse_private_key_vector_' + user]);
        var privateKeyEncryptedVector = localStorage['st_clubhouse_private_key_vector_' + user];
        self.fetch(privateKeyEncryptedHex, privateKeyEncryptedVector, password, resolve, reject);
    };

    self.fetch = function(privateKeyJwkEncryptedHex, privateKeyVectorHex, password, resolve, reject) {
        console.debug('PrivateKeyFetcher.fetch ');
        self.password = password;
        self.resolve = resolve;
        self.reject = reject;
        self.privateKeyJwkEncryptedHex = privateKeyJwkEncryptedHex;
        self.privateKeyVector = hexToArray(privateKeyVectorHex);
        self.passwordDerivedKey = null;
        self.privateKeyPkcs8 = null;
        step1LoadEncryptedPrivateKey();
    };

    function step1LoadEncryptedPrivateKey() {
        console.debug('f1. load private key');
        step2DerivePrivateKeyEncryptionKey();
    }

    function step2DerivePrivateKeyEncryptionKey() {
        console.debug('f2. derive encryption key for ' + self.password);
        crypto.subtle.digest(
            {
                name: "SHA-256"
            },
            convertStringToArrayBufferView(self.password)
        ).then(function(result){
            console.debug('passphrase converted to sha-256, now convert to key: ' + result + " of size " + result.byteLength);
            window.crypto.subtle.importKey(
                "raw",
                result,
                {
                    name: "AES-GCM"
                },
                false,
                ["encrypt", "decrypt"]
            ).then(function(key){
                console.debug('f2. derived key ', key);
                self.passwordDerivedKey = key;
                step3DecryptPrivateKey();
            }, function(e){
                console.error(e);
                self.reject(e);
            });
        });
        
    }

    function step3DecryptPrivateKey() {
        console.debug('f3. decrypt private key hex ' + self.privateKeyEncryptedHex);

        var keyBytes = hexToArray(self.privateKeyJwkEncryptedHex);
        console.debug('f3. decrypt private key bytes ', keyBytes);
        console.debug('f3. decrypt private key vector ', self.privateKeyVector);
        console.debug('f3. password derived key ', self.passwordDerivedKey);
        crypto.subtle.decrypt(
            {
                name: "AES-GCM",
                iv: self.privateKeyVector
            },
            self.passwordDerivedKey,
            keyBytes.buffer
        ).then(
            function(privateKeyJwkBytes) {
                console.debug('f3. decrypte ', privateKeyJwkBytes);
                var privateJwk = JSON.parse(convertArrayBufferViewtoString(privateKeyJwkBytes));
                //self.privateKeyPkcs8 = privateKeyPkcs8;
                step4ImportDecryptedPrivateKey(privateJwk);
            }, 
            function(e){
                console.error(e);
                self.reject(e);
            }
        );        
    }

    function step4ImportDecryptedPrivateKey(privateKeyJwk) {
        console.debug('f4. try importing pfivate key');
        window.crypto.subtle.importKey(
            'jwk',
            privateKeyJwk,
            {
                name: "RSA-OAEP",
                modulusLength: 2048,
                publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
                hash: {name: "SHA-1"}
            },
            false,
            ["decrypt"]
        ).then(function(privateKey) {
            console.debug('f4. private key imported ', privateKey);
            self.resolve(privateKey);
        }, function(e) {
            console.error(e);
            self.reject(e);
        });
    }
};


var KeyGenerator = function() {
    var kg = this;
    var self = this;

    self.generate = function(encpassphrase, callback, reject) {
        self.encpassphrase = encpassphrase;
        self.publicKey = null;
        self.encpassphraseDerivedKey = null;
        self.privateKey = null;
        self.callback = callback;
        self.reject = reject;



        step1GenerateMyPublicPrivate(encpassphrase);
    };

    var step1GenerateMyPublicPrivate = function() {
        console.debug('g1. generate public/private keys');
        var algorithmName = "RSA-OAEP";
        var usages = ["encrypt", "decrypt"];
        window.crypto.subtle.generateKey(
            {
                name: "RSA-OAEP",
                modulusLength: 2048,
                publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
                hash: {name: "SHA-1"}
            },
            true,  // Cannot extract new key
            usages
        ).then(function(keyPair) {
            self.publicKey = keyPair.publicKey;
            self.privateKey = keyPair.privateKey;
            step2ExportPublicKey();
        }).catch(function(err) {
            console.debug(err);
            self.reject(err);
        });          
    };

    function step2ExportPublicKey() {
        console.debug('g2. export public key');
        window.crypto.subtle.exportKey('jwk', self.publicKey).then(function(jwk) {
            //window._spki = jwk;
            //var savedObject = {
            //    name:       name,
            //    spki:       arrayBufferToHexString(jwk)
            //};
            //localStorage['st_clubhouse_public_key_' + self.user] = JSON.stringify(savedObject);
            self.publicKeyJwkJson = JSON.stringify(jwk);
            step3ExportPrivateKey();
        }).catch(function(e) {
            console.debug(e);
            self.reject(e);
        });
    };

    function step3ExportPrivateKey() {
        window.crypto.subtle.exportKey('jwk', self.privateKey).then(function(jwk) {
            console.debug('g3. privatekey jwk ', jwk);
            //var hexPkcs8 = arrayBufferToHexString(pkcs8);
            //console.debug('g3. privateKey after pkcs8 ', hexToArray(arrayBufferToHexString(pkcs8)));
            //console.debug('g3. privateKey hex pkcs8 ', hexPkcs8);
            self.privateKeyJwk = jwk;
            step4DerivePrivateKeyEncryptionKey();
        }).catch(function(e) {
            console.debug(e);
            self.reject(e);
        });
    };

    function step4DerivePrivateKeyEncryptionKey() {
        console.debug('g4. derive key to encrypt private key');
        crypto.subtle.digest(
            {
                name: "SHA-256"
            },
            convertStringToArrayBufferView(self.encpassphrase)
        ).then(function(result){
            window.crypto.subtle.importKey(
                "raw",
                result,
                {
                    name: "AES-GCM"
                },
                false,
                ["encrypt", "decrypt"]
            ).then(function(key){
                console.debug('g4. derived ', key);
                self.encpassphraseDerivedKey = key;
                step5EncryptPrivateKey();
            }, function(e){
                console.debug(e);
                self.reject(e);
            });
        });

    };

    function step5EncryptPrivateKey() {
        var vector = crypto.getRandomValues(new Uint8Array(16));
        self.privateKeyVector = vector;
        self.privateKeyVectorHex = arrayToHex(vector);
        var privateKeyJwkJsonBytes = convertStringToArrayBufferView(JSON.stringify(self.privateKeyJwk));
        var encryptPromise = crypto.subtle.encrypt(
            {
                name: "AES-GCM",
                iv: vector
            },
            self.encpassphraseDerivedKey,
            privateKeyJwkJsonBytes
        );
        encryptPromise.then(
            function(result) {
                var encryptedBytes = new Uint8Array(result);
                console.debug('g5. encrypt vector ', vector);
                console.debug('g5. encryptedPrivateKeyBytes ', encryptedBytes);                
                self.privateKeyJwkEncryptedHex = arrayBufferToHexString(result);
                //enc.encryptedMessageBytes = result;
                //info.encryptedMessageHex = arrayBufferToHexString(encrypted_data);
                //enc.encryptedMessageHex = result;
                //console.debug('e2. encryptedMessageHex ', enc.encryptedMessageHex);
                console.debug('g5. encryptedPrivateKeyHex ', self.privateKeyEncryptedHex);
                self.callback(self);
            }, 
            function(e){
                console.error(e);
                self.reject(e);
            }
        );

    }

    

};

