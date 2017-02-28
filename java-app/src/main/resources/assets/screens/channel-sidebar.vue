<style lang="scss">
 .channel-sidebar-vue {
     background-color: #252a4e;
     width: 220px;
     padding: 0px;
     color: #aaadc3;
     height: 101vh;
     margin-top: -1px;
     border-top: 1px solid #252a4e;
     float: left;
     position: fixed;
     h3 {
         font-size: 12px;
         text-transform: uppercase;
         padding-left: 20px;
         margin-bottom: 0px;
     }
     h2 {
         font-size: 20px;
         color: #F8F8F8;
         padding-left: 20px;
         margin-top: 1em;
         margin-bottom: 0px;
     }
     a {
         color: #aaadc3;
         display: block;
     }
     a.channel-link {
         padding-left: 22px;
         padding-top: 4px;
         padding-bottom: 4px;
     }
     a.active {
         background-color: #5d4877;
     }
     .explain {
         font-style: italic;
         padding-left: 20px;
     }
     .loggedin-name {
         bottom: 10px;
         padding-left: 10px;
         padding-right: 10px;
         padding-top: 4px;
         position: absolute;
         border-top: 1px solid #aaadc3;
         width: 220px;
         text-align: center;
         a {
             display: inline-block;
             padding: 0px;
         }
     }
 }
</style>

<template>
    <div class="channel-sidebar-vue">
        <h2>My Clubhouse</h2>
        <h3>Forums</h3>
        <div v-if="forumChannels.length === 0" class="explain">No forums</div>
        <a :class="{'channel-link': true, active: channel.id === activeChannelId}" v-for="channel in forumChannels" :href="'#/channel/' + channel.id"># {{ channel.name }}</a>                
        <h3>Channels</h3>
        <a :class="{'channel-link': true, active: channel.id === activeChannelId}" v-for="channel in standardChannels" :href="'#/channel/' + channel.id"># {{ channel.name }}</a>        
        <h3><a href="#/open-direct-message">Direct Messages</a></h3>
        <div v-if="directMessageChannels.length === 0">
            <div class="explain">No message history</div>    
        </div>
        <a :class="{'channel-link': true, active: channel.id === activeChannelId}" v-for="channel in directMessageChannels" :href="'#/channel/' + channel.id"># {{ channel.name }}</a>        
        <div v-if="user && user.id" class="loggedin-name">
            <div>{{ user.displayName || user.username }}</div>
            <div><a href="#/my-settings">Settings</a> | <a href="/st-users/logoff">Log off</a></div>
        </div>
        
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             standardChannels: [],
             directMessageChannels: [],
             forumChannels: [],
             user: this.$store.state.user
         }
     },
     created: function() {
         this.fetchData();
     },
     computed: {
         activeChannelId: function() {
             return stallionClubhouseApp.store.state.activeChannelId;
         },
         channels: function() {
             return [
                 {
                     name: 'general',
                     id: 10500
                 },
                 {
                     name: 'humor',
                     id: 10501
                 },
                 {
                     name: 'plotting',
                     id: 10505
                 },
                                                   
             ];
         }
     },
     watch: {

     },     
     methods: {
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/messaging/my-channels',
                 success: function(o) {
                     self.standardChannels = o.standardChannels;
                     self.forumChannels = o.forumChannels;
                     self.directMessageChannels = o.directMessageChannels;
                 }
             });
         }
     }
 }
</script>
