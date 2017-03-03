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
         padding-top: 40px;
     }
     .a-message {
         margin-top: 0em;
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
         max-width: 40px;
         max-height: 40px;
     }
     
     a.a-message-from {
         color: #555;
         font-weight: bold;
     }
     .add-reactions-buttons {
         float: right;
         visibility: hidden;
         height: 10px;
         background-color: white;
         a {
             
             padding: 4px 2px 2px 2px;
             background-color: white;
             height: 28px;
             display: inline-block;
             color: #999;
             border: 1px solid #999;
             i.material-icons {
                 font-size: 18px;    
             }
         }
         a:hover {
             background-color: #F2F2F2;
             color: #333;
         }
         a:first-child {
             border-radius: 4px 0px 0px 4px;
             border-right-width: 0px;
         }
         a:last-child {
             border-radius: 0px 4px 4px 0px;
             border-left-width: 0px;
         }
         a:first-child:last-child {
             border-radius: 4px 4px 4px 4px;
             border-right-width: 1px;
             border-left-width: 1px;
         }
         
     }
     .a-message-outer:hover {
         .a-message {
             background-color: #E9E9E9;
         }
         .add-reactions-buttons {
             visibility: visible;
         }
     }
     a.a-reaction {
         text-decoration: none;
         border: 1px solid #ccc;
         border-radius: 2px;
         padding: 3px;
         font-size: 13px;
         display: inline-block;
         margin-right: 4px;
         color: #888;
         .emoji-sizer {
             height: 17px;
             width: 17px;
         }
     }
     a.a-reaction:hover, a.a-reaction.a-reaction-active {
         background-color: #e0ecf9;
         border-color: #759ece;
         color: #759ece;
         cursor: pointer;
     }
     a.a-reaction.a-reaction-active:hover {
         border-color: #609de4;
     }
     .big-emoji {
         .emoji-sizer {
             width: 33px;
             height: 33px;
         }
     }
     .wrap-insert-emoji-button {
         height: 2px;
         display: inline-block;
         float:right;
         .insert-emoji-button {
             margin-top: 5px;
             margin-right: 15px;
             float: right;
             font-size: 34px;
             color: #888;
             font-weight: 100;
             text-decoration:none;
         }
     }
     .message-edited {
         color: #bbb;
         float: right;
     }
     .settings-link {
         color: #999;
     }
     .channel-header {
         .material-icons {
             color: #666;
             font-size: 20px;
         }
     }
 }
</style>

