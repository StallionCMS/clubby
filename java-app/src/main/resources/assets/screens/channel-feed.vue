<style lang="scss">
 .channel-feed-vue {
     .column-fixed-header {
         .column-header-name {
             margin-top: -4px;
         }
     }

     .channel-right-icon {
         margin-right: 5px;
     }
     
     border-top: 1px solid #4b4c58;
     /*margin-top: -2px;*/

     .open-settings-link, .open-settings-link > .material-icons {
         color: #444;
         text-decoration: none;
         display: flex;
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
         z-index: 100;
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

     .channel-start-message {
         color: #888;
     }
     .channel-loading-div {
         height: 100vh;
         padding-left: 20px;
         padding-top: 60px;
         width: 700px;
         background-color: white;
     }
     .channel-messages {
         padding-left: 20px;
         padding-right: 20px;
         background-color: white;
         overflow: scroll;
         padding-top: 53px;
         padding-bottom: 30px;
     }
     .post-message-box {
         position: fixed;
         bottom: 0px;
         z-index: 100;
         background-color: white;
         padding: 0px;
         padding-top: 0px;
         
         form {
             padding-right: 20px;
             padding-left: 20px;
             padding-bottom: 20px;
             textarea.form-control {
                 width: 100%;
                 border: 3px solid #BBB;
                 padding-right: 30px;
             }
         }
     }

     
     .a-message-outer {
         position: relative;
         margin-bottom: 0px;
         margin-top: .5em;
         .message-html {
             word-break: break-all;
         }
         .message-html p:last-child {
             margin-bottom: 0em;
         }
     }
     .a-message-outer.a-message-with-meta {

     }
     .a-message-outer.a-message-no-meta {
         .a-message-left {
             height: 2px;
             margin-top: 0px;
             margin-bottom: 0px;
             overflow:hidden;
         }
         
     }
     .a-message {
         margin-top: 0em;
         display: flex;
         flex-direction: row;
         width: 100%;
     }
     .a-message-left {
         display: block;
         width: 50px;
         margin-right: 11px;
         clear: left;
         margin-top: 5px;
     }
     .a-message-avatar-wrap {
         width: 40px;
         height: 40px;
         overflow: hidden;
         border-radius: 4px;
         text-align: center;
         border: 1px solid transparent;
     }
     .a-message-avatar {
         max-width: 40px;
         max-height: 40px;
     }
     .a-message-right {
         width: calc(100% - 50px);
     }
     .a-message-outer.a-message-with-meta .a-message-meta {
         margin-top: 4px;
         a.a-message-from {
             color: #333;
             font-weight: 600;
         }
         .a-message-created {
             color: #bbb;
             font-size: 13px;
         }
     }
     .add-reactions-buttons {
         position: absolute;
         right: 0px;
         top: -10px;
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
         border: 1px solid #F3F3F3;
         border-radius: 3px;
         padding: 3px;
         padding-top: 4px;
         font-size: 13px;
         line-height: 14px;
         display: inline-block;
         margin-right: 4px;
         color: #888;
         margin-bottom: 5px;
         margin-top: 5px;
         .emoji-sizer {
             height: 17px;
             width: 17px;
             margin-right: -2px;
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
             line-height: 1em;
             margin-top: 3px;
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
     
 }

 .channel-feed-vue.channel-direct-message {
     .column-header-name {
         font-size: 14px;
     }
 }
</style>

<template>
    <div :class="['channel-feed-vue',  channel && channel.channelType==='DIRECT_MESSAGE' ? 'channel-direct-message' : '']" >
        <div v-if="channel" class="column-fixed-header">
            <div class="column-header-name">
                <a @click="showChannelContextMenu" href="javascript:;" class="open-settings-link">
                    <span class="channel-right-icon hash-icon" v-if="!channel.inviteOnly">&nbsp;#</span>
                    <span class="channel-right-icon" v-if="channel.inviteOnly && !isEncrypted"><i class="material-icons">lock</i></span>
                    <span class="channel-right-icon" v-if="channel.inviteOnly && isEncrypted"><i class="material-icons">enhanced_encryption</i></span>
                    <div>{{ channelName }}<i class="material-icons">keyboard_arrow_down</i></div>
                </a>
            </div>
            <div v-if="contextMenuShown" class="channel-context-menu">
                <a href="javascript:;" @click="toggleFavorite"><span v-if="!channel.favorite">Make Favorite</span><span v-if="channel.favorite">Unfavorite</span></a>
                <a :href="'#/channel-members/' + channelId">View channel members ({{ members ? members.length : '-' }} members)</a>
                <a v-if="isChannelOwner && (channel.channelType==='CHANNEL' || channel.channelType==='FORUM')" class="column-header-icon" :href="'#/channel-settings/' + channelId">Settings</a>
                
            </div>
            <!-- <div class="column-header-search">
                <input type="text" placeholder="Search" class="form-control">
            </div>-->                        
        </div>
        <div class="channel-loading-div" v-if="!isLoaded">
            Loading messages...
        </div>
        <div class="channel-right-panel">
        </div>
        <div class="channel-messages always-show-scrollbars" style="">
            <div v-if="allLoaded">
                <div v-if="!hasPrevious" class="channel-start-message">
                    <h5>This is the begining of the "{{channelName}}" channel.</h5>
                </div>
                <div v-if="messages.length === 0" class="p" style="margin-top: 40px;color:#666;">
                    No messages yet in this channel.
                </div>
                <div :class="['a-message-outer', message.showUser ? 'a-message-with-meta' : 'a-message-no-meta']" v-for="message in messages"  :key="message.id" v-if="!message.deleted">
                    <div class="add-reactions-buttons">
                        <a href="javascript:;" @click="openAddReaction($event, message)"><i class="material-icons">tag_faces</i></a><a v-if="message.fromUsername===$store.state.user.username" href="javascript:;" @click="openEditMessage(message)"><i class="material-icons">mode_edit</i></a><a v-if="isChannelOwner || message.fromUsername===$store.state.user.username" href="javascript:;" @click="openDeleteModal(message)"><i class="material-icons">delete</i></a>
                    </div>
                    <div class="a-message" :id="'channel-message-' + message.id">
                        <div class="a-message-left">
                            <div class="a-message-avatar-wrap"><img v-if="message.showUser" class="a-message-avatar" :src="getAvatarUrl(message.fromUserId)"></div>
                        </div>
                        <div class="a-message-right">
                            <div class="a-message-meta">
                                <div v-if="message.showUser">
                                    <a :href="'#/profile/' + message.fromUserId" class="a-message-from">{{ message.fromUsername }}</a>
                                    <span class="a-message-created">{{ message.createdAtFormatted }}</span>
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
            </div>
        </div>
        <div v-if="channel" class="post-message-box">
            <form @submit.prevent="postMessage">
                <div class="wrap-insert-emoji-button">
                    <a class="insert-emoji-button" @click="openInsertEmoji" href="javascript:;"><i class="material-icons">tag_faces</i></a>
                </div>
                <message-textarea :placeholder="channel && channel.channelType === 'DIRECT_MESSAGE' ? channelName : 'Message ' + '#' + channelName" id="post-message-box" :disabled="messageAreaDisabled || !publicKeysAvailable" @submit="postMessage" v-model="newMessage"></message-textarea>
                
                <!--<input type="text" class="form-control" v-model="newMessage">-->
            </form>
        </div>
        <div class="p">&nbsp;</div>
        <emoji-popup @input="insertEmoji" ref="messageemojipopup"></emoji-popup>
        <emoji-popup @input="onChooseReactionEmoji" @close="onCloseReactionEmoji" ref="emojipopup"></emoji-popup>
        <delete-message-modal v-if="showDeleteModal && messageToDelete" @close="showDeleteModal=false;messageToDelete=null" @delete="onDeleteMessage" :message="messageToDelete"></delete-message-modal>
    </div>
</template>

<script>
 module.exports = {
     mixins: [ClubhouseMessagingMixin],
     data: function() {
         return {
             contextMenuShown: false
         }
     },
     watch: {
        
     },     
     computed: {
        
     },
     mounted: function() {
         var self = this;
         document.addEventListener('scroll', self.onScroll);
         /*
         $(this.$el).find('.channel-messages').scroll(function(e) {
             var el = this;
             debugger;
             if (el.scrollTop === 0 && self.hasPrevious) {
                 console.log('load new page!');
                 self.fetchPage(self.page + 1);
             }
         });*/
     },
     beforeDestroy: function() {
         document.removeEventListener('click', this.hideChannelContextMenu);
         document.removeEventListener('scroll', this.onScroll);
     },
     methods: {
         onScroll: function() {
             var self = this;
             if (window.scrollY === 0 && self.hasPrevious) {
                 window.scrollTo(1, document.body.scrollHeight);
                 self.fetchPage(self.page + 1);
             }
         },
         getAvatarUrl: function(userId) {
             var self = this;
             if (self.$store.state.allUsersById && self.$store.state.allUsersById[userId]) {
                 return self.$store.state.allUsersById[userId].avatarUrl
             }
             return 'https://www.gravatar.com/avatar/' + md5(userId + '') + '?f=y&d=identicon';
         },
         afterFetchingFinished: function() {
             var self = this;
             var $div = $(self.$el).find('.channel-messages');
             var div = $div.get(0);
             if (!div) {
                 return;
             }
             if (self.page <=1) {
                 //div.scrollTop = div.scrollHeight + 200;
                 //div.scrollTop = div.scrollHeight + 200;
                 window.scrollTo(0,document.body.scrollHeight);
                 Vue.nextTick(function() {
                     if (!ClubhouseMobileInterop.isMobile) {
                         $('#post-message-box').focus();
                     }
                 });
             } else if (self.scrollToMessageId) {
                 var $msg = $('#channel-message-' + self.scrollToMessageId);
                 var animateTo = $msg.offset().top - 300;
                 var newScrollTop = $msg.offset().top;
                 div.scrollTop = newScrollTop;
                 console.log('scrollTop ', newScrollTop, animateTo);
                 $div.animate({ scrollTop: animateTo}, 1200);
             } 
         },
         afterIncomingMessage: function() {
             var self = this;
             if (self.pages && self.pages.length) {
                 
             } else {
                 var div = $(self.$el).find('.channel-messages').get(0);
                 var $div = $(div);
                 var $message = $div.find('#channel-message-' + self.messages[self.messages.length-1].id);
                 var distance = div.scrollHeight - div.scrollTop - $div.height() - $message.height();
                 console.log('distance ', distance);
                 if (distance < 150) {
                     div.scrollTop = div.scrollHeight + 200;
                 }
             }
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
 }
</script>

