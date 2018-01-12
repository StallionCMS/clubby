<style lang="scss">
 .app-home-vue {
     .the-main-column {
         padding: 20px;
         max-width: 700px;
         .btn {
             width: 100%;
         }
     }
 }
</style>

<template>
    <div class="app-home-vue">
        <div class="the-main-column" v-if="isLoaded">
            <div class="p">
                <a href="#/login" class="btn btn-danger btn-lg">Login</button>
            </div>
            <div class="p">
                <button href="#/register" class="btn btn-primary btn-lg">Register</button>
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
             publicKeyHex: '',
             isLoaded: false
         }
     },
     created: function() {
         this.onRoute();
     },
     watch: {
         '$route': 'onRoute'
     },
     methods: {
         onRoute: function() {
             if (theApplicationContext.isFirstUser) {
                 window.location.hash = '/first-user';
                 return;
             }
             if (this.$store.state.user && this.$store.state.user.id) {
                 if (localStorage && localStorage['last-channel-path|' + this.$store.state.user.id) {
                     window.location.hash = localStorage['last-channel-path|' + this.$store.state.user.id;
                 } else if (this.$store.state.defaultChannelId) {
                     window.location.hash = '#/channel/' + this.$store.state.defaultChannelId;
                 } else {
                     window.location.hash = '#/my-channels';
                 }
             } else {
                 window.location.hash = '#/login';
             }
         }
     }
 };
</script>


