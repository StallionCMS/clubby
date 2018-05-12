
<style lang="scss">
 .forum-top-level-vue {
     border-top: 1px solid transparent;
     background-color: white;
     .forum-header {
         margin-top: -2px;
         border-bottom: 1px solid #F0F0F0;
         padding: .8em 20px .8em 20px;
         position: fixed;
         background-color: white;
         z-index: 1;
         .forum-name {
             margin-top: 0em;
             margin-bottom: 0em;
             font-size: 18px;
             font-weight: bold;
             padding-left: 40px;
             display: block;
             max-width: 858px;
             .forum-name-inner {
                 vertical-align: -35%;
             }
             a.btn {
                 float: right;
                 margin-top: -3px;
             }
             .material-icons {
                 font-weight: normal;
                 vertical-align: -55%;
                 font-size: 18px;
             }
         }
     }
     .forum-top-body {
         max-width: 900px;
         border-top: 1px solid transparent;
         margin-top: 60px;
         padding: 0em 20px 20px 20px;
         
     }
     .topic-section:first-child {
         margin-top: -12px;
     }
     .topic-section {
         margin-bottom: 2em;
     }
     .channel-favorite, .channel-settings-icon-link  {
         /*float: right;
         margin-right: 20px; */
     }
     .channel-favorite.material-icons {
         cursor: pointer;
         opacity: .8;
     }     
     .material-icons.channel-favorite:hover {
         opacity: 1;
     }
     .channel-favorite-on.material-icons {
         color: #eae080;
     }
     .topic-counts {
         display: inline-block;
         font-weight: 600;
         color: #999;
     }
     .created-at {
         color: #888;
         font-size: 12px;
     }

     h5 {
         width: 100%;
         color: #F9F9F9;
         background-color: #999;
         padding: 6px;
         margin: 0px;
         display: none;
     }
     .topic-section {
         margin-bottom: 0px;
     }
     .open-settings-link, .open-settings-link > .material-icons {
         color: #444;
         text-decoration: none;

     }
     .open-settings-link:hover, .open-settings-link:hover > .material-icons{
         color: #23527c;
         text-decoration: none;
     }
     .open-settings-link:active {
         color: #23727c;
         text-decoration: none;
     }     
     

     .channel-context-menu {
         position: absolute;
         z-index: 100;
         top: 40px;
         left: 10px;
         background-color: white;
         border: 1px solid #ccc;
         width: 300px;
         padding-bottom: 1em;
         padding-top: 1em;
         box-shadow: rgba(55, 55, 55, 0.1) 0px 15px 50px 0px, rgba(55, 55, 55, 0.1) 0px 12px 15px 0px;

         
         a {
             font-weight: 400;
             color: #444;
             display: block;
             padding: 5px 15px;
         }
         a:hover {
             text-decoration: none;
             background-color: #F2F2F2;
         }
     }

     .no-topics {
         margin-left:40px;
         margin-top: 1em;
         margin-bottom: 1em;
         color: #999;
         
     }
     
 }
</style>


