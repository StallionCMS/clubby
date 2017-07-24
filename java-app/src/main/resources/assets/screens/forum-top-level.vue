
<style lang="scss">
 .forum-top-level-vue {
     border-top: 1px solid transparent;
     background-color: white;
     .forum-header {
         margin-top: -2px;
         border-bottom: 1px solid #F0F0F0;
         padding: .8em 20px .8em 20px;
         position: fixed;
         width: 100%;
         background-color: white;
         .forum-name {
             margin-top: 0em;
             margin-bottom: 0em;
             max-width: 700px;
             a.btn {
                 float: right;
                 margin-top: -3px;
             }
             .material-icons {
                 vertical-align: -15%;
             }
         }
     }
     .forum-top-body {
         max-width: 900px;
         border-top: 1px solid transparent;
         margin-top: 70px;
         padding: 0em 20px 20px 20px;
         
     }
     .topic-section {
         margin-bottom: 2em;
     }
     .channel-favorite, .channel-settings-icon-link  {
         float: right;
         margin-right: 20px;
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
     
 }
</style>


<template>
    <div class="forum-top-level-vue">
        <div v-if="channel" class="forum-header">
            <h3 class="forum-name">
                <i v-if="channel.encrypted" class="material-icons">security</i> <i v-if="!channel.encrypted && channel.inviteOnly" class="material-icons">lock</i>
                {{ channel.name }}
                <a class="btn btn-primary btn-md" :href="'#/forum/' + channelId  + '/new-thread'">New Thread</a>
                <a v-if="channel.owner" class="channel-settings-icon-link" :href="'#/channel-settings/' + channelId"><i class="material-icons">settings</i></a>
                <i @click="toggleFavorite" :class="['material-icons', 'channel-favorite', channel.favorite ? 'channel-favorite-on' : '']">star</i> 
            </h3>
        </div>
        <div v-if="isLoading"><loading-div></loading-div></div>        
        <div v-if="!isLoading" class="forum-top-body">
            <div class="topic-section" v-if="pinnedTopics.length">
                <h5>Pinned Topics</h5>
                <div v-for="topic in pinnedTopics">
                    <forum-topic-row :topic="topic"></forum-topic-row>
                </div>
            </div>
            <div class="topic-section" v-if="watchedTopics.length">
                <h5>Your watched topics updated in the last 10 days</h5>
                <div v-if="watchedTopics.length == 0">
                    <em>No watched topics updated in the last 10 days.</em>
                </div>
                <div v-for="topic in watchedTopics">
                    <forum-topic-row :topic="topic"></forum-topic-row>
                </div>
            </div>
            <div class="topic-section">
                <h5>New Topics in the last 10 days.</h5>
                <div v-if="newTopics.length == 0">
                    <em>No new topics in the last 10 days.</em>
                </div>
                <div v-for="topic in newTopics">
                    <forum-topic-row :topic="topic"></forum-topic-row>
                </div>
            </div>
            <div class="topic-section">
                <h5>All Topics</h5>
                <div v-if="updatedTopics.length == 0">
                    <em>No other topics found.</em>
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
     methods: {
         refresh: function() {
             this.fetchData();
         },
         onRoute: function() {
             var self = this;
             localStorage.lastChannelPath = window.location.hash;
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
         }
     }
 };
</script>
