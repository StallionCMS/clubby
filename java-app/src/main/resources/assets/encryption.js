


var clubhouseEncryptMessage = function(message, tos) {
    return new Promise(function(resolve, reject) {
        new Encrypter()
            .encryptMessage(message, tos, resolve, reject);
    });
}

var clubhouseReEncryptMessage = function(message, privateKey, encryptedPasswordHex, passwordVectorHex) {
    return new Promise(function(resolve, reject) {
        new ReEncrypter()
            .rencryptMessage(
                message, privateKey, encryptedPasswordHex, passwordVectorHex, resolve, reject
            );
    });
}

var clubhouseDecryptMessage = function(privateKey, encryptedMessageBytes, messageVector, encryptedPasswordBytes, passwordVector) {
    return new Promise(function(resolve, reject) {
        new Decrypter()
            .decryptMessage(
                privateKey, encryptedMessageBytes, messageVector, encryptedPasswordBytes, passwordVector, resolve, reject
             );
    });
}




var Encrypter = function() {
    var self = this;
    var enc = this;

    enc.encryptMessage = function(message, tos, callback, reject) {
        console.log('encrypt message ', message, tos);
        enc.message = message;
        enc.tos = tos;
        enc.symettricKey = null;
        enc.encryptedPasswordHex = null;
        enc.encryptedPasswordBytes = null;
        enc.encryptedMessageHex = null;
        enc.encryptedMessageBytes = null;
        enc.passwordVector = null;
        enc.messageVector = null;
        enc.messageVectorHex = null;
        enc.callback = callback;
        enc.reject = reject;
        step1GenerateSymettricKey();
    };


    function step1GenerateSymettricKey() {
        var password = arrayBufferToHexString(crypto.getRandomValues(new Uint8Array(32)));
        enc.password = password;
        enc.passwordBytes = convertStringToArrayBufferView(password);
        console.log('e1. password ', password);
        //password = self.messageFromGeorge;
        //var password = "password";
        var key = null;
        crypto.subtle.digest(
            {
                name: "SHA-256"
            },
            convertStringToArrayBufferView(password)
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
                console.log('e1. derived key ', key);
                enc.symettricKey = key;
                step2EncryptMessage();
            }, function(e){
                console.log(e);
                enc.reject(e);
            });
        }).catch(function(e) {
            console.log(e);
            enc.reject(e);
        });
    }

    function step2EncryptMessage() {
        var vector = crypto.getRandomValues(new Uint8Array(16));
        enc.messageVector = vector;
        enc.messageVectorHex = arrayBufferToHexString(enc.messageVector);
        console.log('e2. message ', enc.message);
        encrypt_promise = crypto.subtle.encrypt(
            {
                name: "AES-GCM",
                iv: vector
            },
            enc.symettricKey,
            convertStringToArrayBufferView(enc.message)
        );
        encrypt_promise.then(
            function(result){
                var encrypted_data = new Uint8Array(result);
                enc.encryptedMessageBytes = result;
                //info.encryptedMessageHex = arrayBufferToHexString(encrypted_data);
                enc.encryptedMessageHex = arrayBufferToHexString(result);
                console.log('e2. encryptedMessageHex ', enc.encryptedMessageHex);
                step3EncryptPassword();
            }, 
            function(e){
                console.error(e);
                enc.reject(e);
            }
        );

    }

    
    function step3EncryptPassword() {
        self.encryptedPasswords = [];

        

        enc.tos.forEach(function(to) {
            var vector = crypto.getRandomValues(new Uint8Array(16));
            console.log('e3. ', enc.passwordBytes, ' key ', to.publicKey, 'vector ', vector);
            var encrypt_promise = crypto.subtle.encrypt(
                {
                    name: "RSA-OAEP",
                    iv: vector,
                    hash: {name: "SHA-1"}
                },
                to.publicKey,
                enc.passwordBytes
            );
            encrypt_promise.then(
                function(result){
                    var encPasswordBytes = new Uint8Array(result);
                    console.log('e3. vector ', vector);
                    console.log('e3. encPasswordResult ', result);
                    var encryptedPasswordHex = arrayBufferToHexString(encPasswordBytes);
                    console.log('e3. encPasswordBytes ', encPasswordBytes);                     
                    console.log('e3. encrypted password hex ', encryptedPasswordHex);
                    self.encryptedPasswords.push({
                        userId: to.userId,
                        username: to.username,
                        passwordVector: vector,
                        passwordVectorHex: arrayBufferToHexString(vector),
                        encryptedPasswordHex: encryptedPasswordHex,
                        encryptedPasswordBytes: encPasswordBytes
                    })
                }, 
                function(e){
                    console.error(e);
                    enc.reject(e);
                }
            );                
        });


        var inter = setInterval(function() {
            console.log('check for encrypted passwords all generated');
            if (self.encryptedPasswords === null || self.encryptedPasswords === undefined) {
                console.log('self.encryptedPasswords is undefined ', self.encryptedPasswords, self);
                clearInterval(inter);
                return false;
            }
            if (self.encryptedPasswords.length === enc.tos.length) {
                console.log("e3+. Generated all encrypted passpharses: ", self.encryptedPasswords);
                clearInterval(inter);
                enc.callback(self);
                return false;
            }
            
        }, 20);

                                

    }
}



