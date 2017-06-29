<style lang="scss">
 .encryption-sandbox-vue {
     padding: 20px;
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
     }
</style>

<template>
    <div class="encryption-sandbox-vue">
        <h2>Encryption Sandbox</h2>
        <div>
            <div class="form-row">
                <label>Encryption Passphrase</label>
                <input type="text" v-model="georgePassword">
            </div>
            <div class="form-row">
                <div>Message to send:</div>
                <textarea v-model="messageFromGeorge"></textarea>
            </div>
            <div class="form-row">
                <button @click="run">Encrypt & Send</button>
            </div>
            <hr>
            <div class="p">
                <label>Profile JSON</label>
                <textarea readonly="true" v-model="profileJson"></textarea>
            </div>
            <div class="p">
                <label>Encrypted Message</label>
                <textarea readonly="true" v-model="encryptedMessageJson"></textarea>
            </div>
            <div class="p">
                <label>Decrypted Message</label>
                <textarea readonly="true" v-model="resultMessage"></textarea>
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
             messageFromGeorge: 'Hi john, this is gearge at ' + new Date().getTime(),
             georgePassword: 'winfox',

             profile: null,
             
             encryptedMessageJson: '',
             publicKeyJwkString: '',
             encryptedPrivateKeyJwkString: '',
             resultMessage: ''
             
         }
     },
     computed: {
         profileJson: function() {
             return JSON.stringify(this.profile, 4);
         }
     },
     methods: {
         run: function() {
             var self = this;
             clubhouseGeneratePrivateAndPublicKey(self.georgePassword).then(function(result) {
                 self.encryptedPrivateKeyJwkString = result.privateKeyEncryptedHex;
                 self.publicKeyJwkString = result.publicKeyHex;
                 self.profile = {
                     publicKeyHex: result.publicKeyHex,
                     publicKeyJwkString: result.publicKeyHex,
                     encryptedPrivateKeyJwkString: result.privateKeyEncryptedHex,
                     encryptedPrivateKeyHex: result.privateKeyEncryptedHex,
                     encryptedPrivateKeyInitializationVectorHex: result.privateKeyEncryptionVectorHex
                 }
                 console.log('es1. Generated private & public key, now encrypting message');
                 self.encryptMessage();
             }).catch(function(e) {
                 console.error('error generating new keys ', e);
             });
         },
         encryptMessage: function() {
             var self = this;
             clubhouseImportPublicAndPrivateKey(self.georgePassword, self.profile).then(function(result) {
                 console.log('es2. imported private & public key, now encrypting message');

                 var tos = [{
                     userId: 100,
                     username: 'george',
                     publicKey: result.publicKey
                 }];
                     
                 clubhouseEncryptMessage(
                     self.messageFromGeorge,
                     tos
                 ).then(function(result) {
                         
                         var userMessage = result.encryptedPasswords[0];
                         self.encryptedMessageJson = {
                             message: {
                                 encryptedMessageHex: result.encryptedMessageHex,
                                 messageVectorHex: result.messageVectorHex
                             },
                             userMessage: userMessage
                         }
                         console.log('es3. message imported, now decrypt');
                         self.decryptMessage(
                             result.encryptedMessageHex,
                             result.messageVectorHex,
                             userMessage
                         );
                     }
                 ).catch(function(err) {
                     console.error(err);
                 });
                 
             }).catch(function(err) {
                 console.error(err);
             });
         },
         decryptMessage: function(encryptedMessageHex, messageVectorHex, userMessage) {
             var self = this;
             clubhouseImportPublicAndPrivateKey(self.georgePassword, self.profile).then(function(result) {
                 self.privateKey = result.privateKey;
                 self.publicKey = result.publicKey;
                 console.log('es4. keys imported, now decrypt');
                 clubhouseDecryptMessage(
                     result.privateKey,
                     hexToArray(encryptedMessageHex),
                     hexToArray(messageVectorHex),
                     hexToArray(userMessage.encryptedPasswordHex),
                     hexToArray(userMessage.passwordVectorHex)
                 ).then(function(message) {
                         console.log('es5. decryption complete');
                         self.resultMessage = message;
                     }
                 ).catch(function(err) {
                     console.error(err);
                 });
                     

             }).catch(function(err) {
                 console.error(err);
             });
         }
     }
 };
</script>


