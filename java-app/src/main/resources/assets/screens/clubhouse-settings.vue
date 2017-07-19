
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
        <div v-if="!isLoading">
            <h1>Clubhouse Settings</h1>
            <!-- Nav tabs -->
            <ul class="nav nav-tabs" role="tablist">
                <li role="presentation" class="active"><a href="#settings" aria-controls="settings" role="tab" data-toggle="tab">Settings</a></li>
                <li role="presentation" ><a href="#users" aria-controls="home" role="tab" data-toggle="tab">Users</a></li>
            </ul>
            <!-- Tab panes -->
            <div class="tab-content">
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
                                <image-upload-target message="Drag an image here to upload a new icon. Or click to open a file picker." :is-public="true" @uploaded="onIconUpload"></image-upload-target>
                            </div>
                        </div>
                        <div style="clear:both"></div>
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
             userAndProfiles: []
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
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/admin/clubhouse-settings',
                 success: function(o) {
                     self.isLoading = false;
                     console.log(o.userAndProfiles);
                     self.settings = o.settings;
                     self.userAndProfiles = o.userAndProfiles;
                 }
             });
         }
     }
 };
</script>
