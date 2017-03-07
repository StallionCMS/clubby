
<style lang="scss">
 .forum-thread-vue {
     .topic-header {
         background-color: white;
         border-bottom: 2px solid #ddd;
         position: fixed;
         width: 100%;
         padding: .5em 20px .5em 20px;
         .name-row {
             font-size: 16px;
         }
         .topic-meta {
             color: #888;
             font-size: 12p;
         }
     }
     .topic-body {
         min-height: 99vh;
         background-color: white;
         padding-top: 70px;
         padding-left: 20px;

     }
     .topic-messages, .topic-post {
         max-width: 750px;         
     }
     
     
 }
</style>


<template>
    <div class="forum-thread-vue">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-if="!isLoading">
            <div class="topic-header">
                <div class="name-row"><a :href="'#/forum/' + channel.id">{{ channel.name }}</a> &#8250;
                    {{ topic.title }}
                </div>
                <div class="topic-meta">Created {{ formatDate(topic.createdAt) }} by {{ topic.fromUsername }}</div>
            </div>
            <div class="topic-body">
                <div class="topic-messages">
                    <div v-for="message in messages">
                        <div>{{ message.fromUsername }} {{ formatDate(message.createdAt) }}</div>
                        <div>
                            {{ message.html }}
                        </div>
                    </div>
                </div>
                <div class="topic-post">
                    <h5>Post a message</h5>
                    <textarea></textarea>
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
             messages: null,
             topic: null,
             channelId: null,
             parentMessageId: null
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
             self.parentMessageId = this.$route.params.parentMessageId;
             self.messages = [];
             self.channel = null;
             self.isLoading = true;
             this.fetchData();
         },
         formatFromNow: function(createdAt) {
             return moment.tz(createdAt * 1000, moment.tz.guess()).fromNow();//;.format('MMM d, YYYY h:mm a');
         },
         formatDate: function(createdAt) {
             return moment.tz(createdAt * 1000, moment.tz.guess()).format('MMM D, YYYY h:mm a');
         },         
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/messaging/forum-thread/' + self.channelId  + '/' + self.parentMessageId,
                 success: function(o) {
                     self.isLoading = false;
                     self.channel = o.channel;
                     var messages = o.threadContext.messages;
                     self.topic = o.threadContext.topic;
                     messages.forEach(function(msg) {
                         console.log(msg);
                         msg.html = JSON.parse(msg.messageJson).bodyMarkdown;
                     });
                     self.messages = messages;
                 }
             });
         }
     }
 };
</script>
