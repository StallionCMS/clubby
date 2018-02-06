
<style lang="scss">
 .invite-user-vue {
     
 }
</style>


<template>
    <div class="invite-user-vue  one-column-screen">
        <form>
            <div class="form-group">
                <input type="text" v-model="password" class="form-control">
            </div>
            <div class="p">
                <button class="btn btn-primary btn-danger" @click="runTest">Run</button>
            </div>
            <div id="action-log"></div>
        </form>
    </div>
</template>

<script>

 
 module.exports = {
     data: function() {
         return {
             password: 'winfox',
             privateKeyJwkEncryptedHex: '',
             publicKeyJwkJson: '',
             privateKey: null,
             publicKey: null
         };
     },
     created: function() {
         this.onRoute();
     },
     watch: {
         '$route': 'onRoute'
     },
     methods: {
         onRoute: function() {

         },
         logStep: function(text) {
             $(this.$el).find('#action-log').append($('<div></div>').html(text));
         },
         runTest: function() {
             var self = this;
             self.logStep('Generating private/public keys...');
             new KeyGenerator().generate(
                 self.password,
                 function(result) {
                     self.logStep('Keys generated.');
                     self.privateKeyJwkEncryptedHex = result.privateKeyJwkEncryptedHex;
                     self.publicKeyJwkJson = result.publicKeyJwkJson;
                     self.privateKeyVectorHex = result.privateKeyVectorHex;
                     self.importPrivateKey();
                     
                 },
                 function(err) {
                     console.error(err);
                     self.logStep(err);
                 }
             );
         },
         importPrivateKey: function() {
             var self = this;
             self.logStep('Importing private key...');
             FetchPrivateKey(
                 self.privateKeyJwkEncryptedHex,
                 self.privateKeyVectorHex,
                 self.password
             ).then(function(result) {
                 self.privateKey = result;
                 self.logStep('Private key imported.');
                 self.importPublicKey();
             }).catch(function(err) {
                 console.error(err);
                 self.logStep(err);
             });
         },
         importPublicKey: function() {
             var self = this;
             self.logStep('importPublicKey');
             crypto.subtle.importKey(
                 'jwk',
                 JSON.parse(self.publicKeyJwkJson),
                 {
                     name: "RSA-OAEP",
                     modulusLength: 2048,
                     publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
                     hash: {name: "SHA-1"}
                 },
                 true,
                 ["encrypt"]
             ).then(function(result) {
                 console.log('public key imported.');
                 self.logStep('Imported public key.');
                 self.publicKey = result;
                 self.encryptMessage();
             }).catch(function(err) {
                 console.error(err);
                 self.logStep(err);
             });
         },
         encryptMessage: function() {
             var self = this;
             var text = 'A random message ' + new Date().getTime();
             console.log('encrypt message ', text);
             self.logStep('Encrypting messsage "' + text + '"...');
             var vector = crypto.getRandomValues(new Uint8Array(16));
             crypto.subtle.encrypt(
                 {
                     name: "RSA-OAEP",
                     iv: vector,
                     hash: {name: "SHA-1"}
                 },
                 self.publicKey,
                 stringToArrayBuffer(text)
             ).then(
                 function(result){
                     var encryptedBytes = new Uint8Array(result);
                     debug('encrypted test phrase');
                     self.decryptMessage(text, vector, encryptedBytes);
                 }, 
                 function(e){
                     console.error(e);
                     reject(err);
                 }
             );              
             
         },
         decryptMessage: function(message, vector, encryptedBytes) {
             var self = this;
             crypto.subtle.decrypt(
                 {
                     name: "RSA-OAEP",
                     iv: vector,
                     hash: {name: "SHA-1"}
                 },
                 self.privateKey,
                 encryptedBytes.buffer
             ).then(
                 function(result){
                     var decryptedMessage = convertArrayBufferViewtoString(result);
                     if (message === decryptedMessage) {
                         console.log('Encrypt/decrypt test passed!');
                         self.logStep('Encrypt/decrypt test passed!');
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
         
     }
 };


</script>
