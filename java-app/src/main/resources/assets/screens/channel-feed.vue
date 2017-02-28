<style lang="scss">
 @media (max-width: 920px) {
     .channel-feed-vue .channel-messages {
         min-width: 700px;
     }
 }
 .channel-feed-vue {
     border-top: 1px solid #252a4e;
     margin-top: -2px;
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
         width: 700px;
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
         width: 100%;
     }
     .a-message-right {
         width: 650px;
     }
     .a-message-left {
         display: block;
         width: 50px;
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
            {{ channelName }} <span v-if="isEncrypted">&#128274;</span>
            <span>{{ members.length }} members</span>
        </div>
        <div v-if="!messagesDecrypted">
            Loading messages...
        </div>
        <div v-if="messagesDecrypted" class="channel-messages" style="padding-top: 40px;"">
            <div v-if="messages.length === 0" class="p" style="margin-top: 40px;color:#666;">
                No messages yet in this channel.
            </div>
            <div class="a-message" v-for="message in messages"  :key="message.id">
                <div class="a-message-left">
                    <img class="a-message-avatar" :src="'https://www.gravatar.com/avatar/' + message.userHash + '?d=retro'">
                </div>
                <div class="a-message-right">
                    <div class="a-message-meta">
                        <a :href="'#/user/' + message.fromUserId" class="a-message-from">{{ message.fromUsername }}</a>
                        <span class="a-message-createdAt">{{ message.createdAtFormatted }}</span>
                    </div>
                    <div class="message-html" v-raw-html="message.html"></div>
                </div>
            </div>
            <div class="post-message-box">
                <form @submit.prevent="postMessage">
                    <label>Post a message.</label>
                    <message-textarea id="post-message-box" :disabled="messageAreaDisabled || !publicKeysAvailable" @submit="postMessage" v-model="newMessage"></message-textarea>
                    <!--<input type="text" class="form-control" v-model="newMessage">-->
                </form>
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
             messagesDecrypted: false,
             channel: null,
             channelName: '',
             members: [],
             isEncrypted: null,
             publicKeysAvailable: false,
             newMessage : '',
             messageAreaDisabled: false
         }
     },
     created: function() {
         this.fetchData()
     },
     directives: {
         'raw-html': {
             inserted: function(el, binding) {
                 if (binding.value === null || binding.value === undefined) {
                     el.innerHTML = '';
                 } else {
                     el.innerHTML = binding.value;
                 }
             },
             update: function(el, binding) {
                 return;
                 if (binding.value === null  || binding.value === undefined) {
                     el.innerHTML = '';
                 } else {
                     el.innerHTML = binding.value;
                 }
             }
         }
     },
     watch: {
         '$route': 'fetchData',
         'messages': function() {
             console.log('messages changed!!');
             var self = this;
             Vue.nextTick(function () {
                 // DOM updated
                 var objDiv = $(self.$el).find('.channel-messages').get(0);
                 if (objDiv) {
                     objDiv.scrollTop = objDiv.scrollHeight + 200;
                     console.log('scroll to ', objDiv.scrollHeight);
                 }

             });
         },
         'messagesDecrypted': function() {
             var self = this;
             Vue.nextTick(function () {
                 // DOM updated
                 var objDiv = $(self.$el).find('.channel-messages').get(0);
                 if (objDiv) {

                     objDiv.scrollTop = objDiv.scrollHeight + 200;
                     console.log('scroll to ', objDiv.scrollTop);
                 }

             });
         }
     },     
     computed: {
        
     },
     methods: {
         hashUser: function(username) {
             return md5(username + '@stallion.io');
         },
         fetchData: function() {
             var self = this;
             ClubhouseVueApp.currentChannelComponent = this;
             self.messages = [];
             self.channel = null;
             self.messagesDecrypted = false;
             self.channelId = parseInt(self.$route.params.channelId);
             stallion.request({
                 url: '/clubhouse-api/messaging/my-channel-context/' + self.channelId,
                 success: function(o) {
                     self.isEncrypted = o.channel.encrypted;
                     o.messages.forEach(function(message) {
                         if (!message.messageJson) {
                             return;
                         }
                         var info = JSON.parse(message.messageJson);
                         // TODO: sanitize HTML https://github.com/google/caja/blob/master/src/com/google/caja/plugin/html-sanitizer.js
                         message.createdAtFormatted = self.formatDate(message.createdAt);
                         message.userHash = self.hashUser(message.fromUsername);
                         message.html = self.markdownToHtml(info.bodyMarkdown || '');
                         message.text = info.bodyMarkdown;
                     });
                     self.messages = o.messages;
                     self.channel = o.channel;
                     if (o.channel.channelType === 'DIRECT_MESSAGE') {
                         var name = 'Direct message with';
                         o.members.forEach(function(member) {
                             if (member.id === self.$store.state.user.id) {
                                 return;
                             }
                             name += ' ' + (member.displayName || member.username);
                         });
                         self.channelName = name;
                     } else {
                         self.channelName = o.channel.name;
                     }
                     self.isEncrypted = o.channel.encrypted;


                     self.members = o.members;
                     self.publicKeysAvailable = false;
                     var keysProcessed = 0;
                     if (self.isEncrypted) {
                         self.members.forEach(function(member) {
                             crypto.subtle.importKey(
                                 'spki',
                                 hexToArray(member.publicKeyHex),
                                 {
                                     name: "RSA-OAEP",
                                     modulusLength: 2048,
                                     publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
                                     hash: {name: "SHA-256"}
                                 },
                                 true,
                                 ["encrypt"]
                             ).then(function(result) {
                                 console.log('got public key for member ', member.username);
                                 member.publicKey = result;
                                 keysProcessed++;
                                 if (keysProcessed >= self.members.length) {
                                     self.publicKeysAvailable = true;
                                 }
                             });
                         });
                     }
                     var leftToDecrypt = self.messages.length;
                     if (leftToDecrypt === 0) {
                         self.messagesDecrypted = true;
                     }
                     if (self.isEncrypted) {
                         var index = 0;
                         self.messages.forEach(function(message) {
                             message.$index = index;
                             new Decrypter().decryptMessage(
                                 self.$store.state.privateKey,
                                 hexToArray(message.messageEncryptedJson),//info.encryptedMessageBytes,
                                 hexToArray(message.messageEncryptedJsonVector),//info.messageVector,
                                 hexToArray(message.encryptedPasswordHex),//ep.encryptedPasswordBytes,
                                 hexToArray(message.passwordVectorHex),//ep.passwordVector,
                                 function(bodyJson) {
                                     console.log('decrypted message ', bodyJson);
                                     var data = JSON.parse(bodyJson);
                                     message.html = self.markdownToHtml(data.bodyMarkdown);
                                     message.text = data.bodyMarkdown;
                                     console.log('message.html! ', message.html);
                                     Vue.set(self.messages, message.$index, message);
                                     leftToDecrypt--;
                                     if (leftToDecrypt <= 0) {
                                         self.messagesDecrypted = true;
                                     }
                                     //Vue.$set(self.messages, 
                                 }
                             );  
                             index++;
                         });
                     } else {
                         self.messagesDecrypted = true;
                     }

                 }
             });
         },
         formatDate: function(secs) {
             return moment.tz(secs * 1000, moment.tz.guess()).format('h:mm a');
         },
         handleIncomingMessage: function(incoming, data) {
             var self = this;
             var exists = false;
             self.messages.forEach(function(msg) {
                 console.log('msg.id ', msg.id);
                 if (msg.id === incoming.id) {
                     exists = true;
                 }
             });
             if (exists) {
                 return;
             }
             var message = {
                 html: '',
                 text: '',
                 createdAt: incoming.createdAt,
                 fromUsername: incoming.fromUsername,
                 id: incoming.id,
                 createdAtFormatted: self.formatDate(incoming.createdAt),
                 userHash: self.hashUser(incoming.fromUsername)
             };
             if (self.isEncrypted) {
                 new Decrypter().decryptMessage(
                     self.$store.state.privateKey,
                     hexToArray(incoming.messageEncryptedJson),//info.encryptedMessageBytes,
                     hexToArray(incoming.messageEncryptedJsonVector),//info.messageVector,
                     hexToArray(incoming.encryptedPasswordHex),//ep.encryptedPasswordBytes,
                     hexToArray(incoming.passwordVectorHex),//ep.passwordVector,
                     function(bodyJson) {
                         console.log('decrypted message ', bodyJson);
                         var data = JSON.parse(bodyJson);
                         message.html = self.markdownToHtml(data.bodyMarkdown);
                         message.text = data.bodyMarkdown;
                         console.log('message.html! ', message.html);
                         self.messages.push(message);
                         self.showMessageNotificationMaybe(incoming, message);
                     }
                 );                   
             } else {
                 var messageData = JSON.parse(incoming.messageJson);
                 message.html = self.markdownToHtml(messageData.bodyMarkdown);
                 message.text = messageData.bodyMarkdown;
                 self.messages.push(message);
                 self.showMessageNotificationMaybe(incoming, message);
             }
         },
         markdownToHtml: function(markdown) {
             markdown = markdown.replace(/</g, '&lt;');
             markdown = markdown.replace(/>/g, '&gt;');
             var converter = new showdown.Converter({
                 simplifiedAutoLink: true,
                 excludeTrailingPunctuationFromURLs: true,
                 simpleLineBreaks: true
             });
             
             var html = converter.makeHtml(markdown);
             //return html;
             $(html).find('a').each(function(anchor) {
                 var link = this.getAttribute('href');
                 var iframeId = 'embed-frame-' + generateUUID();
                 if (link.indexOf('//') > -1) {
                     //html += '<iframe  id="' + iframeId + '"></iframe>';
                     html += '<iframe style="display:none;" id="' + iframeId + '" class="embed-iframe" sandbox="allow-scripts allow-popups" src="https://clubhouse.local/oembed-iframe?embedUrl=' + 
                             encodeURIComponent(link) + '&iframeId=' +
                             encodeURIComponent(iframeId) +  '"></iframe>';
                 }
             });
             return html;
         },
         showMessageNotificationMaybe: function(incoming, message) {
             var self = this;
             if (incoming.read) {
                 return;
             }
             if (incoming.fromUsername !== self.$store.state.user.username) {
                 console.log('visible now?' , ifvisible.now(), ifvisible.now('hidden'), ifvisible.now('idle'));
                 if (true || !ifvisible.now() || ifvisible.now('hidden') || ifvisible.now('idle')) {
                     stallionClubhouseApp.sendNotifiction(
                         'Message from ' + incoming.fromUsername,
                         {
                             body: message.text,
                             icon: 'https://www.gravatar.com/avatar/' + self.hashUser(incoming.fromUsername) + '?d=retro',
                             silent: false
                         },
                         'https://clubhouse.local/#/channel/' + self.channelId
                     );
                 }
             }
         },
         postMessage: function() {
             if (this.isEncrypted) {
                 this.postEncryptedMessage();
             } else {
                 this.postPlainMessage();
             }
             
         },
         postEncryptedMessage: function() {
             var self = this;
             console.log('post message!');
             self.messageAreaDisabled = true;

             var message = JSON.stringify({'bodyMarkdown': self.newMessage});

             var tos = [];
             self.members.forEach(function(member) {
                 tos.push({
                     username: member.username,
                     userId: member.id,
                     publicKey: member.publicKey
                 });
                 console.log('prepping to encrypt for ', member.username);
             });

             new Encrypter().encryptMessage(
                 message,
                 tos,
                 function(result) {
                     console.log('encryption complete ', result);
                     stallion.request({
                         url: '/clubhouse-api/messaging/post-encrypted-message',
                         method: 'POST',
                         data: {
                             messageEncryptedJson: result.encryptedMessageHex,
                             messageVectorHex: result.messageVectorHex,
                             channelId: self.channelId,
                             encryptedPasswords: result.encryptedPasswords,
                         },
                         success: function(o) {
                             console.log('encrypted message posted!', o);
                             self.newMessage = '';
                             //self.messages.push(message);
                             self.messageAreaDisabled = false;
                             Vue.nextTick(function() {
                                 $('#post-message-box').focus();
                             });
                             
                         }
                     });
                 }
             );


         },
         encryptMessageForEveryChannelMember: function(members) {
             
         },
         postPlainMessage: function() {
             var self = this;
             console.log('post message!');
             self.messageAreaDisabled = true;
             stallion.request({
                 url: '/clubhouse-api/messaging/post-message',
                 method: 'POST',
                 data: {
                     messageJson: JSON.stringify({'bodyMarkdown': self.newMessage}),
                     channelId: self.channelId
                 },
                 success: function(o) {
                     console.log('message posted!', o);
                     var messageData = JSON.parse(o.messageJson);
                     // TODO: strip out HTML, parse Markdown
                     var message = {
                         html: messageData.bodyMarkdown,
                         text: messageData.bodyMarkdown,
                         createdAt: o.createdAt * 1000,
                         fromUsername: o.fromUsername,
                         id: o.id
                     };
                     console.log('o.id ', o.id);
                     self.newMessage = '';
                     //self.messages.push(message);
                     self.messageAreaDisabled = false;
                 }
             });
         }
     }
 }
</script>
