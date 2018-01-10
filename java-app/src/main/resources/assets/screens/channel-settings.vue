<style lang="scss">
 .channel-settings-vue {
     .one-column-screen {
     }
 }
</style>

<template>
    <div class="channel-settings-vue">
        <div class="one-column-screen">
            <div v-if="channelId && channel" class="breadcrumbs-row"><a class="breadcrumb-link" :href="channel.channelType === 'FORUM'? '#/forum/' + channel.id : '#/channel/' + channel.id"><i v-if="channel.encrypted" class="material-icons">security</i> <i v-if="!channel.encrypted && channel.inviteOnly" class="material-icons">lock</i> {{ channel.name }}</a> &#8250;
                    Settings
            </div>            
            <h3 v-if="channelId">Channel Settings</h3>
            <h3 v-else>New Channel</h3>
            <form v-if="channel" @submit.prevent="saveChanges" class="form">
                <div class="form-group">
                    <label>Channel Name</label>
                    <input type="text" class="form-control" v-model="channel.name" autofocus="autofocus" required="true">
                </div>
                <div class="p">
                    <div><b>Type</b></div>
                    <div class="radio">
                        <label><input required="true" name="channelType" type="radio" v-model="channel.channelType" value="FORUM"> Forum</label>
                    </div>
                    <div class="radio">
                        <label><input required="true" name="channelType" type="radio" v-model="channel.channelType" value="CHANNEL"> Group Chat</label>
                    </div>
                </div>
                <div>
                    <b>Options</b>
                </div>
                <div class="checkbox">
                    <label><input type="checkbox" v-model="channel.inviteOnly"> Invite only?</label>
                </div>
                <div class="checkbox">
                    <label><input type="checkbox" v-model="channel.encrypted"> End-to-End Encrypted? (maximum 50 people in the channel)</label>
                </div>
                <div class="checkbox" v-if="!channel.encrypted">
                    <label><input type="checkbox" v-model="channel.newUsersSeeOldMessages"> New channel members can see messages from before they joined?</label>
                </div>
                <div class="checkbox">
                    <label><input type="checkbox" v-model="channel.defaultForNewUsers"> New users automatically added to this channel?</label>
                </div>
                <div class="form-group">
                    <label>Purge after days? (Zero for never purging)</label>
                    <input type="number" class="form-control" style="max-width: 100px;" v-model="channel.purgeAfterDays"> 
                </div>
                <div class="form-group">
                    <button class="btn btn-primary btn-xl" type="submit">Save Changes</button>
                    <button v-if="channel.id && !channel.deleted" type="button" @click="archiveThisChannel" style="float:right;" class="btn btn-danger">Archive this channel</button>
                    <button v-if="channel.id && channel.deleted" type="button" @click="unarchiveThisChannel" style="float:right;" class="btn btn-success">Unarchive this channel</button>
                </div>
            </form>
            
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             channelId: null,
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
             if (!this.channelId) {
                 this.channel = {
                     inviteOnly: false,
                     encrypted: false,
                     purgeAfterDays: 0,
                     channelType: ''
                 }
             } else {
                 this.fetchData();
             }
         },
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/channels/channel-details/' + self.channelId,
                 success: function(o) {
                     self.channel = o.channel;
                 }
             });
         },
         archiveThisChannel: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/channels/archive-channel/' + self.channelId,
                 method: 'POST',
                 data: self.channel,
                 success: function() {
                     self.channel.deleted = 1;
                     stallion.showSuccess("Channel archived.");
                     ClubhouseVueApp.stateManager.loadContext();
                 }
             });
         },
         unarchiveThisChannel: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/channels/unarchive-channel/' + self.channelId,
                 method: 'POST',
                 data: self.channel,
                 success: function() {
                     self.channel.deleted = 0;
                     stallion.showSuccess("Channel restored.");
                     ClubhouseVueApp.stateManager.loadContext();
                 }
             });
         },         
         saveChanges: function() {
             if (!this.channelId) {
                 this.createChannel();
             } else {
                 this.updateChannel();
             }
         },
         createChannel: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/channels/create-channel',
                 method: 'POST',
                 data: self.channel,
                 success: function(o) {
                     self.$store.commit('channelAdded', o.channel);
                     stallion.showSuccess('Channel added!');
                     window.location.hash = '#/channel/' + o.channel.id;
                 }
             });
         },
         updateChannel: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/channels/update-channel/' + self.channelId,
                 method: 'POST',
                 data: self.channel,
                 success: function(o) {
                     stallion.showSuccess("Channel changes saved!");
                     self.$store.commit('channelUpdated', o.channel);
                 }
             });
         }
     }
 }
</script>
