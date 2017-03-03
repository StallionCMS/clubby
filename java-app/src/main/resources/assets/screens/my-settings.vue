<style lang="scss">
 .my-settings-vue {
     .one-column-screen {
         max-width: 800px;
         margin-left: 20px;
         margin-top: 2em;
     }
     .settings-form {
         
     }
 }
</style>

<template>
    <div class="my-settings-vue">
        <div class="one-column-screen">
            <h3>My Settings</h3>
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
                    <label>My Avatar URL</label>
                    <input class="form-control" type="url" v-model="userProfile.avatarUrl">
                </div>
                <div class="form-group">
                    <label>Web site</label>
                    <input class="form-control" type="url" v-model="userProfile.webSite">
                </div>
                <div class="checkbox">
                    <label><input type="checkbox" v-model="userProfile.emailMeWhenMentioned">
                        Email me messages where I am mentioned?</label>
                </div>
                <div class="checkbox">
                    <label><input type="checkbox" v-model="userProfile.notifyWhenMentioned">
                        Show notifications when I am mentioned?</label>
                </div>
                <div class="form-group p">
                    <button type="submit" class="btn btn-primary btn-xl">Save Changes</button>
                </div>
            </form>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             user: JSON.parse(JSON.stringify(this.$store.state.user)),
             userProfile: JSON.parse(JSON.stringify(this.$store.state.userProfile))
         };
     },
     methods: {
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
                 }
             });
         }
     }
 };
</script>
