<style lang="scss">
 .manual-key-generation-vue {
 input, textarea {
     padding: 3px;
     width: 100%;
     
 }
 textarea {
     width: 100%;
     height: 110px;
 }
 label {
     display: block;
     margin-top: 2em;
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
 .the-main-column {
     width: 600px;
 }
 }
</style>

<template>
    <div class="manual-key-generation-vue">
        <div class="the-main-column">
            <div>
                <label>Password</label>
                <input type="text" class="form-control" v-model="password" >
            </div>
            <div>
                <label>Encrypted Private Key</label>
                <textarea readonly="true" class="form-control" v-model="privateKeyEncryptedHex"></textarea>
            </div>
            <div>
                <label>Private Key Vector Hex</label>
                <textarea readonly="true" class="form-control" v-model="privateKeyEncryptionVectorHex"></textarea>
            </div>
            <div>
                <label>Public Key Hex</label>
                <textarea readonly="true" class="form-control" v-model="publicKeyHex" ></textarea>
            </div>
            <div class="p">
                <button @click="generate">Generate</button>
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
             password: 'winfox',
             privateKeyEncryptedHex: '',
             privateKeyEncryptionVectorHex: '',
             publicKeyHex: ''
         }
     },
     methods: {
         generate: function() {
             var self = this;
             new KeyGenerator().generate('george', self.password, function(result) {
                 self.privateKeyEncryptedHex = result.privateKeyEncryptedHex;
                 self.privateKeyEncryptionVectorHex = result.privateKeyEncryptionVectorHex;
                 self.publicKeyHex = result.publicKeyHex;
             });
         }
     }
 };
</script>


