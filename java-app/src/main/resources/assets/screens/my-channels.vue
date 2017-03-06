<style lang="scss">
 .my-channels-vue {
     .channel-row {
         padding: 8px;
     }
 }
</style>

<template>
    <div class="my-channels-vue">
        <div class="channel-row" v-for="channel in channels">
            <div class="channel-name">{{ channel.name }}</div>
            <div class="channel-action"><a href="javascript:;" v-if="!channel.channelMemberId" class="" @click="joinChannel(channel)">Join</a><a @click="leaveChannel(channel)" href="javascript:;"  v-if="channel.channelMemberId">Leave</a></div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             channels: []
         }
     },
     created: function() {
         this.onRoute();
     },
     watch: {
         '$route': 'onRoute'
     },
     methods: {
         joinChannel: function(channel) {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/messaging/add-channel-member',
                 method: 'POST',
                 data: {
                     userId: self.$store.state.user.id,
                     channelId: channel.id
                 },
                 success: function(o) {
                     channel.channelMemberId = o.channelMemberId;
                 }
             });
             
         },
         leaveChannel: function(channel) {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/messaging/remove-channel-member',
                 method: 'POST',
                 data: {
                     userId: self.$store.state.user.id,
                     channelId: channel.id
                 },
                 success: function(o) {
                     channel.channelMemberId = null;
                 }
             });

         },
         onRoute: function() {
             this.fetchData();
         },
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/messaging/my-channels',
                 method: 'GET',
                 success: function(o) {
                     self.channels = o.channels;
                 }
             });
         }
     }
 };
</script>
