<style lang="scss">
 .channel-feed-vue {
     margin-left: 220px;
     border-top: 1px solid #252a4e;
     margin-top: -1px;
     background-color: #252a4e;
     .channel-header {
         background-color: #F9F9F9;
         border-bottom: 1px solid #ccc;
         padding: 4px;
         font-size: 18px;
         font-weight: bold;
         padding-left: 20px;
         margin-top: 0px;
         position: fixed;
         width: 100%;
         max-width: 700px;
         height: 30px;
         z-index: 10;
     }
     .channel-messages {
         max-width: 700px;
         padding-left: 20px;
         padding-right: 20px;
         background-color: #f2f2f2;
         height: 100vh;
         overflow: scroll;
     }
     .a-message {
         margin-top: 1em;
         display: flex;
         flex-direction: row;
     }
     .a-message-left {
         display: block;
         width: 60px;
         margin-right: 15px;
         clear: left;
         margin-top: 5px;
     }
     .a-message-avatar {
         border: 1px solid #ddd;
         max-width: 50px;
     }
     
     a.a-message-from {
         color: #555;
         font-weight: bold;
     }
 }
</style>

<template>
    <div class="channel-feed-vue" v-if="channel">
        <div class="channel-header">
            {{ channel.name }}
        </div>
        <div class="channel-messages">
            <div class="a-message" v-for="message in messages">
                <div class="a-message-left">
                    <img class="a-message-avatar" :src="'https://www.gravatar.com/avatar/' + hashUser(message.fromUsername) + '?d=retro'">
                </div>
                <div class="a-message-right">
                    <div class="a-message-meta">
                        <a :href="'#/user/' + message.fromUserId" class="a-message-from">{{ message.fromUsername }}</a>
                        <span class="a-message-createdAt">{{ message.createdAt }}</span>
                    </div>
                    {{ message.html }}
                </div>
            </div>
            <div class="post-message-box">
                <label>Post a message.</label>
                <textarea class="form-control" v-model="newMessage"></textarea>
            </div>
            <div class="p">&nbsp;</div>
        </div>

    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             channels: [],
             channelId: null,
             messages: [],
             channel: null,
             newMessage : ''
         }
     },
     created: function() {
         this.fetchData()
     },
     watch: {
         '$route': 'fetchData'
     },     
     computed: {
        
     },
     methods: {
         hashUser: function(username) {
             return md5(username + '@stallion.io');
         },
         fetchData: function() {
             var self = this;
             self.messages = [];
             self.channel = null;
             self.channelId = parseInt(self.$route.params.channelId);
             stallion.request({
                 url: '/clubhouse-api/messaging/my-channel-context/' + self.channelId,
                 success: function(o) {
                     o.messages.forEach(function(msg) {
                         console.log(msg);
                         if (!msg.messageJson) {
                             return;
                         }
                         var info = JSON.parse(msg.messageJson);
                         // TODO: sanitize HTML https://github.com/google/caja/blob/master/src/com/google/caja/plugin/html-sanitizer.js
                         msg.html = info.bodyMarkdown;
                     });
                     self.messages = o.messages;
                     self.channel = o.channel;
                 }
             });
         }
     }
 }
</script>
