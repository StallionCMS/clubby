
<style lang="scss">
 .clubhouse-settings-vue {

 }
</style>


<template>
    <div class="clubhouse-settings-vue one-column-screen">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-if="!isLoading">
            <h1>Clubhouse Settings</h1>
            <h3>Users <a href="#/invite-user" style="float:right;" class="btn btn-lg btn-primary">Invite User</a></h3>
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
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             isLoading: true,
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
         activate: function(up) {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/users/activate',
                 method: 'POST',
                 data: {
                     userId: up.user.id
                 },
                 success: function(o) {
                     up.user.approved = true;
                 }
             });
         },
         deactivate: function(up) {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/users/deactivate',
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
                 url: '/clubhouse-api/users/clubhouse-settings',
                 success: function(o) {
                     self.isLoading = false;
                     console.log(o.userAndProfiles);
                     self.userAndProfiles = o.userAndProfiles;
                 }
             });
         }
     }
 };
</script>
