<style lang="scss">
</style>

<template>
    <div class="clubhouse-login-vue one-column-screen">
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
                <label for="password-field">Password</label>
                <input id="password-field" type="password" class="form-group" v-model="password" required="true" >
            </div>
            <div class="form-group">
                <label for="encryption-passphrase-field">Encryption Passphrase</label>
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
             this.encryptionPassword = this.$route.query.passphrase;
             if (this.password && this.username && this.encryptionPassword) {
                 this.doLogin();
             } else {
                 this.processing = false;
                 this.isLoading = false;
             }
         },
         encryptionLoginOnly: function() {
             var self = this;
             ClubhouseImportPublicAndPrivateKey(
                 self.encryptionPassword
             ).then(function() {
                 sessionStorage['private-key-passphrase-' + self.$store.state.user.id] = self.encryptionPassword;
                 ClubhouseVueApp.stateManager.start();
                 ClubhouseVueApp.stateManager.loadContext(function(ctx) {
                     Vue.nextTick(function() {
                         window.location.hash = '/';
                     });
                 });                 
             }).catch(function(err) {
                 self.processing = false;
                 self.isLoading = false;
                 console.log(err);
                 stallion.showError('Error importing private key. Please check your passphrase');
              });
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
                     //o.encryptedPrivateKeyInitializationVectorHex;
                     //o.publicKeyHex;
                     //o.encryptedPrivateKeyHex;
                     ClubhouseImportPublicAndPrivateKey(
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
                                 window.location.hash = '/';
                             });
                         });
                     }).catch(function(err) {
                         self.processing = false;
                         stallion.showError('Error importing private key. Please check your passphrase');
                     });
                 },
                 error: function(o) {
                     self.processing = false;
                     self.isLoading = false;
                     stallion.showError(o);
                 }
             });
         }
     }
 }
</script>
