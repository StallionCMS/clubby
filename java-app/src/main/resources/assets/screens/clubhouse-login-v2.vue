<style lang="scss">
</style>

<template>
    <div class="login-with-key-vue one-column-screen">
        <loading-div v-if="isLoading"></loading-div>
        <form v-if="!isLoading && $store.state.user && $store.state.user.id" @submit.prevent="encryptionLoginOnly">
            <h1>Unlock {{ $store.state.site.name }}</h1>
            <div class="p">
                You are logged in as <b>{{ $store.state.user.displayName }} (@{{$store.state.user.username}})</b>
            </div>
            <div class="p">
                <a href="#/logoff">Logoff and log in as different user.</a>
            </div>
            <div class="form-group">
                <label for="encryption-passphrase-field">Enter Your Encryption Passphrase</label>
                <input id="encryption-passphrase-field" type="password" class="form-group" required="true" v-model="encryptionPassword">
            </div>
            <div class="form-group">
                <button :disabled="processing" class="btn btn-primary">Unlock and Enter</button>
            </div>
        </form>
        <form v-if="!isLoading && (!$store.state.user || !$store.state.user.id)" @submit.prevent="doLogin">
            <h1>Login</h1>
            <div class="form-group">
                <label for="username-field">Username or Email</label>
                <input id="username-field" type="text" class="form-group" v-model="username" required="true" >
            </div>
            <div class="form-group">
                <label for="encryption-passphrase-field">Password</label>
                <input id="encryption-passphrase-field" type="password" class="form-group" required="true" v-model="encryptionPassword">
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
             isLoading: true,
             username: '',
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
             if (ClubhouseMobileInterop.isMobileApp) {
                 ClubhouseMobileInterop.redirectToLogin();
                 return;
             }
             if (theApplicationContext.isFirstUser) {
                 window.location.hash = '/first-user';
                 return
             }
             this.username = this.$route.query.username;
             this.encryptionPassword = this.$route.query.passphrase;
             if (this.username && this.encryptionPassword) {
                 this.doLogin();
             } else {
                 this.processing = false;
                 this.isLoading = false;
             }
         },
         doLogin: function() {
             var self = this;
             this.processing = true;
             console.log('doLogin');
             stallion.request({
                 url : '/clubhouse-api/auth/private-key-login-step1',                 
                 method: 'POST',
                 data: {
                     username: self.username
                 },
                 success: function(o1) {
                     console.log('got encrypted token from server ', o1.tokenKey, o1.tokenEncryptedHex);
                     clubhouseImportPrivateKey(self.encryptionPassword, o1.privateKeyJwkEncryptedHex, o1.privateKeyVectorHex).then(function(privateKey) {
                         clubhouseDecryptToken(
                             privateKey,
                             o1.tokenEncryptedHex
                         ).then(function(decryptedToken) {
                             stallion.request({
                                 url: '/clubhouse-api/auth/private-key-login-step2',
                                 method: 'POST',
                                 data: {
                                     username: self.username,
                                     tokenKey: o1.tokenKey,
                                     token: decryptedToken
                                 },
                                 success: function(o) {
                                     clubhouseImportPublicAndPrivateKey(
                                         self.encryptionPassword,
                                         o.userProfile
                                     ).then(function() {
                                         self.$store.commit('defaultChannelIdChange', o.defaultChannelId);
                                         stallionClubhouseApp.store.commit('login', {
                                             user: o.user,
                                             userProfile: o.userProfile
                                         });
                                         console.log('private key loaded, login complete');
                                         sessionStorage['private-key-passphrase-' + o.user.id] = self.encryptionPassword;
                                         
                                         ClubhouseVueApp.stateManager.start();
                                         ClubhouseVueApp.stateManager.loadContext(function(ctx) {
                                             Vue.nextTick(function() {
                                                 console.log('login.vue navigate home after tick');
                                                 window.location.hash = '/';
                                             });
                                         });
                                         console.log('login.vue nagivate home');
                                         window.location.hash = '/';
                                         
                                     }).catch(function(err) {
                                         self.processing = false;
                                         console.error('Private key import error: ', err);
                                         stallion.showError('Error importing private key. Please check your passphrase');
                                     });
                                     
                                 }
                             });
                         }).catch(function(err) {
                             console.error('error decrypting token ', err);
                         });
                     }).catch(function(err) {
                         console.error('error importing private key ', err);
                     });
                     

                     
                 },
                 error: function(o) {
                     self.processing = false;
                     self.isLoading = false;
                     stallion.showError(o.message || o + '');
                 }
             });
         }
     }
 }
</script>
