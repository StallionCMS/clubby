<style lang="scss">
 input, textarea {
     padding: 3px;
     width: 100%;
     
 }
 label {
     display: block;
 }
 .form-row {
     margin-top: 1em;
 }
 h3:first-child {
     margin: 0em 0em 0em 0em;
 }
 .app-home-vue {
     display: flex;
     flex-direction: row;
 }
 .george-column {
     width: 45%;
     padding: 10px;
     
 }
 .john-column {
     width: 45%;
     padding: 10px;
     margin-left: 20px;
 } 
</style>

<template>
    <div class="app-home-vue">
        <div class="george-column">
            <h3>George</h3>
            <div class="form-row">
                <label>Password</label>
                <input type="text" v-model="georgePassword">
            </div>
            <div class="form-row">
                <label>Decrypted Private Key</label>
                <textarea v-model="georgePrivateKey"></textarea>
            </div>
            <div class="form-row">
                <label>Message recieved:</label>
                {{ decryptedFromJohnForGeorge || 'No message' }}
            </div>
            <div class="form-row">
                <div>Message to send:</div>
                <textarea v-model="messageFromGeorge"></textarea>
            </div>
            <div class="form-row">
                <button @click="sendToJohn">Send Message</button>
            </div>
            <div class="form-row">
                <button @click="createAndSaveKeyPair('george')">Make new key pair</button>
            </div>
            <div class="form-row">
                <button @click="loadFromLocalStorage('george')">Load from localStorage</button>
            </div>
        </div>
        <div class="john-column">
            <h3>John</h3>
            <div class="form-row">
                <label>Password</label>
                <input type="text" v-model="johnPassword">
            </div>
            <div class="form-row">
                <div>Message recieved:</div>
                {{ decryptedFromGeorgeForJohn }}
            </div>
            <div class="form-row">
                <div>Message to send:</div>
                <textarea v-model="messageFromJohn"></textarea>
            </div>
            <div class="form-row">
                <button @click="sendToGeorge">Send Message</button>
            </div>
        </div>
        
    </div>
</template>

