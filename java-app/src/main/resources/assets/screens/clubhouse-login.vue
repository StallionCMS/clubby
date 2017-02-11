<style lang="scss">
</style>

<template>
    <div class="clubhouse-login-vue">
        <form @submit.prevent="doLogin">
            <div class="form-group">
                <label>Username or Email</label>
                <input type="text" class="form-group" v-model="username">
            </div>
            <div class="form-group">
                <label>Password</label>
                <input type="password" class="form-group" v-model="password">
            </div>
            <div class="form-group">
                <label>Encryption Passphrase</label>
                <input type="password" class="form-group" v-model="encryptionPassword">
            </div>
            <div class="form-group">
                <button :disabled="processing" class="btn btn-primary">Log in</button>
            </div>
        </form>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             username: '',
             password: '',
             encryptionPassword: '',
             processing: false
         }
     },
     created: function() {
         this.loadFromQuery()
     },
     watch: {
         '$route': 'loadFromQuery'
     },     
     methods: {
         loadFromQuery: function() {
             this.password = this.$route.query.password;
             this.username = this.$route.query.username;
             this.encryptionPassword = this.$route.query.encryptionPassword;
             this.processing = false;
         },
         doLogin: function() {
             var self = this;
             this.processing = true;
             stallion.request({
                 url : '/clubhouse-api/auth/login',
                 method: 'POST',
                 data: {
                     username: self.username,
                     password: self.password
                 },
                 success: function(o) {
                     console.log(o);
                     o.encryptedPrivateKeyInitializationVectorHex;
                     o.publicKeyHex;
                     o.encryptedPrivateKeyHex;
                     stallionClubhouseApp.store.commit('login', {
                         user: o.user,
                         userProfile: o.userProfile
                     });
                     new VueKeyImporter(self.encryptionPassword, function() {
                         console.log('private key loaded, login complete');
                         window.location.hash = '/channel/' + o.defaultChannelId;
                     }).importPublicAndPrivate();
                 },
                 error: function(o) {
                     self.processing = false;
                     stallion.showError(o);
                 }
             });
         }
     }
 }
</script>
