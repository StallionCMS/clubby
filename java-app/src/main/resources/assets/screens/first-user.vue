
<style lang="scss">
 .first-user-vue {
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
    <div class="first-user-vue one-column-screen">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-if="!isLoading">
            <form @submit.prevent="createAccount">
                <h1>Create Admin Account for {{ $store.state.site.name }}</h1>
                <fieldset>
                    <div class="form-group">
                        <label>Secret Key from conf/stallion.toml</label>
                        <div class="form-explanation">Look in conf/stallion.toml and find the value for 'healthCheckSecret' and enter it here.</div>
                        <input type="text" class="form-control" v-model="secretKey" required="true" autofocus="autofocus">
                    </div>
                </fieldset>
                <fieldset>
                    <h4>Profile Information</h4>
                    <div class="form-group">
                        <label>First name</label>
                        <input type="text" class="form-control" v-model="user.givenName" required="true" >
                    </div>
                    <div class="form-group">
                        <label>Last name</label>
                        <input type="text" class="form-control" v-model="user.familyName" required="true">
                    </div>
                    <div class="form-group">
                        <label>Username</label>
                        <div class="form-explanation">Letters, numbers, and underscores only</div>
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
                        <input type="email" class="form-control" v-model="user.email" required="true">
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
                    <button v-disable-for-processing="processing" type="submit" class="btn btn-primary btn-lg">Create Account</button>
                </div>
            </form>
            
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             secretKey: '',
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
             if (!theApplicationContext.isFirstUser) {
                 window.location.hash = '/login';
                 return;
             }
             self.isLoading = false;
         },
         createAccount: function() {
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
                 
                 self.httpCreateAccount(result.publicKey, result.privateKey);

             }).catch(function(err) {
                 console.error(err);
                 self.processing = false;
             });
                        
         },
         httpCreateAccount: function(publicKey, privateKey) {
             var self = this;
             var salt = generateUUID().replace(/\-/g, '');
             var password4chars = md5(self.user.password + '|' + salt).substr(0, 4);
                          
             
             stallion.request({
                 url: '/clubhouse-api/users/create-first-user',
                 method: 'POST',
                 form: $(self.$el).find('form'),
                 data: {
                     secretKey: self.secretKey,
                     username: self.user.username,
                     givenName: self.user.givenName,
                     email: self.user.email,
                     familyName: self.user.familyName,
                     password4chars: password4chars,
                     passwordSalt: salt,
                     aboutMe: self.profile.aboutMe,
                     contactInfo: self.profile.contactInfo,
                     webSite: self.profile.webSite,
                     publicKeyJwkJson: self.profile.publicKeyJwkJson,
                     privateKeyVectorHex: self.profile.privateKeyVectorHex,
                     privateKeyJwkEncryptedHex: self.profile.privateKeyJwkEncryptedHex,
                     rememberThisDevice: self.rememberThisDevice
                 },
                 success: function(o) {
                     stallion.showSuccess("New user created!");
                     theApplicationContext.isFirstUser = false;
                     sessionStorage['private-key-passphrase-' + o.user.id] = self.user.password;
                     self.$store.commit('privateKey', privateKey);
                     self.$store.commit('defaultChannelIdChange', o.defaultChannelId);
                     o.user.role = 'ADMIN';
                     stallionClubhouseApp.store.commit('login', {
                         user: o.user,
                         userProfile: o.userProfile
                     });
                     ClubhouseVueApp.stateManager.start();
                     ClubhouseVueApp.stateManager.loadContext(function(ctx) {
                         Vue.nextTick(function() {
                             window.location.hash = '/clubhouse-settings';
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
            
         }
     }
 };
</script>