<template>
    <div class="forum-top-level-vue">
        <div v-if="channel" class="forum-header">
            <h3 class="forum-name">
                <a @click="showChannelContextMenu" href="javascript:;" class="open-settings-link">
                    <i v-if="channel.encrypted && channel.inviteOnly" class="material-icons">enhanced_encryption</i> <i v-if="!channel.encrypted && channel.inviteOnly" class="material-icons">lock</i>
                    <span class="forum-name-inner">{{ channel.name }}</span>
                    <i class="material-icons">keyboard_arrow_down</i>
                </a>
                <a class="btn btn-large btn-primary" :href="'#/forum/' + channel.id + '/new-thread'">New Topic</a>
            </h3>
        </div>
        <div v-if="contextMenuShown" class="channel-context-menu">
                <a href="javascript:;" @click="toggleFavorite"><span v-if="!channel.favorite">Make Favorite</span><span v-if="channel.favorite">Unfavorite</span></a>
                <a :href="'#/channel-members/' + channelId">View forum members</a>
                <a v-if="channel.owner" class="column-header-icon" :href="'#/channel-settings/' + channelId">Settings</a>
                
            </div>        
        <div v-if="isLoading"><loading-div></loading-div></div>        
        <div v-if="!isLoading" class="forum-top-body">
            <div class="topic-section" v-if="pinnedTopics.length">
                <h5>Pinned Topics</h5>
                <div v-for="topic in pinnedTopics">
                    <forum-topic-row :topic="topic" :pinned="true"></forum-topic-row>
                </div>
            </div>
            <div class="topic-section" v-if="watchedTopics.length">
                <h5>Your watched topics updated in the last 10 days</h5>
                <div class="no-topics" v-if="watchedTopics.length == 0">
                    No watched topics updated in the last 10 days.
                </div>
                <div v-for="topic in watchedTopics">
                    <forum-topic-row :watched="true" :topic="topic"></forum-topic-row>
                </div>
            </div>
            <div class="topic-section">
                <h5>New Topics in the last 10 days.</h5>
                <div class="no-topics" v-if="newTopics.length == 0">
                    No new topics in the last 10 days.
                </div>
                <div v-for="topic in newTopics">
                    <forum-topic-row :topic="topic" :newish="true"></forum-topic-row>
                </div>
            </div>
            <div class="topic-section">
                <h5>All Topics</h5>
                <div class="no-topics" v-if="updatedTopics.length == 0">
                    No other topics found.
                </div>
                <div v-for="topic in updatedTopics">
                    <forum-topic-row :topic="topic"></forum-topic-row>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             contextMenuShown: false,
             isLoading: true,
             channel: null,
             pinnedTopics: [],
             watchedTopics: [],
             updatedTopics: [],
             newTopics: [],
         };
     },
     created: function() {
         this.onRoute();
     },
     watch: {
         '$route': 'onRoute'
     },
     beforeDestroy: function() {
         document.removeEventListener('click', this.hideChannelContextMenu);
     },     
     methods: {
         refresh: function() {
             this.fetchData();
         },
         onRoute: function() {
             var self = this;
             localStorage['last-channel-path|' + this.$store.state.user.id] = window.location.hash;
             ClubhouseVueApp.currentChannelComponent = this;
             self.isLoading = true;
             self.channelId = this.$route.params.channelId;
             self.channel = self.$store.state.channelById[self.channelId] || null;
             this.fetchData();
         },
         handleIncomingNewReaction: function() {
             this.refresh();
         },
         handleIncomingRemoveReaction: function() {
             this.refresh();
         },
         handleIncomingMessage: function(message) {
             this.$store.commit('newChannelMessage', message);
             this.refresh();
         },
         
         toggleFavorite: function() {
             var self = this;
             var favorite = !self.channel.favorite;
             stallion.request({
                 url: '/clubhouse-api/channels/mark-channel-favorite/' + self.channel.id,
                 method: 'POST',
                 data: {
                     favorite: favorite
                 },
                 success: function() {
                     self.channel.favorite = favorite;
                     self.$store.commit('channelUpdated', self.channel);
                 }
             });
         },
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/messaging/forum-top-level/' + self.channelId,
                 success: function(o) {
                     self.isLoading = false;
                     self.channel = o.channel;
                     self.pinnedTopics = o.topicContext.pinnedTopics;
                     self.watchedTopics = o.topicContext.watchedTopics;
                     self.newTopics = o.topicContext.newTopics;
                     self.updatedTopics = o.topicContext.updatedTopics;
                     setTimeout(function() {
                         ClubhouseMobileInterop.markRouteLoaded();
                     }, 20);                     
                                      /*        private List<ForumTopic> pinnedTopics = list();
        private List<ForumTopic> watchedTopics = list();
        private List<ForumTopic> newTopics = list();
        private List<ForumTopic> updatedTopics = list();
*/
                 }
             });
         },
         showChannelContextMenu: function() {
             var self = this;
             if (this.contextMenuShown) {
                 this.hideChannelContextMenu();
             } else {
                 setTimeout(function() {
                     document.addEventListener('click', self.hideChannelContextMenu);
                 }, 20);
                 this.contextMenuShown = true;
             }
         },
         hideChannelContextMenu: function() {
             this.contextMenuShown = false;
             document.removeEventListener('click', this.hideChannelContextMenu);
         }
     }
 };
</script>
