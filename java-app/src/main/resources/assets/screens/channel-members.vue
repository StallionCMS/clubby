<style lang="scss">
 .channel-members-vue {
     .one-column-screen {
         max-width: 800px;
         margin-left: 20px;
         margin-top: 2em;
     }
     .member-row {
         padding-top: 1em;
         padding-bottom: 1em;
         border-bottom: 1px solid #DDD;
         .member-name {
             display: inline-block;
             
         }
         a {
             display: inline-block;
             float: right;
         }
     }
 }
</style>

<template>
    <div class="channel-members-vue">
        <div class="one-column-screen">
            <div v-if="channel">
                <h3>Channel Members for {{ channel.name }}</h3>
            </div>
            <div class="member-row" v-for="member in members">
                <div class="member-name">
                    {{ member.displayName }}
                </div>
                <span v-if="channel.owner">
                    <a v-if="member.channelMemberId"  class="btn btn-default" @click="removeMemberFromChannel(member)">Remove from Channel</a>
                    <a v-else class="btn btn-default" @click="addMemberToChannel(member)">Add to Channel</a>
                </span>
            </div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             channelId: null,
             members: [],
             channel: null
         }
     },
     created: function() {
         this.onRoute();
     },
     watch: {
         '$route': 'onRoute'
     },
     methods: {
         addMemberToChannel: function(member) {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/messaging/add-channel-member',
                 method: 'POST',
                 data: {
                     userId: member.id,
                     channelId: self.channelId
                 },
                 success: function(o) {
                     member.channelMemberId = o.channelMemberId;
                 }
             });
             
         },
         removeMemberFromChannel: function(member) {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/messaging/remove-channel-member',
                 method: 'POST',
                 data: {
                     userId: member.id,
                     channelId: self.channelId
                 },
                 success: function(o) {
                     member.channelMemberId = null;
                 }
             });
         },
         onRoute: function() {
             this.channelId = this.$route.params.channelId;
             this.fetchData();
         },
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/messaging/get-channel-members/' + self.channelId,
                 success: function(o) {
                     self.members = o.members;
                     self.channel = o.channel;
                 }
             });
         }
     }
 };
</script>
