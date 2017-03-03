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
                
                <a v-if="member.channelMemberId"  class="btn btn-default">Remove from Channel</a>
                <a v-else class="btn btn-default">Add to Channel</a>                
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
