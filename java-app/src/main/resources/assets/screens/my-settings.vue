<style lang="scss">
 .my-settings-vue {
     .icon-image {
         max-width: 64px;
         min-width: 64px;
     }
     .image-upload-target-vue, #my-image-dropzone {
         max-width: 400px;
         height: 100px; 
         display: inline-block;
         margin-bottom: 5em;
         margin-left: 20px;
     }
     .settings-form {
         
     }
 }
</style>

<template>
    <div class="my-settings-vue">
        <div class="one-column-screen">
            <h3>My Settings</h3>
<!-- Nav tabs -->
            <ul class="nav nav-tabs" role="tablist">
                <li role="presentation" class="active"><a href="#settings" aria-controls="settings" role="tab" data-toggle="tab">Settings</a></li>
                <li role="presentation" ><a href="#mobile" aria-controls="mobile" role="tab" data-toggle="tab">Mobile Apps</a></li>
            </ul>
            <div class="tab-content">
                <div role="tabpanel" class="tab-pane active" id="settings">
                    <form class="settings-form" @submit.prevent="saveSettings">
                        <div class="form-group">
                            <label>First name</label>
                            <input class="form-control" type="text" v-model="user.givenName">
                        </div>
                        <div class="form-group">
                            <label>Last name</label>
                            <input class="form-control" type="text" v-model="user.familyName">
                        </div>
                        <div class="form-group">
                            <label>Username</label>
                            <input class="form-control" type="text" v-model="user.username">
                        </div>            
                        <div class="form-group">
                            <label>About Me</label>
                            <input class="form-control" type="text" v-model="userProfile.aboutMe">
                        </div>
                        <div class="form-group">
                            <label>My Avatar</label>
                            <div>
                                <img class="icon-image" :src="userProfile.avatarUrl">
                                <image-upload-target message="Drag an image here to upload, or click on this box to open a file picker." @uploaded="onIconUpload"></image-upload-target>
                            </div>
                        </div>
                        <div class="form-group">
                            <label>Contact Info</label>
                            <input class="form-control" type="text" v-model="userProfile.contactInfo">
                        </div>
                        <div class="checkbox">
                            <label><input type="checkbox" v-model="userProfile.emailMeWhenMentioned">
                                Email me messages where I am mentioned?</label>
                        </div>
                        <div class="form-group">
                            <label>Desktop Notifications</label>
                            <select class="form-control" v-model="userProfile.desktopNotifyPreference">
                                <option value="SOUND">Sound</option>
                                <option value="SILENT">Silent</option>
                                <option value="NONE">None</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Mobile Notifications</label>
                            <select class="form-control" v-model="userProfile.mobileNotifyPreference">
                                <option value="SOUND">Sound</option>
                                <option value="VIBRATE">Vibrate</option>
                                <option value="SILENT">Silent</option>
                                <option value="NONE">None</option>
                            </select>
                        </div>                        
                       
                        <div class="form-group p">
                            <button type="submit" class="btn btn-primary btn-xl">Save Changes</button>
                        </div>
                    </form>
                </div>
                <div role="tabpanel" class="tab-pane" id="mobile">
                    <div class="p"></div>
                    <h3>Scan this code from your Folkmot app to add {{ $store.state.site.name }} to your app.</h3>
                    <div class="p"></div>
                    <div id="qrtarget"></div>
                </div>
            </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             user: JSON.parse(JSON.stringify(this.$store.state.user)),
             userProfile: JSON.parse(JSON.stringify(this.$store.state.userProfile)),
             qrCookie: null
         };
     },
     mounted: function() {
         this.fetchData();
     },
     methods: {
         onIconUpload: function(data) {
             var self = this;
             self.userProfile.avatarUrl = data.fileInfo.smallUrl;
             self.userProfile.avatarFileId = data.fileInfo.id;
         },
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/auth/new-mobile-qr-cookie',
                 method: 'POST',
                 success: function(o) {
                     self.qrCookie = o.qrCookie;
                     var state = self.$store.state;         
                     var qrcode = new QRCode("qrtarget", {
                         text: state.site.siteUrl +
                               "?username=" + encodeURIComponent(state.user.username) +
                               "&encpassphrase=" + sessionStorage['private-key-passphrase-' + state.user.id] +
                               "&cookie=" + encodeURIComponent(o.qrCookie)
                             ,
                         width: 256,
                         height: 256,
                         colorDark : "#000000",
                         colorLight : "#ffffff",
                         correctLevel : QRCode.CorrectLevel.H
                     });
                 }
             });
         },
         saveSettings: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/users/update-current-user',
                 method: 'POST',
                 form: $(self.$el).find('form'),
                 data: {
                     userProfile: self.userProfile,
                     user: self.user
                 },
                 success: function(o) {
                     self.$store.commit('updateCurrentUser', o);
                     stallion.showSuccess('User updated');
                     ClubhouseMobileInterop.updateProfile(o.user, o.userProfile);
                 }
             });
         }
     }
 };
</script>
