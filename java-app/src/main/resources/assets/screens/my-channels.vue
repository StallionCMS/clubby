<style lang="scss">
 .my-channels-vue {
     .channel-row {
         margin-bottom: 1em;
         .btn {
             width: 70px;
         }
         .type-label {
             display: inline-block;
             margin-right: 20px;
             width: 60px;
             text-align: center;
             border-radius: 4px;
             background-color: #888;
             padding: 2px;
             color: #EEE;
             font-size: 10px;
             margin-right: 20px;
         }
     }
     .channel-name {
         display: inline-block;
         width: 330px;
     }
     .channel-action {
         display: inline-block;
     }
     .channel-icon {
         color: #444;
         .material-icons {
             font-size: 16px;
             color: #444;
             vertical-align: -22%;
         }
     }
     h3 {
         .label-part {
             width: 330px;
             display: inline-block;
         }
         .btn {

         }
     }
 }
</style>

<template>
    <div class="my-channels-vue">
        <div class="one-column-screen">
            <h3> 
                <span class="label-part">Forums & Group Chats</span>
                <a class="btn btn-primary" href="#/channel-settings">New Forum or Group</a>
            </h3>
            <div class="channel-row" v-for="channel in channels">
                <div class="channel-name">
                    <span class="channel-icon" v-if="channel.channelType==='FORUM'"><i class="material-icons">forum</i></span>
                    <span class="channel-icon" v-if="channel.channelType==='CHANNEL'">#</span>
                    {{ channel.name }}
                </div>
                <div class="channel-action">
                    <span class="type-label">{{ channel.channelType }}</span>
                    <a class="btn btn-primary" href="javascript:;" v-if="!channel.channelMemberId" @click="joinChannel(channel)">Join</a>
                    <a class="btn btn-default" @click="leaveChannel(channel)" href="javascript:;"  v-if="channel.channelMemberId">Leave</a>
                    
                </div>
                
            </div>
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
                 url: '/clubhouse-api/channels/add-channel-member',
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
                 url: '/clubhouse-api/channels/remove-channel-member',
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
                 url: '/clubhouse-api/channels/my-channels',
                 method: 'GET',
                 success: function(o) {
                     self.channels = o.channels;
                 }
             });
         }
     }
 };
</script>
