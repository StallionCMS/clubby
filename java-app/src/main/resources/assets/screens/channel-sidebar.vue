<style lang="scss">
 $fontColor: rgba(255, 255, 255, .7);
 
 .channel-sidebar-vue {
     background-color: #4b4c58;
     width: 220px;
     padding: 0px;
     color: #DDD;
     height: 101vh;
     margin-top: -1px;
     border-top: 1px solid #252a4e;
     float: left;
     position: fixed;
     .sidebar-items {
         padding-top: 1em;
         max-height: 70vh;
         overflow: auto;

     }
     h3 {
         font-size: 14px;
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
     h2.sidebar-title {
         margin-top: 10px;
         position: relative;
     }
     .sidebar-settings-link {
         position: absolute;
         right: 7px;
         color: rgba(255, 255, 255, .5);
         .material-icons {
             font-size: 22px;
             
         }
     }
     .sidebar-settings-link:hover {
         color: rgba(255, 255, 255, .9);
     }
     a {
         color: $fontColor;
         display: block;
     }
     a.channel-link {
         padding-left: 22px;
         padding-top: 2px;
         padding-bottom: 2px;
     }
     a.channel-link:hover {
         text-decoration: none;
         background-color: rgba(100, 100, 100, .6);
     }
     a.active {
         background-color: rgba(100, 100, 100, .4);
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
         border-top: 1px solid rgba(230, 230, 230, .1);
         width: 220px;
         text-align: center;
         a {
             display: inline-block;
             padding: 0px;
         }
     }
     .unread-mentions-count {

         background-color: #bd1515;
         display: inline-block;
         border-radius: 9px;         
         width: 18px;
         height: 18px;
         padding: 1px 1px 1px 5px;
         font-size: 12px;
     }
     .channel-unread {
         color: #FFF;
     }
     a.channel-link.direct-message-link {
         padding-left: 40px;

     }
     .dm-user-count {
         margin-left: -18px;
         font-size: 11px;
         background-color: #CCC;
         color: #222;
         border-radius: 2px;
         display: inline-block;
         width: 12px;
         height: 12px;
         opacity: .5;
         padding: 7px 2px 4px 3px;
         line-height: 0px;
     }
     .username-span {
         display: inline-block;
         padding-right: 5px;
     }
     .username-span:first-child {
         margin-left: -18px;
     }
     .user-awake-bubble {
         color: #494;
         display: inline-block;
         margin-right: 2px;
     }
     .user-offline-bubble {
         display: inline-block;
         margin-right: 2px;
     }
     .add-channel-link {
         display: inline-block;
         vertical-align: -25%;         
         .material-icons {
             font-size: 16px;

         }
     }
 }
</style>

<template>
    <div  class="channel-sidebar-vue">
        <div v-if="$store.state.user && $store.state.user.id">
            <h2 class="sidebar-title">My Clubhouse <a class="sidebar-settings-link" style="display: inline-block;" href="#/clubhouse-settings" v-if="$store.state.user.role === 'ADMIN'"><i class="material-icons">settings</i></a></h2>
            <div class="sidebar-items">
                <h3 v-if="favorites.length">Favorites</h3>
                <sidebar-link-item :channel="channel" :active-channel-id="activeChannelId" v-for="channel in favorites"></sidebar-link-item>
                <h3>Forums</h3>
                <div v-if="forumChannels.length === 0" class="explain">No forums</div>
                <sidebar-link-item :channel="channel" :active-channel-id="activeChannelId" v-for="channel in forumChannels"></sidebar-link-item>
                <h3><a style="display: inline-block" href="#/my-channels">Group Chat</a> <a class="add-channel-link" href="#/channel-settings"><i class="material-icons">add_circle_outline</i></a></h3>
                <sidebar-link-item :channel="channel" :active-channel-id="activeChannelId" v-for="channel in standardChannels"></sidebar-link-item>
                <h3><a href="#/open-direct-message">Direct Chat</a></h3>
                <div v-if="directMessageChannels.length === 0">
                    <div class="explain">No message history</div>    
                </div>
                <sidebar-link-item :channel="channel" :active-channel-id="activeChannelId" v-for="channel in directMessageChannels"></sidebar-link-item>
            </div>
            <div v-if="user && user.id" class="loggedin-name">
                <div>{{ user.displayName || user.username }}</div>
                <div><a href="#/my-settings">Settings</a> <span style="color: rgba(230, 230, 230, .6);">&#8226;</span> <a href="/st-users/logoff">Log off</a></div>
            </div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
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
         forumChannels: function() {
             return this.$store.state.forumChannels.filter(function(channel) {
                 return !channel.favorite && !channel.deleted
             });
         },
         directMessageChannels: function() {
             return this.$store.state.directMessageChannels.filter(function(channel) {
                 return !channel.favorite && !channel.deleted
             });
         },
         standardChannels: function() {
             return this.$store.state.standardChannels.filter(function(channel) {
                 return !channel.favorite && !channel.deleted
             });
         },
         favorites: function() {
             var favs = [];
             var all = []
                 .concat(this.$store.state.standardChannels)
                 .concat(this.$store.state.directMessageChannels)
                 .concat(this.$store.state.forumChannels);
             all.forEach(function(channel) {
                 if (channel.favorite) {
                     favs.push(channel);
                 }
             });
             return favs;
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
             
         }
     }
 }
</script>
