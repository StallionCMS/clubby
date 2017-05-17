
<style lang="scss">
 .profile-public-vue {
     .bigger-image {
         max-width: 80px;
         max-height: 80px;
         margin-right: 10px;
     }
     .about-me {
         display: inline-block;
     }
     .btn-direct-message {

     }
 }
</style>


<template>
    <div class="profile-public-vue">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-if="!isLoading" class="one-column-screen">
            <h3>Profile for {{ user.displayName }}</h3>
            <div class="p" v-if="user.id != $store.state.user.id">
                <button class="btn btn-default btn-lg btn-direct-message" @click="openDirectMessage">Open Direct Message</button>
            </div>
            <div>
                <img class="bigger-image" :src="user.avatarUrl">
                <div class="about-me">{{ user.aboutMe }}</div>
            </div>
            <div class="p" v-if="user.contactInfo">
                {{ user.contactInfo }}
            </div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             isLoading: true,
             userId: 0,
             user: {}
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
             self.userId = this.$route.params.userId;
             this.fetchData();
         },
         openDirectMessage: function() {
             var self = this;
             stallion.request({
                 method: 'POST',
                 data: {
                     userIds: [self.user.id],
                 },
                 url: '/clubhouse-api/messaging/open-direct-message',
                 success: function(o) {
                     window.location.hash = '/channel/' + o.channel.id;
                 }
             });
         },         
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/users/public-profile/' + self.userId,
                 success: function(o) {
                     self.user = o.user;
                     self.isLoading = false;
                 }
             });
         }
     }
 };
</script>