var ReEncrypter = function() {
    var enc = this;
    var self = this;
    
    enc.reencryptMesage = function(message, privateKey, encryptedPasswordHex, passwordVectorHex, callback) {
        
        enc.message = message;
        enc.privateKey = privateKey;        
        enc.encryptedPasswordHex = encryptedPasswordHex;
        enc.passwordVectorHex = passwordVectorHex;
        enc.callback = callback;
        
        enc.encryptedPasswordBytes = hexToArray(encryptedPasswordHex);
        enc.passwordVector = hexToArray(passwordVectorHex);
        
        enc.password = '';
        enc.symettricKey = null;
        enc.encryptedMessageHex = null;
        enc.messageVectorHex = '';
        enc.encryptedMessageBytes = null;
        enc.messageVectorHex = null;
        
        step1DecryptPassword();
    }


    function step1DecryptPassword() {
        console.log('d1. encryptedPassword ', self.encryptedPasswordBytes);
        var decryptPromise = crypto.subtle.decrypt(
            {
                name: "RSA-OAEP",
                iv: self.passwordVector,
                hash: {name: "SHA-1"}
            },
            self.privateKey,
            self.encryptedPasswordBytes
        );
        decryptPromise.then(
            function(result){
                var decrypted = new Uint8Array(result);
                console.log('d1. Decrypted passphrase ', result, decrypted, convertArrayBufferViewtoString(decrypted));
                self.password = convertArrayBufferViewtoString(decrypted);
                step2PasswordToSymettricKey();
            },
            function(e){
                console.error(e);
                
            }
        );              
    }

    function step2PasswordToSymettricKey() {
        console.log('d2. password to key ', self.password);
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
                    name: "AES-GCM"
                },
                false,
                ["encrypt", "decrypt"]
            ).then(function(key){
                console.log('d2. re-derived key ', key);
                self.symettricKey = key;
                step3EncryptMessage();
            }, function(e){
                console.error(e);
            });
        });      
    }
    
    

    function step3EncryptMessage() {
        var vector = crypto.getRandomValues(new Uint8Array(16));
        enc.messageVector = vector;
        enc.messageVectorHex = arrayBufferToHexString(enc.messageVector);
        console.log('e2. message ', enc.message);
        encrypt_promise = crypto.subtle.encrypt(
            {
                name: "AES-GCM",
                iv: vector
            },
            enc.symettricKey,
            convertStringToArrayBufferView(enc.message)
        );
        encrypt_promise.then(
            function(result){
                var encrypted_data = new Uint8Array(result);
                enc.encryptedMessageBytes = result;
                //info.encryptedMessageHex = arrayBufferToHexString(encrypted_data);
                enc.encryptedMessageHex = arrayBufferToHexString(result);
                console.log('e2. encryptedMessageHex ', enc.encryptedMessageHex);
                self.callback(self);
            }, 
            function(e){
                console.error(e);
            }
        );

    }

    
    

};


var Decrypter = function() {
    var dec = this;
    var self = this;

    dec.decryptMessage = function(privateKey, encryptedMessageBytes, messageVector, encryptedPasswordBytes, passwordVector, callback, reject) {
        dec.privateKey = privateKey;
        dec.encryptedMessageBytes = encryptedMessageBytes;
        dec.messageVector = messageVector;
        dec.encryptedPasswordBytes = encryptedPasswordBytes;
        dec.passwordVector = passwordVector;
        dec.callback = callback;
        dec.password = null;
        dec.symettricKey = null;
        dec.reject = reject;
        step1DecryptPassword();
    };

    function step1DecryptPassword() {
        console.log('d1. encryptedPassword ', self.encryptedPasswordBytes);
        crypto.subtle.decrypt(
            {
                name: "RSA-OAEP",
                iv: self.passwordVector,
                hash: {name: "SHA-1"}
            },
            self.privateKey,
            self.encryptedPasswordBytes
        ).then(
            function(result){
                var decrypted = new Uint8Array(result);
                console.log('d1. Decrypted passphrase ', result, decrypted, convertArrayBufferViewtoString(decrypted));
                self.password = convertArrayBufferViewtoString(decrypted);
                step2PasswordToSymettricKey();
            },
            function(e){
                console.error(e);
                dec.reject(e);
            }
        )
    }

    function step2PasswordToSymettricKey() {
        console.log('d2. password to key ', self.password);
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
                    name: "AES-GCM"
                },
                false,
                ["encrypt", "decrypt"]
            ).then(function(key){
                console.log('d2. re-derived key ', key);
                self.symettricKey = key;
                step3DecryptMessage();
            }, function(e){
                console.error(e);
                dec.reject(e);
            });
        }).catch(function(e) {
            console.error(e);
            dec.reject(e);
        });
    }

    function step3DecryptMessage() {
        console.log('d3. decrypt message', self.messageVector, self.symettricKey, self.encryptedMessageBytes);
        decrypt_promise = crypto.subtle.decrypt(
            {
                name: "AES-GCM",
                iv: self.messageVector
            },
            self.symettricKey,
            self.encryptedMessageBytes
        );
        decrypt_promise.then(
            function(result){
                var bytes = new Uint8Array(result);
                var message = convertArrayBufferViewtoString(bytes);
                console.log('received message ', message);
                self.callback(message);
            }, 
            function(e){
                console.error(e);
                dec.reject(e);
            }
        );            
    }


};
