
<style lang="scss">

 .forum-thread-vue {
     
     .column-fixed-header {
         margin-top: -1px;
     }
     .thread-fixed-footer {
         height: 50px;
         position: fixed;
         width: 100vw;
         left: 0px;
         bottom: 0px;
         background-color: white;
         border-top: 1px solid #999;
         padding: 5px;
         z-index: 1;
     }
     .thread-fixed-footer.footer-expanded {
         height: 300px;
     }
     
     .topic-body {
         background-color: white;
         margin-top: 0px;
         padding-top: 50px;
         padding-left: 20px;
         padding-right: 20px;
         width: 100%;
         overflow: scroll;
         position: relative;
         border-top: 1px solid transparent;
     }
     > div {
         border-bottom: 1px solid transparent;
     }
     .topic-messages, .topic-post {
         
         max-width: 750px;
     }
     


     .a-message {
         margin-top: 0em;
         width: 100%;
     }
     .a-message-header {
         display: flex;
         flex-direction: row;
         width: 100%;
     }
     .a-message-body-wrap {
         display: block;
         .message-html {
             padding-top: 10px;
         }
     }

     .a-message-left {
         display: block;
         width: 50px;
         margin-right: 2px;
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
         width: 650px;
     }
     .a-message-outer {
         border-top: 1px solid #F0F0F0;
         padding-top: 6px;
         position: relative;
         padding-bottom: 30px;
     }
     .a-message-outer.a-message-first {
         border-top: 1px solid transparent;
     }
     .a-message-permalink {
         color: #bbb;
         float: right;
     }
     .a-message-outer.a-message-with-meta .a-message-meta {
         display: block;
         width: calc(100% - 61px);
         margin-top: 2px;
         a.a-message-from {
             color: #333;
             font-weight: 600;
         }
         .a-message-created {
             color: #bbb;
             font-size: 13px;
         }
     }
     .wiki-style-explanation {
         color: #BBB;
         font-weight: 500;
         font-style: italic;
     }
     .add-reactions-buttons {
         position: absolute;
         left: 60px;
         bottom: 24px;
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
             /*background-color: #E9E9E9;*/
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
     .post-message-box {
         margin-top: 1em;
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
     .resettingOverlay {
         width: 100%;
         height: 100%;
         position: fixed;
         left: 200;
         top: 0;
         z-index: 100;
         opacity: .7;
         background-color: #F9F9F9;
         h1 {
             margin-top: 20%;
             margin-left: 30%;
         }
     }
     .topic-watch-toggle {
         .material-icons.watched-on {
             vertical-align: -30%;
             color: #eae080 !important;
         }
         .material-icons.watched-off {
             vertical-align: -30%;
             color: #999;
         }
     }
     .material-icons.channel-favorite {
         float: right;
         margin-right: 20px;
         color: #999 !important;
         cursor: pointer;
     }

     .slider-outer-wrapper {
         position:relative;
     }
     .slider-wrapper {
         z-index: 10;
         position: fixed;
         right:90px;
         top: 75px;         
     }
     .slider-numbers {
         padding: 10px;
         line-height: 1em;
         text-align: center;
         display: block;
         width: 100vw;
         text-decoration: none;
         font-size: 18px;
         .material-icons {
             vertical-align: -25%;
             font-size: 20px;
             margin-left: -2px;
         }
     }
     .expand-upward-link {
         display: inline-block;
         font-size: 24px;
         border: 1px solid #888;
         text-decoration: none;
         padding: 6px;
         border-radius: 4px;
         float: right;
         width: 40px;
         height: 40px;
         text-align: center;
         
     }

     
 }



 @media (max-width: 700px) {

     .topic-body {
         height: calc(100vh - 100px);
     }
      .forum-thread-vue {
          .slider-wrapper {
              border-top: 1px solid #ccc;
              background-color: white;
              width: 100vw;
              height: 50px;
              top: auto;
              bottom: 0px;
              right: 0px;
              .slider-visibility-wrapper {
                  display: none;
                  padding-left: 50%;
              }
          }

          .expand-link-wrapper {
              display: block;
              padding-top: 5px;
          }

          
          .slider-wrapper.footer-expanded {
              height: 300px;
              .slider-visibility-wrapper {
                  display: block;
              }
          }
      }
 }
 
 @media (min-width: 700px) {
     .topic-messages, .topic-post {
         width: calc(100% - 100px);
     }
     
     .expand-link-wrapper {
         display: none;
     }
     .thread-fixed-footer {
         display: none;
         height: calc(100vh - 100px);
     }
 } 
 
 
</style>


<template>
    <div :class="['forum-thread-vue', isMobile ? 'mobile-view' : '']">
        <div v-if="!isLoaded"><loading-div></loading-div></div>
        <div v-if="resetting" class="resettingOverlay">
            <h1>Loading...</h1>
        </div>
        <div v-if="isLoaded && channel" >
            <div class="column-fixed-header">
                <div class="name-row"><a class="breadcrumb-link" :href="'#/forum/' + channel.id"><i v-if="channel.encrypted" class="material-icons">security</i> <i v-if="!channel.encrypted && channel.inviteOnly" class="material-icons">lock</i> {{ channel.name }}</a> &#8250;
                    {{ topic.title }}
                </div>
                <div class="header-meta">
                    Created {{ formatFullDate(topic.createdAt) }} by {{ topic.fromUsername }}.
                    <a title="Toggle watching this thread" class="topic-watch-toggle" href="javascript:;" @click="toggleWatched"><i v-if="topic.watched" class="material-icons watched-on">visibility</i><i v-if="!topic.watched" class="material-icons watched-off">visibility_off</i></a>
                </div>
            </div>
            <div class="slider-outer-wrapper" >
                <div :class="['slider-wrapper', expanded ? 'footer-expanded' : '']" class="slider-wrapper">
                    <div class="expand-link-wrapper">
                        <a @click="expanded = !expanded" href="javascript:;" class="slider-numbers">{{ $refs.messageslider ? $refs.messageslider.viewingMessageIndex + 1 : 1 }} of {{ allIdsDates.length }} posts <i class="material-icons">swap_vert</i></a>
                    </div>
                    <div class="slider-visibility-wrapper">
                        <forum-messages-slider ref="messageslider" v-if="allIdsDates && pages.length > 1" :viewing-message="viewingMessage" :viewing-message-index="viewingMessageIndex" :messages-id-dates="allIdsDates" @scrollpost="onSliderScrollToPost" ></forum-messages-slider>
                    </div>
                </div>
            </div>
            <div class="topic-body">
                <div class="topic-messages channel-messages" @click="expanded=false">
                    <div class="wrap-loading"><loading-div v-if="beginningLoading"></loading-div></div>
                    <template v-for="(pageMessages, pIndex) in pages" v-if="pageMessages != null">
                        <div :class="['a-message-outer', 'a-message-with-meta', message.$index===0 ? 'a-message-first':'']" v-for="(message, mIndex) in pageMessages" :data-message-id="message.id" :data-thread-index="message.threadIndex" :key="message.id" v-if="!message.deleted">
                            <div class="add-reactions-buttons">
                                <a href="javascript:;" @click="openAddReaction($event, message)"><i class="material-icons">tag_faces</i></a><a v-if="message.fromUsername===$store.state.user.username || (channel.wikiStyle && message.threadId === message.id)" href="javascript:;" @click="openEditMessage(pIndex, mIndex)"><i class="material-icons">mode_edit</i></a><a v-if="isChannelOwner || message.fromUsername===$store.state.user.username" href="javascript:;" @click="openDeleteModal(message)"><i class="material-icons">delete</i></a>
                            </div>
                            <div class="a-message" :id="'channel-message-' + message.id">
                                <div v-if="channel.wikiStyle && message.id === message.threadId" class="wiki-style-explanation a-message-header">This is a wiki-style post created by {{ message.fromUsername }} on {{ message.createdAtFullFormatted }} and last edited by {{ topic.lastEditedTopicUsername }}</div>
                                <div v-else class="a-message-header">
                                    <div class="a-message-left">
                                        <div class="a-message-avatar-wrap"><img class="a-message-avatar" :src="$store.state.allUsersById[message.fromUserId].avatarUrl"></div>
                                    </div>
                                    <div class="a-message-meta">
                                        <div>
                                            <a class="a-message-permalink" :href="'#/forum/' + channelId + '/' + threadId + '?messageId=' + message.id">#{{ message.threadIndex + 1 }}</a>
                                            <span><a :href="'#/user/' + message.fromUserId" class="a-message-from">{{ message.fromUsername }}</a></span>
                                            <span class="a-message-created">{{ message.createdAtFullFormatted }}</span> 
                                        </div>
                                    </div>
                                </div><!-- a-message-header -->
                                <div class="a-message-body-wrap">
                                    <div v-if="!message.editing && message.html">
                                        <div class="message-html" v-raw-html="message.html"></div>
                                    </div>
                                    <div class="message-reactions">
                                        <a @click="toggleReaction(message, data)" :title="data.title" :class="{'a-reaction': true, 'a-reaction-active': data.currentUserReacted}" v-for="data, emoji in message.reactionsProcessed" v-show="data.count > 0"><span v-raw-html="data.sprite"></span> &nbsp;{{ data.count }}</a>
                                    </div>
                                </div>
                            </div>                    
                        </div>
                    </template>
                    <div class="wrap-loading"><loading-div v-if="endLoading"></loading-div></div>
                </div>
                <div class="topic-post" v-if="pages[pages.length-1]!==null">
                    <hr style="margin-bottom: 1em;">
                    <div style="color: #777; margin-top: 0px; margin-bottom: 1em;">Post a new comment to this thread</div>
                    <forum-text-editor ref="editor" @input-debounced="onReplyTextInput" :original-content="replyMarkdown" :widgets="replyWidgets"></forum-text-editor>
                    <div class="form-group p">
                        <button tabindex="2" class="btn btn-primary btn-submit" @click="submitReply">Post Reply</button>
                    </div>
                </div>
                <div v-if="editingMessage && members.length">
                    <thread-edit-modal v-if="editingMessage" @close="editingMessage=null" :message="editingMessage" :channel="channel" :members="members"></thread-edit-modal>
                </div>
                <emoji-popup @input="onChooseReactionEmoji" @close="onCloseReactionEmoji" ref="emojipopup"></emoji-popup>
                
            </div>
        </div>
        <delete-message-modal v-if="showDeleteModal && messageToDelete" @close="showDeleteModal=false;messageToDelete=null" @delete="onDeleteMessage" :message="messageToDelete"></delete-message-modal>
    </div>
</template>

<script>
 module.exports = {
     mixins: [ClubhouseMessagingMixin, ClubhouseMessageSendingMixin],
     data: function() {
         return {
             beginningLoading: false,
             currentOffsetIndex: 0,
             editingMessage: null,
             editingChannel: null,
             endLoading: false,
             expanded: false,
             isFirstFetch: true,
             isMobile: $("body").width() < 700,
             lastScrollTop: 0,
             loadOnScroll: false,
             messageToDelete: null,
             offsets: [],
             offsetByThreadIndex: {},
             replyWidgets: [],
             replyMarkdown: '',
             resetting: false,
             showDeleteModal: false,
             threadIndexPosition: {index: 0, position: 0},
             threadIndexToGoToAfterPageLoad: null,
             viewingMessage: {},
             viewingMessageIndex: 0,
         };
     },
     created: function() {
         //this.onRoute();
     },
     watch: {
        // '$route': 'onRoute'
     },
     mounted: function() {
         var self = this;
     },
     methods: {
         openEditMessage: function(pIndex, mIndex) {
             this.editingMessage = this.pages[pIndex][mIndex];
         },
         onReplyTextInput: function(editor) {
             var self = this;
             var d = editor.getData();
             this.replyWidgets = d.widgets;
             this.replyMarkdown = d.originalContent;
             ClubhouseMessageAutoSaver.autoSave(self.channelId, self.threadId, 0, self.replyMarkdown, self.replyWidgets);
         },
         submitReply: function() {
             var self = this;
             if (!this.$refs.editor) {
                 console.log('editor has disappeared');
                 return;
             }
             var d = this.$refs.editor.getData();
             if (d == null) {
                 console.log('editor content was null');
                 return;
             }
             self.widgets = d.widgets;
             self.originalContent = d.originalContent;
             this.postMessage({
                 channelId: self.channelId,
                 channelMembers: self.members,                 
                 encrypted: self.channel.encrypted,                 
                 originalContent: self.originalContent,
                 parentMessageId: self.threadId,
                 threadId: self.threadId,
                 title: self.title,
                 widgets: self.widgets,
                 success: function(message) {
                     ClubhouseMessageAutoSaver.clearInProgress(self.channelId, self.threadId, 0);
                     self.originalContent = '';
                     self.widgets = [];
                     self.replyMarkdown = '';
                     self.replyWidgets = [];
                     
                     window.location.hash = '#/forum/' + self.channelId + '/' + self.threadId + '?messageId=' + message.id;
                     self.isLoaded = false;
                     
                     
                 }
             });
             
             
         },
         onSliderScrollToPost: function(newIndex) {
             var self = this;
             self.goToThreadIndex(newIndex);
         },
         onAfterFetchingFinished: function() {
             var self = this;
             console.log('set threadIndex for number of pages ', this.pages.length);
             
             for (var p = 0; p<this.pages.length;p++) {
                 var pageMessages = this.pages[p];
                 if (pageMessages === null) {
                     continue;
                 }
                 for(var m = 0; m<pageMessages.length;m++) {
                     var index = (p * this.pageSize) + m;
                     pageMessages[m].threadIndex = index;
                 }
             }
             if (ClubhouseMessageAutoSaver.isInProgress(self.channelId, self.threadId, 0)) {
                 var recent = ClubhouseMessageAutoSaver.loadRecentAutoSaves(self.channelId, self.threadId, 0);
                 if (recent !== null) {
                     self.replyMarkdown = recent.text;
                     self.replyWidgets = recent.widgets;
                 }
             }
             console.log('tick 0');
             Vue.nextTick(function() {
                 console.log('tick 1');
                 Vue.nextTick(self.onAfterFetchingNextTick);
             });
         },
         onAfterFetchingNextTick: function() {
             var self = this;
             console.log('onAfterFetchingNextTick');
             var $div = $(this.$el).find('.topic-body');
             
             
             var offsets = [];
             this.offsetByThreadIndex = {};
             $div.find('.a-message-outer').each(function() {
                 var threadIndex = parseInt($(this).attr('data-thread-index'), 10);
                 offsets.push([this.offsetTop, threadIndex]);
                 self.offsetByThreadIndex[threadIndex] = this.offsetTop;
                 //console.log(this.offsetTop);
             });
             this.offsets = offsets;
             this.viewingMessage = this.messages[0];
             if (self.isFirstFetch) {
                 //$div.scroll(this.onScroll);
                 $(window).scroll(this.onScroll);
                 self.isFirstFetch = false;
                 if (this.$route.query.messageId) {
                     self.goToId(parseInt(this.$route.query.messageId, 10));
                     return;
                 }
             } else {
                 if (self.currentThreadIndexPosition) {
                     //setTimeout(function() {
                         var offset = self.offsetByThreadIndex[self.currentThreadIndexPosition.index];
                     //$div.scrollTop(offset-self.currentThreadIndexPosition.position);
                         $(window).scrollTop(offset-self.currentThreadIndexPosition.position);
                     //}, 300);
                 }
             }

             

             setTimeout(function() {
                 self.beginningLoading = false;
                 self.endLoading = false;
                 self.loadOnScroll = true;
                 self.resetting = false;
             }, 200);
             
         },
         onScroll: function(evt) {
             var self = this;
             var lastScrollTop = self.lastScrollTop
             var thisScrollTop = $(window).scrollTop();//$(this.$el).find('.topic-body').scrollTop();
             self.lastScrollTop = thisScrollTop;
             this.checkScrollLoadNewPageMaybe(lastScrollTop);
             if (!self.loadOnScroll) {
                 return;
             }
             var i = parseInt(self.offsets.length / 2, 10);
             if (i < 2) {
                 return;
             }
             if (thisScrollTop === lastScrollTop) {
                 console.log('thisScrollTop unchanged ', thisScrollTop);
                 return;
             }
             var scrollingDown = true;
             if (thisScrollTop < lastScrollTop) {
                 scrollingDown = false;
             }
             
             var $div = $(this.$el).find('.topic-body');
             var min = thisScrollTop + 70;
             var max = min + $div.height();
             var upperBound = self.offsets.length;
             var lowerBound = -1;
             console.log(min, max);
             //var index = 0;
             //var k = 0;
             var activeThreadIndex = this.viewingMessageIndex;
             //var currentOffsetIndex = this.currentOffsetIndex;
             //var currentMessageOffset = this.offsets[currentOffsetIndex][0];
             //var newOffsetIndex = ;
             var newThreadIndex = activeThreadIndex;
             var maxThreadIndex = this.pages.length * this.pageSize;
             //var currentMessageOffset = this.offsetByThreadIndex[activeThreadIndex];
             //if (currentMessageOffset === undefined) {
             //    return;
             //}
             //console.log('currentMessageOffset ', currentMessageOffset, 'min ', min);
             if (scrollingDown) {
                 console.log('scrolling down', activeThreadIndex);
                 if ((activeThreadIndex + 1) === maxThreadIndex) {
                     return;
                 }
                 for(var i = activeThreadIndex; i<maxThreadIndex; i++) {
                     var offset = self.offsetByThreadIndex[i];
                     if (offset === undefined) {
                         break;
                     }
                     if (offset > max) {
                         break;
                     }
                     //var offset = pair[0];
                     //var threadIndex = pair[1];
                     //newOffsetIndex = i;
                     newThreadIndex = i;
                 }
             } else if (activeThreadIndex === 0) {
                 console.log('at top');
                 return;
             } else {
                 console.log('scrolling up', activeThreadIndex);
                 for(var i = activeThreadIndex; i>=0; i--) {
                     var offset = self.offsetByThreadIndex[i];
                     if (offset === undefined) {
                         break;
                     }
                     if (offset < min) {
                         break;
                     }
                     newThreadIndex = i;
                 }
             }
             //this.currentOffsetIndex = newOffsetIndex;
             this.viewingMessageIndex = newThreadIndex;
             //console.log('find message for ', min, max);
             /*
             for (var m = 0; m < self.offsets.length; m++) {
                 var pair = self.offsets[i];
                 var offset = pair[0];
                 var threadIndex = pair[1];
                 console.log('i ', i, 'offset ', offset, 'ti', threadIndex);
                 if (offset >= min && offset <= max) {
                     index = i;
                     activeThreadIndex = threadIndex;
                     console.log('new i achieved ', i, activeThreadIndex);
                     break;
                 }
                 if (offset < min) {
                     lowerBound = i;
                     k = parseInt((lowerBound + upperBound) / 2, 10);
                 } else if (offset > max) {
                     upperBound = i;
                     k = parseInt((lowerBound + upperBound) / 2, 10);
                 }
                 console.log('lower ', lowerBound, 'upper', upperBound, 'k ', k, 'i ', i);
                 if (k === i) {
                     console.log('repeat k');
                     break;
                 }
                 i = k;

                 console.log('new i', i);
                 if (m === (self.offsets.length-1)) {
                     console.log('too many itereations');
                 }
             }
             */
             //this.viewingMessageIndex = activeThreadIndex;
             
         },
         toggleWatched: function() {
             var self = this;
             var watched = !this.topic.watched;
             stallion.request({
                 url: '/clubhouse-api/messaging/toggle-watched',
                 method: 'POST',
                 data: {
                     messageId: this.topic.id,
                     watched: watched
                 },
                 success: function(o) {
                     self.topic.watched = watched;
                 }
             });
         },
         checkScrollLoadNewPageMaybe: function(lastScrollTop) {
             var self = this;
             if (!self.loadOnScroll) {
                 return;
             }

             var self = this;
             var $div = $(window);//$(this.$el).find('.topic-body');
             var movingDown = true;
             if ($div.scrollTop() === lastScrollTop) {
                 console.log('repeat scroll top');
                 return;
             }
             
             var currentPageIndex = self.page - 1;

             this.currentThreadIndexPosition = {
                 index: self.viewingMessageIndex,
                 position: self.offsetByThreadIndex[self.viewingMessageIndex] - $div.scrollTop()
             }
             
             if ($div.scrollTop() < 100) {
                 for (var i=0;i<self.pages.length;i++) {
                     if (i === 0 && self.pages[i] !== null) {
                         console.log('at beginning');
                         break;
                     } else if (self.pages[i] !== null) {
                         console.log('fetch previous page ', i);
                         self.loadOnScroll = false;
                         self.beginningLoading = true;
                         self.fetchPage(i);
                         break;
                     }
                 }
             } else {
                 var fromBottom = $div.find('.topic-messages').height() - $div.scrollTop() - $div.height();
                 console.log('fromBottom ', fromBottom, currentPageIndex, self.pages.length);
                 if (fromBottom < 500 && currentPageIndex < (self.pages.length - 1)) {
                     self.loadOnScroll = false;
                     self.endLoading = true;
                     for (var i=self.pages.length-1;i>=0;i--) {
                         if (i === (self.pages.length-1) && self.pages[i] !== null) {
                             console.log('at end');
                             break;
                         } else if (self.pages[i] !== null) {
                             console.log('fetch next page ', i+2);
                             self.loadOnScroll = false;
                             self.fetchPage(i+2);
                             break;
                         }
                     }
                 }
             }
             


         },
         goToId: function(messageId) {
             var self = this;
             messageId = parseInt(messageId, 10);
             var threadIndex = null;
             for(var x = 0; x<this.allIdsDates.length; x++) {
                 var pair = this.allIdsDates[x];
                 if (pair[0] === messageId) {
                     threadIndex = x;
                 }
             }
             if (threadIndex !== null) {
                 self.goToThreadIndex(threadIndex);
             }
         },
         goToThreadIndex: function(newThreadIndex) {
             var self = this;
             for (var p = 0; p < self.pages.length; p++) {
                 var page = p + 1;
                 if (page * self.pageSize > newThreadIndex) {
                     if (self.pages[p] === null) {
                         self.threadIndexToGoToAfterPageLoad = newThreadIndex;
                         self.loadOnScroll = false;
                         self.currentThreadIndexPosition = {
                             index: newThreadIndex,
                             position: 200
                         }
                         self.viewingMessageIndex = newThreadIndex;
                         self.resetting = true;
                         self.fetchPage(page);
                     } else {
                         var offset = self.offsetByThreadIndex[newThreadIndex];
                         $(this.$el).find('.topic-body').scrollTop(offset-100);
                     }
                 }
             }
         },
         
         /*
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
         */
     }
 };
</script>
