
<style lang="scss">
 .forum-top-level-vue {
     padding: 1em 20px 20px 20px;
     .forum-name {
         margin-top: 0em;
         margin-bottom: 1em;
     }
     .topic-section {
         margin-bottom: 2em;
     }
 }
</style>


<template>
    <div class="forum-top-level-vue">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-if="!isLoading">
            <h3 class="forum-name">{{ channel.name }}</h3>
            <div class="topic-section">
                <h5>Pinned Topics</h5>
                <div v-for="topic in pinnedTopics">
                    <forum-topic-row :topic="topic"></forum-topic-row>
                </div>
            </div>
            <div class="topic-section">
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
         onRoute: function() {
             var self = this;
             self.channelId = this.$route.params.channelId;
             self.channel = null;
             this.fetchData();
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