<script>
 // Base on Tutorials & Reference
 // http://qnimate.com/asymmetric-encryption-using-web-cryptography-api/
 // http://qnimate.com/symmetric-encryption-using-web-cryptography-api/
 // http://qnimate.com/passphrase-based-encryption-using-web-cryptography-api/
 // https://blog.engelke.com/2015/02/14/deriving-keys-from-passwords-with-webcrypto/
 // 
 
 window.keys = window.keys || {};
 module.exports = {
     data: function() {
         return {
             name: 'Joe',
             decryptedFromJohnForGeorge: '',
             decryptedFromGeorgeForJohn: 'asdf',
             georgePrivateKey: '',
             johnPrivateKey: '',
             messageFromJohn: '',
             messageFromGeorge: 'Hi john, this is gearge at ' + new Date().getTime(),
             georgePassword: 'winfox',
             johnPassword: 'xofniw'
         }
     },
     methods: {
         createAndSaveKeyPair: function(username) {
             var algorithmName = "RSA-OAEP";
             var usages = ["encrypt", "decrypt"];
             var self = this;
             window.crypto.subtle.generateKey(
                 {
                     name: algorithmName,
                     modulusLength: 2048,
                     publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
                     hash: {name: "SHA-256"}
                 },
                 false,  // Cannot extract new key
                 usages
             ).
                    then(function(keyPair) {
                        return self.saveKey(keyPair.publicKey, keyPair.privateKey, username);
                    }).
                    catch(function(err) {
                        alert("Could not create and save new key pair: " + err.message);
                    });             
         },
         saveKey: function(publicKey, privateKey, username) {
             console.log('publicKey ', publicKey, 'privateKey ', privateKey, username);
             window.keys[username] = {
                 publicKey: publicKey,
                 privateKey: privateKey
             };
             window.crypto.subtle.exportKey('spki', publicKey).then(function(spki) {
                 var savedObject = {
                     publicKey:  publicKey,
                     privateKey: privateKey,
                     name:       name,
                     spki:       arrayBufferToHexString(spki)
                 };
                 console.log('savedObject ', savedObject);
                 console.log(JSON.stringify(savedObject));
                 console.log(JSON.stringify(JSON.parse(JSON.stringify(savedObject))));
                 localStorage['st_clubhouse_key_' + username] = JSON.stringify(savedObject);
             });
         },
         loadFromLocalStorage: function(username) {
             var pair = this.loadKeyPair(username);
             console.log(pair);
         },
         loadKeyPair: function(username) {
             if (localStorage['st_clubhouse_key_' + username]) {
                 return JSON.parse(localStorage['st_clubhouse_key_' + username]);
             }
         },
         loadKeyPairWithEncryptedPrivateKey: function(name) {

         },
         deriveKeyFromPassphrase: function() {
             var self = this;
             window.crypto.subtle.importKey(
                 "raw",
                 stringToArrayBuffer(self.georgePassword),
                 {"name": "PBKDF2"},
                 false,
                 ["deriveKey"]
             ).
                    // Derive a key from the password
                    then(function(baseKey){
                        var salt = 'feelinsalty';
                        var iterations = 500;
                        var hash = "SHA-256";
                        console.log('derive key ', baseKey);
                        return window.crypto.subtle.deriveKey(
                            {
                                "name": "PBKDF2",
                                "salt": stringToArrayBuffer(salt),
                                "iterations": iterations,
                                "hash": hash
                            },
                            baseKey,
                            {"name": "AES-CBC", "length": 128}, // Key we want
                            true,                               // Extrable
                            ["encrypt", "decrypt"]              // For new key
                        );
                    }).
        // Export it so we can display it
                    then(function(aesKey) {
                        return window.crypto.subtle.exportKey("raw", aesKey);
                    }).
                    // Display it in hex format
                    then(function(keyBytes) {
                        console.log(keyBytes);
                        var hexKey = arrayBufferToHexString(keyBytes);
                        console.log('hex private key ', hexKey);
                        self.georgePrivateKey = hexKey;
                    }).
                    catch(function(err) {
                        alert("Key derivation failed: " + err.message);
                    });             
         },
         sendToGeorge: function() {

         },
         sendToJohn: function() {
             var self = this;
             if (!window.keys['george']) {
                 this.createAndSaveKeyPair('george');
             }
             if (!window.keys['john']) {
                 this.createAndSaveKeyPair('john');
             }
             setTimeout(function() {
                 console.log('sending');
                 self.doSendMessage('george', 'john');
                 
             }, 2000);
         },
         doSendMessage: function(from, to) {
             var self = this;
             this.step1GenerateSymettricKey(
                 {
                     from: from,
                     to: to,
                     password: '',
                     symettricKey: null,
                     message: self.messageFromGeorge,
                     encryptedPasswordHex: null,
                     encryptedMessageHex: null,
                     passwordVector: null,
                     messageVector: null
                 }
             );
         },
         step1GenerateSymettricKey: function(info) {
             var self = this;
             var password = arrayBufferToHexString(crypto.getRandomValues(new Uint8Array(32)));
             info.password = password;
             console.log('password ', password);
             console.log('MSG: ', self.messageFromGeorge);
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
                         name: "AES-CBC"
                     },
                     false,
                     ["encrypt", "decrypt"]
                 ).then(function(e){
                     key = e;
                     console.log('derived key ', key);
                     info.symettricKey = key;
                     self.step2EncryptPassphrase(info);
                 }, function(e){
                     console.log(e);
                 });
             });
         },
         step2EncryptPassphrase: function(info) {
             //iv: Is initialization vector. It must be 16 bytes
             var self = this;
             var vector = crypto.getRandomValues(new Uint8Array(16));
             var passwordBytes = convertStringToArrayBufferView(info.password);
             console.log('1. password bytes ', passwordBytes);
             info.passwordVector = vector;
             var encrypt_promise = crypto.subtle.encrypt(
                 {
                     name: "RSA-OAEP",
                     iv: vector
                 },
                 window.keys[info.to].publicKey,
                 passwordBytes
             );
             encrypt_promise.then(
                 function(result){
                     var encPasswordBytes = new Uint8Array(result);
                     console.log('1. vector ', vector);
                     console.log('encPasswordResult ', result);

                     info.encryptedPasswordHex = arrayBufferToHexString(encPasswordBytes);
                     info.encryptedPasswordBytes = encPasswordBytes;
                     console.log('1. encPasswordBytes ', encPasswordBytes);                     
                     console.log('1. encrypted password hex ', info.encryptedPasswordHex);
                     self.step3EncryptMessage(info);
                 }, 
                 function(e){
                     console.log(e.message);
                 }
             );             
                          
         },
         step3EncryptMessage: function(info) {
             var self = this;
             //iv: Is initialization vector. It must be 16 bytes
             var vector = crypto.getRandomValues(new Uint8Array(16));
             info.messageVector = vector;
             encrypt_promise = crypto.subtle.encrypt(
                 {
                     name: "AES-CBC",
                     iv: vector
                 },
                 info.symettricKey,
                 convertStringToArrayBufferView(info.message)
             );
             encrypt_promise.then(
                 function(result){
                     var encrypted_data = new Uint8Array(result);
                     //info.encryptedMessageHex = arrayBufferToHexString(encrypted_data);
                     info.encryptedMessageHex = result;
                     console.log('encryptedMessageHex ', info.encryptedMessageHex);
                     self.step4DecryptPassphrase(
                         info.to,
                         info.encryptedMessageHex,
                         info.messageVector,
                         info.encryptedPasswordHex,
                         info.passwordVector,
                         info.encryptedPasswordBytes
                     );
                 }, 
                 function(e){
                     console.log(e.message);
                 }
             );
             //self.receiveEncryptedMessage(from, to, encrypted_data, vector);
         },
         step4DecryptPassphrase(to, encryptedMessageHex, messageVector, encryptedPasswordHex, passwordVector, encryptedPasswordBytes) {
             var info = {
                 to: to,
                 password: null,
                 encryptedMessageHex: encryptedMessageHex,
                 encryptedPasswordHex: encryptedPasswordHex,
                 passwordVector: passwordVector,
                 messageVector: messageVector,
                 symettricKey: null,
                 message: null
             }
             console.log('4. decrypt passphrase');
             var passwordBytes = hexToArrayBuffer(encryptedPasswordHex);
             console.log('2 encrypted password bytes ', encryptedPasswordBytes);
             console.log('2 iv ', passwordVector);
             console.log('2 privateKey ', window.keys[to].privateKey);
             //window.keys[to].privateKey
             var self = this;
             var decrypt_promise = crypto.subtle.decrypt(
                 {
                     name: "RSA-OAEP",
                     iv: passwordVector
                 },
                 window.keys[to].privateKey,
                 encryptedPasswordBytes
             );
             decrypt_promise.then(
                 function(result){
                     var decrypted_data = new Uint8Array(result);
                     
                     console.log('Decrypted passphrase ', result, decrypted_data, convertArrayBufferViewtoString(decrypted_data));
                     info.password = convertArrayBufferViewtoString(decrypted_data);
                     self.step5PassphraseToSymmetricKey(info);
                 },
                 function(e){
                     console.error(e);

                 }
             );             
         },
         step5PassphraseToSymmetricKey: function(info) {
             var self = this;

             crypto.subtle.digest(
                 {
                     name: "SHA-256"
                 },
                 convertStringToArrayBufferView(info.password)
             ).then(function(result){
                 window.crypto.subtle.importKey(
                     "raw",
                     result,
                     {
                         name: "AES-CBC"
                     },
                     false,
                     ["encrypt", "decrypt"]
                 ).then(function(e){
                     key = e;
                     console.log('step 5 re-derived key ', key);
                     info.symettricKey = key;
                     self.step6DecryptMessage(info);
                 }, function(e){
                     console.error(e);
                 });
             });             
         },
         step6DecryptMessage: function(info) {
             var self = this;
             console.log('step 6 decrypt message');
             decrypt_promise = crypto.subtle.decrypt(
                 {
                     name: "AES-CBC",
                     iv: info.messageVector
                 },
                 info.symettricKey,
                 info.encryptedMessageHex
             );
             decrypt_promise.then(
                 function(result){
                     var bytes = new Uint8Array(result);
                     var message = convertArrayBufferViewtoString(bytes);
                     console.log('received message ', message);
                 }, 
                 function(e){
                     console.error(e);
                 }
             );             
         },
         receiveEncryptedMessage: function(from, to, encrypted, vector) {
             var self = this;
             var decrypt_promise = crypto.subtle.decrypt(
                 {
                     name: "RSA-OAEP",
                     iv: vector
                 },
                 window.keys[to].privateKey,
                 encrypted
             );
             decrypt_promise.then(
                 function(result){
                     decrypted_data = new Uint8Array(result);
                     console.log('Decrypted: ', convertArrayBufferViewtoString(decrypted_data));
                     self.decryptedFromGeorgeForJohn = convertArrayBufferViewtoString(decrypted_data);
                 },
                 function(e){
                     console.log(e.message);
                 }
             );             
         },
         createKey: function() {
             window.crypto.subtle.generateKey(
                 //                 algorithmIdentifier, extractableFlag, keyUsagesList
                 {
                     name: "RSA-OAEP",
                     modulusLength: 2048,
                     publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
                     hash: {name: "SHA-256"}
                 },
                 true,
                 ["encrypt", "decrypt"]
             ).then(function(pair, b, c) {
                 console.log('key generated ', pair, b, c);
                 console.log('exported ', window.crypto.subtle.exportKey('raw', pair.publicKey));
             }).catch(function(a, b, c) {
                 console.log('key gen failed', a, b, c);
             });
         }
     }
 };
</script>


