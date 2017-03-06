
<style lang="scss">
 .accept-invite-vue {

 }
</style>


<template>
    <div class="accept-invite-vue one-column-screen">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-if="!isLoading">
            <form @submit.prevent="doAccept">
                <h1>Accept Invite</h1>
                <div class="form-group">
                    <label>First name</label>
                    <input type="text" class="form-control" v-model="user.givenName" required="true" autofocus="autofocus">
                </div>
                <div class="form-group">
                    <label>Last name</label>
                    <input type="text" class="form-control" v-model="user.familyName" required="true">
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" disabled="true" class="form-control" v-model="user.email" required="true">
                </div>                
                <div class="form-group">
                    <label>Username</label>
                    <input type="text" class="form-control" v-model="user.username" required="true">
                </div>
                <div class="form-group">
                    <label>About me</label>
                    <input type="text" class="form-control" v-model="profile.aboutMe" >
                </div>
                <div class="form-group">
                    <label>Website</label>
                    <input type="text" class="form-control" v-model="profile.webSite" >
                </div>

                <div class="form-group">
                    <label>Password</label>
                    <input type="password" class="form-control" v-model="user.password" required="true">
                </div>
                <div class="form-group">
                    <label>Confirm Password</label>
                    <input type="password" class="form-control" v-model="user.passwordConfirm" required="true">
                </div>
                <div class="form-group">
                    <label>Encryption Passphrase</label>
                    <input type="password" class="form-control" v-model="profile.passphrase" required="true">
                </div>
                <div class="form-group">
                    <label>Confirm Encryption Passphrase</label>
                    <input type="password" class="form-control" v-model="profile.passphraseConfirm" required="true">
                </div>                
               
                
                <div class="p">
                    <button type="submit" class="btn btn-primary btn-xl">Accept Invite</button>
                </div>
            </form>
            
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             user: {
                 email: '',
                 username: '',
                 givenName: '',
                 familyName: '',
                 password: '',
                 passwordConfirm: ''
             },
             profile: {
                 aboutMe: '',
                 webSite: '',
                 publicKeyHex: '',
                 encryptedPrivateKeyHex: '',
                 encryptedPrivateKeyInitializationVectorHex: '',
                 passphrase: '',
                 passphraseConfirm: ''
             },
             isLoading: true,
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
             var self = this;
             this.fetchData();
         },
         doAccept: function() {
             var self = this;
             if (self.user.password.length < 6) {
                 stallion.showError('Password must be at least six characters');
                 return;                 
             }
             if (self.user.password != self.user.passwordConfirm) {
                 stallion.showError('Passwords must match.');
                 return;
             }
             if (self.profile.passphrase.length < 6) {
                 stallion.showError('Encryption passphrase must be at least six characters');
                 return;
             }
             if (self.profile.passphrase != self.profile.passphraseConfirm) {
                 stallion.showError('Encryption passphrases  must match.');
                 return;
             }
             new KeyGenerator().generate(self.user.username, self.profile.passphrase, function(result) {
                 self.profile.encryptedPrivateKeyHex = result.privateKeyEncryptedHex;
                 self.profile.encryptedPrivateKeyInitializationVectorHex = result.privateKeyEncryptionVectorHex;
                 self.profile.publicKeyHex = result.publicKeyHex;
                 self.saveAcceptance(result.publicKey, result.privateKey);
             });             
         },
         saveAcceptance: function(publicKey, privateKey) {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/users/accept-invite',
                 method: 'POST',
                 form: $(self.$el).find('form'),
                 data: {
                     userId: self.$route.query.userId,
                     username: self.user.username,
                     givenName: self.user.givenName,
                     familyName: self.user.familyName,
                     password: self.user.password,
                     aboutMe: self.profile.aboutMe,
                     webSite: self.profile.webSite,
                     publicKeyHex: self.profile.publicKeyHex,
                     encryptedPrivateKeyHex: self.profile.encryptedPrivateKeyHex,
                     encryptedPrivateKeyInitializationVectorHex: self.profile.encryptedPrivateKeyInitializationVectorHex,
                     token: self.$route.query.token
                     
                 },
                 success: function(o) {
                     stallion.showSuccess("Invitation accepted!");
                     sessionStorage.privateKeyPassphrase = self.profile.passphrase;
                     self.$store.commit('privateKey', privateKey);
                     stallionClubhouseApp.store.commit('login', {
                         user: o.user,
                         userProfile: o.userProfile
                     });
                     ClubhouseVueApp.stateManager.start();
                     ClubhouseVueApp.stateManager.loadContext();
                     window.location.hash = '/channel/10500';
                 }
             });             
         },
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/users/accept-invite-context?token=' + self.$route.query.token + '&userId=' + self.$route.query.userId,
                 success: function(o) {
                     self.isLoading = false;
                     self.user.email = o.email;
                     self.user.username = o.username,
                     self.user.givenName = o.givenName;
                     self.user.familyName = o.familyName;
                 }
             });
         }
     }
 };
</script>
