<style lang="scss">
 .open-direct-message-vue {
     
 }
</style>

<template>
    <div class="open-direct-message-vue">
        <h1>Direct Messages</h1>
        <div class="member-row" v-for="member in members" v-if="user.id!=member.id">
            <div><a @click="openDirectMessage(member)" href="javascript:;">{{ member.displayName }} {{ member.username }}</a></div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             members: [],
             user: this.$store.state.user
         };
     },
     created: function() {
         this.fetchData();
     },
     watch: {
         '$route': 'fetchData',
     },     
     methods: {
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/users/all-users',
                 success: function(o) {
                     self.members = o.users;
                 }
             });
         },
         openDirectMessage: function(member) {
             stallion.request({
                 method: 'POST',
                 data: {
                     userIds: [member.id],
                 },
                 url: '/clubhouse-api/messaging/open-direct-message',
                 success: function(o) {
                     window.location.hash = '/channel/' + o.channel.id;
                 }
             });
         }
     }
 }
</script>
