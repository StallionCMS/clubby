
<style lang="scss">
 .clubhouse-settings-vue {
     .icon-image {
         max-width: 128px;
     }
     .upload-target-wrapper {
         float: right;
         clear: right;
         width: 400px;
         height: 200px;
     }
 }
</style>


<template>
    <div class="clubhouse-settings-vue one-column-screen">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-show="!isLoading">
            <h1>Clubhouse Settings</h1>
            <!-- Nav tabs -->
            <ul class="nav nav-tabs" role="tablist">
                <li role="presentation" class="active"><a href="#settings" aria-controls="settings" role="tab" data-toggle="tab">Settings</a></li>
                <li role="presentation" ><a href="#users" aria-controls="home" role="tab" data-toggle="tab">Users</a></li>
                <li role="presentation" ><a class="license-tab" href="#license" aria-controls="home" role="tab" data-toggle="tab">License</a></li>
                <li role="presentation" ><a href="#email" aria-controls="home" role="tab" data-toggle="tab">Email</a></li>
            </ul>
            <!-- Tab panes -->
            <div class="tab-content" v-if="!isLoading">
                <div role="tabpanel" class="tab-pane active" id="settings">
                    <form @submit.prevent="saveSettings" class="p">
                        <div class="form-group">
                            <label>Site name</label>
                            <input class="form-control" v-model="settings.siteName">
                        </div>
                        <div class="form-group">
                            <label>Logo Icon</label>
                            <span><img class="icon-image" :src="settings.iconUrl"></span>
                            <div class="upload-target-wrapper">
                                <image-upload-target message="Drag an image here to upl oad a new icon. Or click to open a file picker." :is-public="true" @uploaded="onIconUpload"></image-upload-target>
                            </div>
                        </div>
                        <div style="clear:both"></div>
                        <div class="p">
                            <button class="btn btn-primary " type="submit">Save Changes</button>
                        </div>
                    </form>
                </div>
                <div role="tabpanel" class="tab-pane " id="license">
                    <form @submit.prevent="saveLicense" class="p" id="save-license-form">
                        <div v-if="!license.key">
                            <div class="alert alert-warning">
                                You have not configured a license for this Clubby instance.
                            </div>
                        </div>
                        <div v-if="license.key">
                            License installed of type {{ license.type }}. License expires at {{ formatLicenseDate(license.expiresAt) }}.
                        </div>
                        
                        <div class="form-group">
                            <label>License Key</label>
                            <input class="form-control" v-model="license.key">
                        </div>
                        <div style="clear:both"></div>
                        <div class="p">
                            <button class="btn btn-primary st-button-submit" type="submit">Save Changes</button>
                        </div>
                    </form>
                </div>
                <div role="tabpanel" class="tab-pane " id="email">
                    <form @submit.prevent="saveEmailSettings" class="p">
                        <h3>Email Server Configuration</h3>
                        <div>An email server is required for verifying login information, sending invitations, and sending message notifications.</div>
                        <div class="radio">
                            <label><input type="radio" v-model="emailType" :disabled="!license || !license.validForNotifications" name="emailType" value="clubbyhost"> Use paid subcription to Clubby. <div class="alert alert-warning" v-if="!license || !license.validForNotifications"><a href="#license">Requires a license.</a></label>
                        </div>
                        <div class="radio">
                            <label><input type="radio" v-model="emailType" name="emailType" value="custom"> Use custom email server </label>
                        </div>
                        <hr>
                        <div v-if="emailType==='custom'">
                        <div class="p">
                            <button type="button" class="btn btn-default btn-send-test-email" @click="sendTestEmail">Send Test Email to {{ $store.state.user.email }}</button>
                        </div>
                        <hr>
                            
                        <div class="form-group">
                            <label>Server Host</label>
                            <input class="form-control" v-model="emailSettings.host" placeholder="smtp.domain.com, eg.">
                        </div>
                        <div class="form-group">
                            <label>Server Port</label>
                            <input class="form-control" type="number" v-model="emailSettings.port" >
                        </div>
                        <div class="checkbox">
                            <label>
                                <input type="checkbox" v-model="emailSettings.tls"> TLS enabled?
                            </label>
                        </div>                                                  
                        <div class="form-group">
                            <label>Username</label>
                            <input class="form-control" v-model="emailSettings.username" placeholder="username">
                        </div>
                        <div class="form-group">
                            <label>Password</label>
                            <input class="form-control" v-model="emailSettings.password" placeholder="password">
                        </div>
                        <div class="form-group">
                            <label>Sender Email Address</label>
                            <input class="form-control" type="email" v-model="emailSettings.defaultFromAddress">
                        </div>
                        <div style="clear:both"></div>
                        </div>

                        <div class="p">
                            <button class="btn btn-primary" type="submit">Save Changes</button>
                        </div>
                        
                    </form>
                </div>                  
                <div role="tabpanel" class="tab-pane" id="users">
                    <a href="#/invite-user" style="float:right;" class="btn btn-lg btn-primary">Invite User</a>
                    <table class="table">
                <thead>
                    <tr>
                        <th>Username</th>
                        <th>Display Name</th>
                        <th>Email</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="up in userAndProfiles" key="up.user.id">
                        <td>{{ up.user.username }}</td>
                        <td>{{ up.user.displayName }}</td>
                        <td>{{ up.user.email }}</td>
                        <td>
                            <button @click="deactivate(up)" v-if="up.user.id !== $store.state.user.id && up.user.approved && up.user.emailVerified" class="btn btn-default btn-xs">Deactivate</button>
                            <button @click="activate(up)" v-if="up.user.id !== $store.state.user.id && !up.user.approved && up.user.emailVerified" class="btn btn-default btn-xs">Activate</button>
                            <span class="" v-if="!up.user.emailVerified">Invite sent</span>
                            <button @click="resendInvite(up.user)" class="btn btn-default btn-xs" v-if="!up.user.emailVerified">Resend Invite</button>
                        </td>
                    </tr>
                </tbody>
            </table>
                </div>
                
                <div role="tabpanel" class="tab-pane" id="messages">...</div>
                <div role="tabpanel" class="tab-pane" id="settings">...</div>
            </div>            
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             isLoading: true,
             settings: null,
             emailSettings: null,
             license: {},
             userAndProfiles: [],
             emailType: 'custom'
         };
     },
     mounted: function() {
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
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/admin/clubhouse-settings',
                 success: function(o) {
                     self.isLoading = false;
                     console.log(o.userAndProfiles);
                     self.settings = o.settings;
                     self.emailType = o.emailType;
                     self.emailSettings = o.emailSettings;
                     self.license = o.license;
                     self.userAndProfiles = o.userAndProfiles;
                     Vue.nextTick(self.postLoadSelectTab);
                 }
             });
         },
         postLoadSelectTab: function() {
             $(this.$el).find('.nav-tabs li').on('shown.bs.tab', function(e) {
                 var tabId = e.target.getAttribute("href").substr(1);;
                 history.pushState({}, "", window.location.pathname + "#/clubhouse-settings/" + tabId);
             });             
             if (this.$route.params.tab) {
                 
                 $(this.$el).find('[href="#' + this.$route.params.tab + '"]').tab('show');
             }
         },
         saveSettings: function(settings) {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/admin/save-settings',
                 method: 'POST',
                 
                 data: {
                     iconImageId: self.settings.iconImageId,
                     siteName: self.settings.siteName
                 },
                 success: function(o) {
                     stallion.showSuccess('Settings saved!');
                 }
             });
         },
         saveLicense: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/admin/save-license',
                 method: 'POST',
                 button: '#save-license-form',
                 data: {
                     key: self.license.key
                 },
                 success: function(o) {
                     stallion.showSuccess('License updated.');
                 }
             });
         },
         saveEmailSettings: function(settings) {
             var self = this;
             if (self.emailType === 'clubbyhost') {
                 stallion.request({
                     url: '/clubhouse-api/admin/use-clubby-hoster-for-email',
                     method: 'POST',
                     success: function(o) {
                         stallion.showSuccess('Settings saved!');
                     }
                 });
                 return;
             }

             stallion.request({
                 url: '/clubhouse-api/admin/save-email-settings',
                 method: 'POST',
                 data: self.emailSettings,
                 success: function(o) {
                     stallion.showSuccess('Settings saved!');
                 }
             });
         },
         resendInvite: function(user) {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/admin/resend-invite',
                 method: 'POST',
                 data: {userId: user.id},
                 success: function(o) {
                     if (o.succeeded) {
                         stallion.showSuccess('Invite resent');
                     } else {
                         stallion.showError('There was a problem resending an invite.');
                     }
                 }
             });
         },
         activate: function(up) {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/admin/activate',
                 method: 'POST',
                 data: {
                     userId: up.user.id
                 },
                 success: function(o) {
                     up.user.approved = true;
                 }
             });
         },
         onIconUpload: function(data) {
             var self = this;
             self.settings.iconUrl = data.fileInfo.url;
             self.settings.iconImageId = data.fileInfo.id;
         },
         deactivate: function(up) {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/admin/deactivate',
                 method: 'POST',
                 data: {
                     userId: up.user.id
                 },
                 success: function(o) {
                     up.user.approved = false;
                 }
             });

         },
         formatLicenseDate: function(ts) {
             return moment(ts).fromNow();
         },
         sendTestEmail: function(settings) {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/admin/send-test-email',
                 method: 'POST',
                 form: '.btn-send-test-email',
                 success: function(o) {
                     stallion.showSuccess('Email sent!');
                 }
             });
         }         
         
     }
 };
</script>
