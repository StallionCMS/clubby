
<style lang="scss">
 .invite-user-vue {
     
 }
</style>


<template>
    <div class="invite-user-vue  one-column-screen">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-if="!isLoading">
            <form @submit.prevent="sendInvite">
                <h1>Invite User</h1>
                <div class="form-group">
                    <label>First name</label>
                    <input type="text" class="form-control" v-model="user.givenName" require="true" autofocus="autofocus">
                </div>
                <div class="form-group">
                    <label>Last name</label>
                    <input type="text" class="form-control" v-model="user.familyName" require="true">
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" class="form-control" v-model="user.email" require="true">
                </div>                
                <div class="form-group">
                    <label>Username</label>
                    <input type="text" class="form-control" v-model="user.username" require="true">
                </div>                
                <div class="p">
                    <button type="submit" class="btn btn-primary btn-xl">Send Invite</button>
                </div>
            </form>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             isLoading: false,
             user: {
                 givenName: '',
                 familyName: '',
                 email: '',
                 username: ''
             }
         };
     },
     created: function() {
         this.onRoute();
     },
     watch: {
         '$route': 'onRoute'
     },
     methods: {
         sendInvite: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/users/send-invite',
                 method: 'POST',
                 data: self.user,
                 form: $(self.$el).find('form'),
                 success: function(o) {
                     stallion.showSuccess('Invitation sent.');
                     window.location.hash = '#/clubhouse-settings';
                 }
             });
         },
         onRoute: function() {
             var self = this;
             this.fetchData();
         },
         fetchData: function() {

         }
     }
 };
</script>
