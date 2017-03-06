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
         padding-bottom: 0px;
     }
     a.channel-link:hover {
         text-decoration: none;
         background-color: rgba(100, 100, 100, .5) ;
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
        <div v-if="$store.state.user">
            <h2>My Clubhouse <a style="display: inline-block;" href="#/clubhouse-settings" v-if="$store.state.user.role === 'ADMIN'"><i class="material-icons">settings</i></a></h2>
            <h3>Forums</h3>
            <div v-if="$store.state.forumChannels.length === 0" class="explain">No forums</div>
            <a :class="{'channel-unread': channel.hasNew, 'channel-link': true, active: channel.id === activeChannelId}" v-for="channel in $store.state.forumChannels" :href="'#/channel/' + channel.id"><i style="font-size:12px;" class="material-icons">forum</i> {{ channel.name }} <span v-if="channel.mentionsCount>0" class="unread-mentions-count">{{ channel.mentionsCount }}</span></a>                
            <h3><a style="display: inline-block" href="#/my-channels">Channels</a> <a class="add-channel-link" href="#/channel-settings"><i class="material-icons">add_circle_outline</i></a></h3>
            <a :class="{'channel-unread': channel.hasNew, 'channel-link': true, active: channel.id === activeChannelId}" v-for="channel in $store.state.standardChannels" :href="'#/channel/' + channel.id"># {{ channel.name }} <span v-if="channel.mentionsCount>0" class="unread-mentions-count">{{ channel.mentionsCount }}</span></a>        
            <h3><a href="#/open-direct-message">Direct Messages</a></h3>
            <div v-if="$store.state.directMessageChannels.length === 0">
                <div class="explain">No message history</div>    
            </div>
            <a :class="{'channel-unread': channel.hasNew, 'direct-message-link': true, 'channel-link': true, active: channel.id === activeChannelId}" v-for="channel in $store.state.directMessageChannels" :href="'#/channel/' + channel.id">
                <span v-if="channel.directMessageUserIdsList.length===1" class="username-span" ><span class="user-awake-bubble" v-if="$store.state.allUsersById[channel.directMessageUserIdsList[0]].state==='AWAKE'">&#9679;</span><span class="user-offline-bubble" v-else>&#9675;</span>{{$store.state.allUsersById[channel.directMessageUserIdsList[0]].username}}</span>
                <span v-else>
                    <span class="dm-user-count">{{ channel.directMessageUserIdsList.length }}</span>
                    <span class="username-span" v-for="userId,index in channel.directMessageUserIdsList" v-if="userId != $store.state.user.id">{{ $store.state.allUsersById[userId].username }}<span v-if="index < (channel.directMessageUserIdsList.length-1)">,</span></span>
                </span>
                <span v-if="channel.mentionsCount>0" class="unread-mentions-count">{{ channel.mentionsCount }}</span>
            </a>
            <div v-if="user && user.id" class="loggedin-name">
                <div>{{ user.displayName || user.username }}</div>
                <div><a href="#/my-settings">Settings</a> | <a href="/st-users/logoff">Log off</a></div>
            </div>
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
             
         }
     }
 }
</script>
