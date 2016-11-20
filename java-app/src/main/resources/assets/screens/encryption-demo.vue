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
                <button @click="sendMessage('george', 'john')">Send Message</button>
            </div>
            <div class="form-row">
                <button @click="makeNewKeys">Make new key pairs</button>
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
                <button @click="sendMessage('john', 'george')">Send Message</button>
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
         makeNewKeys: function() {
             var self = this;
             new KeyGenerator().generate('george', self.georgePassword, function() {
                 console.log('new key generated for george');
                 new KeyGenerator().generate('john', self.johnPassword, function() {
                     console.log('new key generated for john');
                 });
             });
         },
         sendMessage: function(from, to) {
             var self = this;
             console.log('send message ', from, to);
             var password = self.georgePassword;
             if (to === 'john') {
                 password = self.johnPassword;
             }
             var self = this;
             //KeyManager.generateIfNotExists(to, password, function() {
             //    console.log('key generated for ' + to);
             new KeyGenerator().generate('john', self.johnPassword, function() {
                 KeyManager.getPublicKey(to, function(publicKey) {
                     console.log('got public key, call doSendMessage');
                     self.doSendMessage(from, to, publicKey);
                 });
             });
             //});
             //return;
             //self.createAndSaveKeyPair(to, function() {
             //    self.doSendMessage(from, to);
             //});
         },
         doSendMessage: function(from, to, toPublicKey) {
             var self = this;
             var message = self.messageFromGeorge;
             var publicKey = toPublicKey;
             if (from === 'john') {
                 message = self.messageFromJohn;
             }
             new Encrypter().encryptMessage(
                 message,
                 [
                     {
                         username: to,
                         userId: to,
                         publicKey: publicKey
                     }],
                 self.onMessageEncrypted
             );
         },
         onMessageEncrypted: function(info) {
             console.log('onMessageEncrypted -- message was encrypted ', info);
             this.decryptMessage(info);
         },
         decryptMessage: function(info) {
             var self = this;
             var ep = info.encryptedPasswords[0];
             console.log('decrypt message to info.encryptedPasswords[0].username ', ep.username, ep.encryptedPasswordBytes);

             KeyManager.getMyPrivateKey(ep.username, self[ep.username + 'Password'], function(privateKey) {
                 new Decrypter().decryptMessage(
                     privateKey,
                     info.encryptedMessageBytes,
                     info.messageVector,
                     ep.encryptedPasswordBytes,
                     ep.passwordVector,
                     function(message) {
                         console.log('decrypted message ', message);
                         if (ep.username === 'john') {
                             self.decryptedFromGeorgeForJohn = message;
                         } else {
                             self.decryptedFromJohnForGeorge = message;
                         }
                     }
                 );
             });
         },
         createAndSaveKeyPair: function(username, callback) {
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
             ).then(function(keyPair) {
                 self.saveKey(keyPair.publicKey, keyPair.privateKey, username, callback);
             }).catch(function(err) {
                 alert("Could not create and save new key pair: " + err.message);
             });             
         },
         saveKey: function(publicKey, privateKey, username, callback) {
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
                 callback();
             });
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


