<style lang="scss">
</style>

<template>
    <div class="login-with-key-vue one-column-screen">
        <loading-div v-if="isLoading"></loading-div>
        <form v-if="!isLoading && (screen === 'new-login')" @submit.prevent="doLogin">
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
                <button v-disable-for-processing="processing" class="btn btn-primary loading-mask">Log in</button>
            </div>
        </form>
        <form v-if="!isLoading && (screen === 'email-second-factor')" @submit.prevent="submitEmailSecondFactor">
            <h1>Check your email for a confirmation code</h1>
            <div class="form-group">
                <label for="code">Your code</label>
                <input id="code-field" type="text" class="form-group" v-model="secondFactorCode" required="true" >
            </div>
            <div class="checkbox">
                <label><input type="checkbox" v-model="rememberThisDevice"> Remember this device?</label>
            </div>
            <div class="form-group">
                <button :disabled="processingSecondFactor" class="btn btn-primary">Confirm</button>
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
             userId: null,
             encryptionPassword: '',
             processing: false,
             rememberThisDevice: true,
             processingSecondFactor: false,
             secondFactorKey: '',
             secondFactorCode: '',
             screen: 'new-login'
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
             var self = this;

             /*
             if (ClubhouseMobileInterop.isMobileApp) {
                 ClubhouseMobileInterop.redirectToLogin();
                 return;
             }
             */
             if (theApplicationContext.isFirstUser) {
                 window.location.hash = '/first-user';
                 return;
             }
             var username = this.$route.query.username;
             if (!username && this.$store.state.user) {
                 username = this.$store.state.user.username;
             }
             if (!username && localStorage.rememberDeviceToken) {
                 username = localStorage.rememberDeviceToken.split(/\|/g)[0];
             }
             this.username = username;
             this.userId = parseInt(this.$route.query.userId || 0, 10) || null;
             this.encryptionPassword = this.$route.query.password || '';
             if (this.username && this.encryptionPassword) {
                 this.doLogin();
             } else {
                 this.processing = false;
                 this.isLoading = false;
             }
             if (window.localStorage.rememberDeviceToken) {
                 this.rememberThisDevice = true;
             }
         },
         doLogin: function() {
             var self = this;
             this.processing = true;
             console.log('doLogin');
             stallion.request({
                 url: '/clubhouse-api/auth/login-new-device-step1-get-salt',
                 method: 'POST',
                 data: {
                     username: self.username,
                     userId: this.userId
                 },
                 success: function(o) {
                     var rememberToken = '';
                     if (window.localStorage.rememberDeviceToken && window.localStorage.rememberDeviceToken.indexOf(self.username + "|") === 0) {
                         rememberToken = window.localStorage.rememberDeviceToken;
                     }
                     var hashed4 = md5(self.encryptionPassword + "|" + o.salt).substr(0, 4);
                     console.log('submit pasword salt hashed');
                     stallion.request({
                         url: '/clubhouse-api/auth/login-new-device-step2-check-password-partial',
                         method: 'POST',
                         data: {
                             username: self.username,
                             password4chars: hashed4,
                             rememberDeviceToken: rememberToken
                         },
                         success: function(o2) {
                             console.log('salt passed, next step is', o2.nextStep);
                             self.isLoading = false;
                             if (o2.nextStep === 'validatePrivateKey') {
                                 self.doLoginWithPrivateKey(o2);
                             } else if (o2.nextStep === 'googleAuth') {
                                 self.showGoogleSecondFactorRequest(o2);
                             } else {
                                 self.showEmailSecondFactorRequest(o2);
                             }
                         },
                         error: self.showError
                     });
                 }
             });
         },
         showError: function(response) {
             var self = this;
             console.error(response, response.message);
             if (response.message) {
                 stallion.showError(response.message);
             } else {
                 stallion.showError('Error processing request');
             }
             self.processing = false;
             self.processingSecondFactor = false;
             self.isLoading = false;
         },
         showGoogleSecondFactorRequest: function(response) {
             var self = this;
             self.screen = 'google-second-factor';
         },
         showEmailSecondFactorRequest: function(response) {
             var self = this;
             self.secondFactorKey = response.key;
             self.screen = 'email-second-factor';
         },
         submitEmailSecondFactor: function() {
             var self = this;
             self.processingSecondFactor = true;
             stallion.request({
                 url : '/clubhouse-api/auth/login-new-device-step3-verify-token',                 
                 method: 'POST',
                 data: {
                     username: self.username,
                     tokenKey: self.secondFactorKey,
                     tokenCode: self.secondFactorCode
                 },
                 success: function(pkInfo) {
                     console.log('got encrypted token from server ', pkInfo.tokenKey, pkInfo.tokenEncryptedHex);
                     self.doLoginWithPrivateKey(pkInfo);
                 },
                 error: self.showError
             });
         },
         doLoginWithPrivateKey: function(pkInfo) {
             var tokenKey = pkInfo.tokenKey;
             var tokenEncryptedHex = pkInfo.tokenEncryptedHex;
             var privateKeyJwkEncryptedHex = pkInfo.privateKeyJwkEncryptedHex;
             var privateKeyVectorHex = pkInfo.privateKeyVectorHex;
             
             var self = this;
             clubhouseImportPrivateKey(self.encryptionPassword, privateKeyJwkEncryptedHex, privateKeyVectorHex).then(function(privateKey) {
                 clubhouseDecryptToken(
                     privateKey,
                     tokenEncryptedHex
                 ).then(function(decryptedToken) {
                     stallion.request({
                         url: '/clubhouse-api/auth/private-key-login-step2',
                         method: 'POST',
                         data: {
                             username: self.username,
                             tokenKey: tokenKey,
                             token: decryptedToken,
                             generateElectronAuthCookie: isInElectron
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

                                 if (self.rememberThisDevice) {
                                     window.localStorage.rememberDeviceToken = o.rememberDeviceToken;
                                 }
                                 ClubhouseMobileInterop.onLoggedIn(o.user.id, o.user.username, o.electronAuthCookie);
                                 console.log('login.vue nagivate home');
                                 window.location.hash = '/';
                                 
                             }).catch(function(err) {
                                 self.processing = false;
                                 console.error('Private key import error: ', err);
                                 self.showError({message: 'Error importing private key. Please check your passphrase'});
                             });
                             
                         },
                         error: self.showError
                     });
                 }).catch(function(err) {
                     console.error('error decrypting token ', err);
                     self.showError({message: 'Error with password.'});
                 });
             }).catch(function(err) {
                 console.error('error importing private key ', err);
                 self.showError({message: 'Error with password.'});
             });
         },
         error: function(o) {
             self.processing = false;
             self.isLoading = false;
             stallion.showError(o.message || o + '');
         }
     }
 }
</script>
