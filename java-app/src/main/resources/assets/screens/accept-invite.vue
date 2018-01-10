
<style lang="scss">
 .accept-invite-vue {
     fieldset {
         border-bottom: 1px solid #bbb;
         margin-bottom: 1em;
         padding-bottom: 1em;
         h4 {
             font-weight: bold;
             font-size: 18px;
             color: #333;
             margin-bottom: 1.2em;
         }
     }
 }
</style>


<template>
    <div class="accept-invite-vue one-column-screen">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-if="!isLoading">
            <form @submit.prevent="doAccept">
                <h1>Accept Invite to {{ $store.state.site.name }}</h1>

                <fieldset>
                    <h4>Profile Information</h4>
                    <div class="form-group">
                        <label>First name</label>
                        <input type="text" class="form-control" v-model="user.givenName" required="true" autofocus="autofocus">
                    </div>
                    <div class="form-group">
                        <label>Last name</label>
                        <input type="text" class="form-control" v-model="user.familyName" required="true">
                    </div>
                    <div class="form-group">
                        <label>Username</label>
                        <input type="text" class="form-control" v-model="user.username" required="true">
                    </div>
                    <div class="form-group">
                        <label>About me</label>
                        <autogrow-text type="text" class="form-control" v-model="profile.aboutMe" placeholder="Eg., Account manager for European sales"></autogrow-text>
                    </div>
                    <div class="form-group">
                        <label>Contact Information</label>
                        <div class="form-explanation">Optionally put in your email address, Twitter handle, website, or any other contact information you wish to have in your profile.</div>
                        <autogrow-text type="text" class="form-control" v-model="profile.contactInfo" placeholder="Twitter: @myusername Email: me@email.com, etc." ></autogrow-text>
                    </div>
                </fieldset>
                <fieldset>
                    <h4>Login Information</h4>                
                    <div class="form-group">
                        <label>Email (will be private)</label>
                        <input type="email" disabled="true" class="form-control" v-model="user.email" required="true">
                    </div>                
                    <div class="form-group">
                        <label>Password</label>
                        <input type="password" class="form-control" v-model="user.password" required="true">
                    </div>
                    <div class="form-group">
                        <label>Confirm Password</label>
                        <input type="password" class="form-control" v-model="user.passwordConfirm" required="true">
                    </div>
                    <div class="alert alert-info">
                        <b>Attention!</b> You encryption passphrase cannot be recovered. Make sure that you keep a copy of it in a secure place (use either password manager like 1Password or a piece of paper stored in safe). <b>If you lose this password, you will be unable to decrypt the messages that you you have previously received &mdash; they will be irretrievably lost.</b>
                    </div>
                    <div class="checkbox">
                        <label><input type="checkbox" v-model="rememberThisDevice"> Remember this device?</label>
                    </div>
                </fieldset>
                <div class="p">
                    <button v-disable-for-processing="processing" type="submit" class="btn btn-primary btn-lg">Accept Invite</button>
                </div>
            </form>
            
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             processing: false,
             rememberThisDevice: true,
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
                 contactInfo: '',
                 publicKeyHex: '',
                 encryptedPrivateKeyHex: '',
                 encryptedPrivateKeyInitializationVectorHex: ''
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

             if (self.processing) {
                 return;
             }
             self.processing = true;

             
             clubhouseGeneratePrivateAndPublicKey(self.user.password).then(function(result) {
                 self.profile.privateKeyJwkEncryptedHex = result.privateKeyJwkEncryptedHex;
                 self.profile.privateKeyVectorHex = result.privateKeyVectorHex;
                 self.profile.publicKeyJwkJson = result.publicKeyJwkJson;
                 self.saveAcceptance(result.publicKey, result.privateKey);

             }).catch(function(err) {
                 console.error(err);
                 self.processing = false;
             });
                        
         },
         saveAcceptance: function(publicKey, privateKey) {
             var self = this;

             var salt = generateUUID().replace(/\-/g, '');
             var password4chars = md5(self.user.password + '|' + salt).substr(0, 4);
             
             stallion.request({
                 url: '/clubhouse-api/users/accept-invite',
                 method: 'POST',
                 form: $(self.$el).find('form'),
                 data: {
                     userId: self.$route.query.userId,
                     username: self.user.username,
                     givenName: self.user.givenName,
                     familyName: self.user.familyName,
                     password4chars: password4chars,
                     passwordSalt: salt,
                     aboutMe: self.profile.aboutMe,
                     contactInfo: self.profile.contactInfo,
                     webSite: self.profile.webSite,
                     rememberThisDevice: self.rememberThisDevice,
                     publicKeyJwkJson: self.profile.publicKeyJwkJson,
                     privateKeyVectorHex: self.profile.privateKeyVectorHex,
                     privateKeyJwkEncryptedHex: self.profile.privateKeyJwkEncryptedHex,
                     token: self.$route.query.token
                     
                 },
                 success: function(o) {
                     stallion.showSuccess("Invitation accepted!");
                     sessionStorage['private-key-passphrase-' + o.user.id] = self.user.password;
                     self.$store.commit('privateKey', privateKey);
                     self.$store.commit('defaultChannelIdChange', o.defaultChannelId);
                     stallionClubhouseApp.store.commit('login', {
                         user: o.user,
                         userProfile: o.userProfile
                     });
                     ClubhouseVueApp.stateManager.start();
                     ClubhouseVueApp.stateManager.loadContext(function(ctx) {
                         Vue.nextTick(function() {
                             window.location.hash = '/';
                         });
                     });

                 },
                 error: function(o) {
                     stallion.showError(o.message || 'Error processing request');
                     self.processing = false;
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