<template>
    <div class="channel-feed-vue" >
        <div v-if="channel" class="channel-header">
            {{ channelName }}
            <span v-if="channel.inviteOnly"><i class="material-icons">lock</i></span>
            <span v-if="isEncrypted"><i class="material-icons">security</i></span>
            <a class="settings-link" :href="'#/channel-members/' + channelId"><span>{{ members.length }} <i class="material-icons">people</i></span></a>
            <a class="settings-link" :href="'#/channel-settings/' + channelId"><i class="material-icons">settings</i></a>
        </div>
        <div v-if="!isLoaded" style="padding-top: 40px;">
            Loading messages...
        </div>
        <div class="channel-messages" style="">
            <div v-if="messagesDecrypted && isLoaded">
                <div v-if="!hasMore">
                    <h5><em>This is the begining of the #{{channelName}} channel.</em></h5>
                </div>
                <div v-if="messages.length === 0" class="p" style="margin-top: 40px;color:#666;">
                    No messages yet in this channel.
                </div>
                <div class="a-message-outer" v-for="message in messages"  :key="message.id" v-if="!message.deleted">
                    <div class="add-reactions-buttons">
                        <a href="javascript:;" @click="openAddReaction($event, message)"><i class="material-icons">tag_faces</i></a><a v-if="message.fromUsername===$store.state.user.username" href="javascript:;" @click="openEditMessage(message)"><i class="material-icons">mode_edit</i></a><a v-if="isChannelOwner || message.fromUsername===$store.state.user.username" href="javascript:;" @click="openDeleteModal(message)"><i class="material-icons">delete</i></a>
                    </div>
                    <div class="a-message" :id="'channel-message-' + message.id">
                        <div class="a-message-left">
                            <img v-if="message.showUser" class="a-message-avatar" :src="$store.state.allUsersById[message.fromUserId].avatarUrl">
                        </div>
                        <div class="a-message-right">
                            <div class="a-message-meta">
                                <div v-if="message.showUser">
                                    <a :href="'#/user/' + message.fromUserId" class="a-message-from">{{ message.fromUsername }}</a>
                                    <span class="a-message-createdAt">{{ message.createdAtFormatted }}</span>
                                </div>
                                
                            </div>
                            <div v-if="message.editing">
                                <message-textarea :disabled="message.saving" @submit="saveMessageEdits(message)" v-model="message.text"></message-textarea>
                            </div>
                            <div v-if="!message.editing && message.html">
                                <div class="message-html" v-raw-html="message.html"></div>
                            </div>
                            
                            <div class="message-reactions">
                                <a @click="toggleReaction(message, data)" :title="data.title" :class="{'a-reaction': true, 'a-reaction-active': data.currentUserReacted}" v-for="data, emoji in message.reactionsProcessed" v-show="data.count > 0"><span v-raw-html="data.sprite"></span> &nbsp;{{ data.count }}</a>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="post-message-box">
                    <form @submit.prevent="postMessage">
                        <label>Post a message.</label>
                        <div class="wrap-insert-emoji-button">
                            <a class="insert-emoji-button" @click="openInsertEmoji" href="javascript:;">&#x263a;</a>
                        </div>
                        <message-textarea id="post-message-box" :disabled="messageAreaDisabled || !publicKeysAvailable" @submit="postMessage" v-model="newMessage"></message-textarea>
                        
                        <!--<input type="text" class="form-control" v-model="newMessage">-->
                    </form>
                </div>
                <div class="p">&nbsp;</div>
                <emoji-popup @input="insertEmoji" ref="messageemojipopup"></emoji-popup>
                <emoji-popup @input="onChooseReactionEmoji" @close="onCloseReactionEmoji" ref="emojipopup"></emoji-popup>
            </div>
            <delete-message-modal v-if="showDeleteModal && messageToDelete" @close="showDeleteModal=false;messageToDelete=null" @delete="onDeleteMessage" :message="messageToDelete"></delete-message-modal>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             channels: [],
             channelId: null,
             isLoaded: false,
             isChannelOwner: false,
             messages: [],
             messagesDecrypted: false,
             channel: null,
             channelName: '',
             members: [],
             scrollToMessageId: '',
             fetching: false,
             page: 1,
             reactToMessage: null,
             isEncrypted: null,
             publicKeysAvailable: false,
             newMessage : '',
             messageAreaDisabled: false,
             showDeleteModal: false,
             messageToDelete: null,
             hasMore: false
         }
     },
     created: function() {
         this.onRoute()
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
         '$route': 'onRoute',
         'messages': function() {
             return;
             console.log('messages changed!!');
             var self = this;
             Vue.nextTick(function () {
                 if (self.page > 2) {
                     return;
                 }
                 // DOM updated
                 var objDiv = $(self.$el).find('.channel-messages').get(0);
                 if (objDiv) {
                     objDiv.scrollTop = objDiv.scrollHeight + 200;
                     console.log('scroll to ', objDiv.scrollHeight);
                 }

             });
         },
         'messagesDecrypted': function() {
             return;
             var self = this;
             Vue.nextTick(function () {
                 if (self.page > 2) {
                     return;
                 }
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
     mounted: function() {
         var self = this;
         $(this.$el).find('.channel-messages').scroll(function(e) {
             var el = this;
             if (el.scrollTop === 0 && self.hasMore) {
                 console.log('load new page!');
                 self.fetchPage();
             }
         });
     },
     methods: {
         onRoute: function() {
             var self = this;
             ClubhouseVueApp.currentChannelComponent = this;
             self.hasMore = false;
             self.messages = [];
             self.channel = null;
             self.messagesDecrypted = false;
             self.channelId = parseInt(self.$route.params.channelId);
             self.page = 1;
             self.fetchPage(self.page);
         },
         hashUser: function(username) {
             return md5(username);
         },
         refresh: function() {
             console.log('refresh channel feed');
             // refresh endpoint
             // Find all messages from (startpoint and greater)
             // If any changed, update it
         },
         openEditMessage: function(message) {
             message.editing = true;
         },
         saveMessageEdits: function(message) {
             var self = this;
             if (self.isEncrypted) {
                 self.saveEncryptedMessageEdits(message);
                 return;
             }
             message.saving = true;
             stallion.request({
                 url: '/clubhouse-api/messaging/update-message',
                 method: 'POST',
                 data: {
                     id: message.id,
                     messageJson: JSON.stringify({
                         bodyMarkdown: message.text
                     })
                 },
                 success: function(o) {
                     message.saving = false;
                     message.editing = false;
                     message.edited = true;
                     message.html = self.markdownToHtml(message.text, message);
                 }
             });
         },
         saveEncryptedMessageEdits: function(message) {
             var self = this;
             message.saving = true;
             new ReEncrypter().reencryptMesage(
                 JSON.stringify({
                         bodyMarkdown: message.text
                 }),
                 self.$store.state.privateKey,
                 message.encryptedPasswordHex,
                 message.passwordVectorHex,
                 function(result) {
                     stallion.request({
                         url: '/clubhouse-api/messaging/update-encrypted-message',
                         method: 'POST',
                         data: {
                             id: message.id,
                             messageEncryptedJsonVector: result.messageVectorHex,
                             messageEncryptedJson: result.encryptedMessageHex
                         },
                         success: function(o) {
                             message.html = self.markdownToHtml(message.text, message);
                             message.saving = false;
                             message.editing = false;
                             message.edited = true;
                         }
                     });             
                 });
                 
         },
         openDeleteModal: function(message) {
             this.showDeleteModal = true,
             this.messageToDelete = message;
         },
         onDeleteMessage: function(message) {
             stallion.request({
                 url: '/clubhouse-api/messaging/delete-message',
                 method: 'POST',
                 data: {messageId: message.id},
                 success: function() {
                     message.deleted = true;
                 }
             });
         },
         openAddReaction: function(event, message) {
             this.reactToMessage = message;
             this.$refs.emojipopup.toggle(event);
         },
         convertEmoji: function(emoji) {
             console.log('convert emoji ', emoji);
             return emoji;
         },
         onCloseReactionEmoji: function() {
             this.reactToMessage = null;
         },
         onChooseReactionEmoji: function(emoji) {
             var self = this;
             this.addReaction(this.reactToMessage, emoji);
             this.reactToMessage = null;
         },
         toggleReaction: function(message, data) {
             var self = this;
             if (data.currentUserReacted) {
                 self.removeReaction(message, data.emoji);
             } else {
                 self.addReaction(message, data.emoji);
             }
         },
         addReaction: function(message, emoji) {
             var self = this;
             console.log('add reaction', message.id, emoji);
             stallion.request({
                 url: '/clubhouse-api/messaging/add-reaction',
                 method: 'POST',
                 data: {
                     messageId: message.id,
                     emoji: emoji
                 },
                 success: function(o) {
                     self.addPersonToReactionEmoji(message, emoji, self.$store.state.user.username);
                     Vue.set(self.messages, message.$index, message);
                 }
             });
         },
         removeReaction: function(message, emoji) {
             var self = this;
             console.log('remove reaction', message.id, emoji);
             stallion.request({
                 url: '/clubhouse-api/messaging/remove-reaction',
                 method: 'POST',
                 data: {
                     messageId: message.id,
                     emoji: emoji
                 },
                 success: function(o) {
                     self.removePersonFromReactionEmoji(message, emoji, self.$store.state.user.username);
                     Vue.set(self.messages, message.$index, message);
                 }
             });

         },
         addPersonToReactionEmoji: function(message, emoji, username) {
             var self = this;
             var people = message.reactions[emoji] || [];
             people.push(username);
             var processed = self.processReactionEmoji(emoji, people);
             message.reactionsProcessed[emoji] = processed;
         },
         removePersonFromReactionEmoji: function(message, emoji, username) {
             var self = this;
             var people = message.reactions[emoji] || [];
             var newPeople = [];
             people.forEach(function(p) {
                 if (p === username) {
                     return;
                 }
                 newPeople.push(p);
             });
             message.reactions[emoji] = newPeople;
             var processed = self.processReactionEmoji(emoji, newPeople);
             message.reactionsProcessed[emoji] = processed;
         },
         processReactionEmoji: function(emoji, people) {
             var self = this;
             var text = people.join(', ') + ' reacted with ' + emoji;
             var currentUserReacted = people.indexOf(self.$store.state.user.username) > -1;
             var data = {
                 title: text, 
                 count: people.length,
                 currentUserReacted: currentUserReacted,
                 emoji: emoji,
                 sprite: stallionClubhouseApp.emojiConverter.replace_colons(emoji)
             }
             return data;
         },
         openInsertEmoji: function(event) {
             this.$refs.messageemojipopup.toggle(event);
         },
         insertEmoji: function(emoji) {
             console.log('insert emoji ', emoji);
             var ta = $(this.$el).find('#post-message-box').get(0);
             this.insertAtCursor(ta, emoji);
             setTimeout(function() {
                 $(this.$el).find('#post-message-box').focus();
             }, 20);
         },
         insertAtCursor(myField, myValue) {
             var self = this;
             //IE support
             if (document.selection) {
                 myField.focus();
                 sel = document.selection.createRange();
                 sel.text = myValue;
                 console.log('IE!');
             }
             //MOZILLA and others
             else if (myField.selectionStart || myField.selectionStart == '0') {
                 var startPos = myField.selectionStart;
                 var endPos = myField.selectionEnd;
                 
                 self.newMessage = myField.value.substring(0, startPos)
                     + myValue
                               + myField.value.substring(endPos, myField.value.length);
             } else {
                 self.newMessage += myValue;
             }

         },
         postFetchingFinished: function() {
             var self = this;
             var $div = $(self.$el).find('.channel-messages');
             var div = $div.get(0);
             if (self.page <=2) {
                 div.scrollTop = div.scrollHeight + 200;
             } else if (self.scrollToMessageId) {
                 var $msg = $('#channel-message-' + self.scrollToMessageId);
                 var animateTo = $msg.offset().top - 300;
                 var newScrollTop = $msg.offset().top;
                 div.scrollTop = newScrollTop;
                 console.log('scrollTop ', newScrollTop, animateTo);
                 $div.animate({ scrollTop: animateTo}, 1200);
             } 
         },
         postIncomingMessage: function() {
             var self = this;
             var div = $(self.$el).find('.channel-messages').get(0);
             var $div = $(div);
             var $message = $div.find('#channel-message-' + self.messages[self.messages.length-1].id);
             var distance = div.scrollHeight - div.scrollTop - $div.height() - $message.height();
             console.log('distance ', distance);
             if (distance < 150) {
                 div.scrollTop = div.scrollHeight + 200;
             }
         },
         fetchPage: function() {
             var self = this;
             if (self.fetching) {
                 return;
             }
             self.fetching = true;
             stallion.request({
                 url: '/clubhouse-api/messaging/my-channel-context/' + self.channelId + '?page=' + self.page, 
                 success: function(o) {
                     self.fetching = false;
                     self.page++;
                     if (o.messages.length >= 50) {
                         self.hasMore = true;
                     } else {
                         self.hasMore = false;
                     }
                     self.isEncrypted = o.channel.encrypted;
                     var messages = [];
                     var index = 0;
                     o.messages.forEach(function(messageCombo) {
                         var showUser = true;
                         var message = self.initMessageForFeed(messageCombo);
                         if (index > 0) {
                             if (messages[index-1].fromUsername === message.fromUsername) {
                                 showUser = false;
                             }
                             // If more than 20 minutes elapsed since previous message
                             if ((message.createdAt - messages[index-1].createdAt) > 1200) {
                                 showUser = true;
                             }
                         }
                         message.showUser = showUser;
                         messages.push(message);
                         index++;
                     });
                     messages.forEach(function(message) {
                         if (!message.messageJson) {
                             return;
                         }
                         var info = JSON.parse(message.messageJson);
                         message.html = self.markdownToHtml(info.bodyMarkdown || '', message);
                         message.text = info.bodyMarkdown;
                     });
                     
                     // Add new page of messages to the old one;
                     if (messages.length > 0) {
                         self.scrollToMessageId = messages[messages.length - 1].id;
                     } else {
                         self.scrollToMessageId = '';
                     }
                     var allMessages = messages.concat(self.messages);
                     for (var i =0;i<allMessages.length;i++) {
                         allMessages[i].$index = i;
                     }
                     self.messages = allMessages;
                     
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
                     } else {
                         self.publicKeysAvailable = true;
                     }
                     var leftToDecrypt = self.messages.length;
                     if (leftToDecrypt === 0) {
                         self.messagesDecrypted = true;
                     }
                     if (self.isEncrypted) {
                         messages.forEach(function(message) {
                             new Decrypter().decryptMessage(
                                 self.$store.state.privateKey,
                                 hexToArray(message.messageEncryptedJson),//info.encryptedMessageBytes,
                                 hexToArray(message.messageEncryptedJsonVector),//info.messageVector,
                                 hexToArray(message.encryptedPasswordHex),//ep.encryptedPasswordBytes,
                                 hexToArray(message.passwordVectorHex),//ep.passwordVector,
                                 function(bodyJson) {
                                     console.log('decrypted message ', bodyJson);
                                     var data = JSON.parse(bodyJson);
                                     message.html = self.markdownToHtml(data.bodyMarkdown, message);
                                     message.text = data.bodyMarkdown;
                                     console.log('message.html! ', message.html);
                                     Vue.set(self.messages, message.$index, message);
                                     leftToDecrypt--;
                                     if (leftToDecrypt <= 0) {
                                         self.messagesDecrypted = true;
                                         Vue.nextTick(self.postFetchingFinished);
                                     }
                                     self.$store.commit('markChannelSeen', self.channelId);
                                 }
                             );  
                         });
                     } else {
                         self.messagesDecrypted = true;
                         Vue.nextTick(self.postFetchingFinished);
                         self.$store.commit('markChannelSeen', self.channelId);
                     }
                     self.isLoaded = true;
                 }
             });
         },
         formatDate: function(secs) {
             return moment.tz(secs * 1000, moment.tz.guess()).format('h:mm a');
         },
         handleIncomingMessage: function(incoming, type, data, event) {
             var self = this;
             var isEdit = type === 'message-edited';
             var message = null;
             var existing = null;
             self.messages.forEach(function(msg) {
                 console.log('msg.id ', msg.id, incoming.id);
                 if (msg.id === incoming.id) {
                     existing = msg;
                 }
             });
             console.log('isEdit', isEdit);
             if (!isEdit && existing) {
                 return;
             } else if (isEdit && !existing) {
                 return;
             }
             if (!existing) {
                 var showUser = true;
                 if (self.messages.length > 0) {
                     if (self.messages[self.messages.length - 1].fromUsername === incoming.fromUsername) {
                         showUser = false;
                     }
                     // If more than 20 minutes elapsed since previous message
                     if ((incoming.createdAt - self.messages[self.messages.length-1].createdAt) > 1200) {
                         showUser = true;
                     }
                     
                 }
                 message = self.initMessageForFeed(incoming);
                 message.showUser = showUser;
                 message.$index = self.messages.length;
             } else {
                 console.log('editing message ', message);
                 message = self.initMessageForFeed(incoming);
                 message.edited = true;
                 message.showUser = existing.showUser;
                 message.$index = existing.$index;
             }
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
                         message.html = self.markdownToHtml(data.bodyMarkdown, message);
                         message.text = data.bodyMarkdown;
                         console.log('message.html! ', message.html);
                         if (isEdit) {
                             $(self.$el).find('#channel-message-' + message.id + ' .message-html').html(message.html);
                             Vue.set(self.messages, message.$index, message);
                         } else {
                             self.messages.push(message);
                             self.showMessageNotificationMaybe(incoming, message);
                             if (!message.read) {
                                 stallion.request({
                                     url: '/clubhouse-api/messaging/mark-read',
                                     method: 'POST',
                                     data: {messageId: message.id}
                                 });
                                 Vue.nextTick(self.postIncomingMessage);
                             }
                         }
                     }
                 );                   
             } else {
                 var messageData = JSON.parse(incoming.messageJson);
                 message.html = self.markdownToHtml(messageData.bodyMarkdown, message);
                 message.text = messageData.bodyMarkdown;
                 if (isEdit) {
                     Vue.set(self.messages, message.$index, message);
                     $(self.$el).find('#channel-message-' + message.id + ' .message-html').html(message.html);

                 } else {
                     self.messages.push(message);
                     self.showMessageNotificationMaybe(incoming, message);
                     Vue.nextTick(self.postIncomingMessage);
                     if (!message.read) {
                         stallion.request({
                             url: '/clubhouse-api/messaging/mark-read',
                             method: 'POST',
                             data: {messageId: message.id}
                         });
                     }
                 }
             }
         },
         initMessageForFeed: function(messageCombo) {
             var self = this;
             var message = {
                 '$index': null,
                 html: '',
                 text: '',
                 messageJson: messageCombo.messageJson,
                 messageEncryptedJson: messageCombo.messageEncryptedJson,
                 messageEncryptedJsonVector: messageCombo.messageEncryptedJsonVector,
                 encryptedPasswordHex: messageCombo.encryptedPasswordHex,
                 passwordVectorHex: messageCombo.passwordVectorHex,
                 edited: messageCombo.edited,
                 showUser: true,
                 editing: false,
                 saving: false,
                 deleted: false,
                 createdAt: messageCombo.createdAt,
                 createdAtFormatted: self.formatDate(messageCombo.createdAt),
                 userHash: self.hashUser(messageCombo.fromUsername),
                 fromUsername: messageCombo.fromUsername,
                 fromUserId: messageCombo.fromUserId,
                 id: messageCombo.id,
                 createdAtFormatted: self.formatDate(messageCombo.createdAt),
                 userHash: self.hashUser(messageCombo.fromUsername),
                 reactionsProcessed: {},
                 reactions: messageCombo.reactions || []
             }
             if (message.reactions) { 
                 Object.keys(message.reactions).forEach(function(emoji) {
                     console.log('emoji ', emoji);
                     if (emoji) {
                         var people = message.reactions[emoji] || [];
                         var processed = self.processReactionEmoji(emoji, people);
                         message.reactionsProcessed[emoji] = processed;
                     }
                 });
             }
             return message;
         },
         markdownToHtml: function(markdown, message) {
             markdown = markdown.replace(/</g, '&lt;');
             markdown = markdown.replace(/>/g, '&gt;');
             markdown = stallionClubhouseApp.emojiConverter.replace_emoticons_with_colons(markdown);
             var html = stallionClubhouseApp.emojiConverter.replace_colons(markdown);
             var converter = new showdown.Converter({
                 simplifiedAutoLink: true,
                 excludeTrailingPunctuationFromURLs: true,
                 simpleLineBreaks: true
             });
             html = converter.makeHtml(html);

             
             
             if (markdown.length > 0 && markdown.indexOf(':') === 0 && markdown.indexOf(' ') === -1 && markdown.lastIndexOf(':') === markdown.length -1) {
                 if (html.indexOf('<p>') === 0) {
                     html = html.substr(3);
                     html = html.substr(0, html.length - 4);
                 }
                 html = '<span class="big-emoji">' + html + '</span>';
             }
             //return html;
             $(html).find('a').each(function(anchor) {
                 var link = this.getAttribute('href');
                 var iframeId = 'embed-frame-' + generateUUID();
                 if (link.indexOf('//') > -1) {
                     //html += '<iframe  id="' + iframeId + '"></iframe>';
                     //html += '<iframe style="display:none;" id="' + iframeId + '" class="embed-iframe" sandbox="allow-scripts allow-popups" src="https://clubhouse.local/oembed-iframe?embedUrl=' + 
                     //        encodeURIComponent(link) + '&iframeId=' +
                     //        encodeURIComponent(iframeId) +  '"></iframe>';
                 }
             });
             if (message.edited) {
                 html = '<span class="message-edited">(edited)</span>' + html;
             }
             return html;
         },
         showMessageNotificationMaybe: function(incoming, message) {
             var self = this;
             if (incoming.read) {
                 return;
             }
             if (incoming.fromUsername === self.$store.state.user.username) {
                 return;
             }
             if (!incoming.mentioned && !incoming.hereMentioned) {
                 return;
             }
             if (ifvisible.now() && !ifvisible.now('hidden') && !ifvisible.now('idle')) {
                 // We are visible and active, no notification needed
                 return;
             }
             stallionClubhouseApp.sendNotifiction(
                 'Message from ' + incoming.fromUsername,
                 {
                     body: message.text,
                     icon: 'https://www.gravatar.com/avatar/' + self.hashUser(incoming.fromUsername) + '?d=retro',
                     silent: false
                 },
                 'https://clubhouse.local/#/channel/' + self.channelId
             );
         },
         messageTextToJson: function(text) {
             return JSON.stringify({'bodyMarkdown': text});
         },
         mentionsFromText: function(text) {
             var m = {
                 hereMentioned: false,
                 channelMentioned: false,
                 usersMentioned: []
             }
             var re = new RegExp("\@\\w+", "g");
             var match;
             while (match = re.exec(text)) {
                 if (match && match.length === 1) {
                     var name = match[0];
                     if (name === '@here') {
                         m.hereMentioned = true;
                     } else if (name === '@channel' || name === '@everyone') {
                         m.channelMentioned = true;
                     } else {
                         name = name.substr(1);
                         m.usersMentioned.push(name);
                     }
                 }
             }
             return m;
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
             var text = self.newMessage;
             var mentions = self.mentionsFromText(text);
             var messageJson = self.messageTextToJson(self.newMessage);

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
                 messageJson,
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
                             usersMentioned: mentions.usersMentioned,
                             channelMentioned: mentions.channelMentioned,
                             hereMentioned: mentions.hereMentioned
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
             var mentions = self.mentionsFromText(self.newMessage);
             stallion.request({
                 url: '/clubhouse-api/messaging/post-message',
                 method: 'POST',
                 data: {
                     messageJson: JSON.stringify({'bodyMarkdown': self.newMessage}),
                     channelId: self.channelId,
                     usersMentioned: mentions.usersMentioned,
                     channelMentioned: mentions.channelMentioned,
                     hereMentioned: mentions.hereMentioned
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
