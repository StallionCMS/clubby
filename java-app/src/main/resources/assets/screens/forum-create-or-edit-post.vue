
<style lang="scss">
 .forum-new-thread-vue {
     background-color: white;
     min-height: 100vh;
     border-top: 1px solid transparent;
     .fixed-header {
         background-color: white;
         z-index: 100;
         border-bottom: 2px solid #ddd;
         position: fixed;
         width: 100%;
         padding: .5em 20px .5em 20px;
         .breadcrumbs {
             font-size: 16px;
         }
     }
     .one-column-screen {
         margin-top: 55px;
     }
     .md-editor textarea {
         height: 60vh;
     }
 }
</style>


<template>
    <div class="forum-new-thread-vue">
        <div class="fixed-header">
            <div v-if="channel" class="breadcrumbs">
                <a :href="'#/forum/' + channel.id">{{ channel.name }}</a> &#8250;
                <span v-if="isNew">New Thread</span>
                <span v-if="!isNew">Edit Post</span>
            </div>
        </div>        
        <div class="one-column-screen">
            <div v-if="isLoading"><loading-div></loading-div></div>
            <div v-if="!isLoading">
                <div v-if="hasTitle" class="form-group">
                    <label>Thread Title</label>
                    <input type="input" class="form-control" v-model="title">
                </div>
                <div class="p">
                    <forum-text-editor :config="config" ref="editor" @input-debounced="onInput"  :original-content="originalContent" :widgets="widgets"></forum-text-editor>
                </div>
                <div class="p">
                    <a v-if="isNew && hasTitle" :disabled="!canSubmit" class="btn btn-primary" href="javascript:;" @click="saveChanges">Create Thread</a>
                    <a v-if="isNew && !hasTitle" :disabled="!canSubmit" class="btn btn-primary" href="javascript:;" @click="saveChanges">Create Thread</a>
                    <a v-if="!isNew" :disabled="!canSubmit" class="btn btn-primary" href="javascript:;" @click="saveChanges">Save Changes</a>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     mixins: [ClubhouseMessageSendingMixin],
     props: {
         message: {
             required: false,
             type: Object
         },
         theChannel: {
             required: false,
             type: Object
         },
         theMembers: {
             required: false,
             type: Array
         }
     },
     data: function() {
         var self = this;
         var d = {
             isLoading: true,
             isNew: true,
             canSubmit: false,
             channelId: 0,
             members: [],
             channel: {},
             hasTitle: true,
             messageId: 0,
             parentMessageId: null,
             threadId: null,
             title: '',
             originalContent: '',
             widgets: [],
             config: {}
         };
         if (this.message) {
             d.widgets = JSON.parse(JSON.stringify(this.message.widgets || []));
             d.channelId = this.theChannel.id;
             d.messageId = this.message.id;
             d.parentMessageId = this.message.parentMessageId;
             //d.threadId = this.message.threadId;
             d.title = this.message.title;
             d.hasTitle = d.title || !this.message.parentMessageId;
             if (!d.hasTitle) {
                 d.config.autofocus = true;
             }
             d.isNew = false;
             d.originalContent = this.message.text;
             d.isLoading = false
             d.channel = this.theChannel;
             d.members = this.theMembers;
             d.canSubmit = true;
         }
         return d;
     },
     created: function() {
         if (!this.message) {
             this.onRoute();
         }
     },
     watch: {
         '$route': 'onRoute'
     },
     methods: {
         onRoute: function() {
             var self = this;
             var self = this;
             self.canSubmit = false;
             self.channelId = parseInt(this.$route.params.channelId, 10) || 0;
             self.messageId = parseInt(this.$route.params.messageId, 10) || 0;
             if (!self.messageId) {
                 if (ClubhouseMessageAutoSaver.isInProgress(self.channelId, 0)) {
                     var recent = ClubhouseMessageAutoSaver.loadRecentAutoSaves(self.channelId, 0);
                     if (recent !== null) {
                         self.originalContent = recent.text;
                         self.widgets = recent.widgets;
                     }
                 }
             }
             self.fetchData();
         },
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/messaging/forum-post-editor-context?channelId=' + self.channelId + '&messageId=' + self.messageId,
                 success: function(o) {
                     self.members = o.members;
                     self.channel = o.channel;
                     self.isLoading = false;
                     self.afterFetchedData(self.channel, self.members);
                 }
             });
         },
         saveChanges: function() {
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
                 message: self.message,
                 messageId: self.message && self.message.id ? self.message.id : 0,
                 originalContent: self.originalContent,
                 parentMessageId: self.parentMessageId,
                 threadId: self.parentMessageId,
                 title: self.title || '',
                 widgets: self.widgets,
                 success: function(message) {
                     if (self.message) {
                         self.$emit('close');                     
                         message = self.message;
                         return;
                     }
                     var path = '#/forum/' + message.channelId + '/' + message.threadId;
                     if (message.id !== message.threadId) {
                         path += '?messageId=' + message.id;
                     }
                     self.$emit('close');
                     ClubhouseMessageAutoSaver.clearInProgress(self.channelId, 0);
                     window.location.hash = path;
                     
                 }
             });
             
         },
         onInput: function(editor) {
             var self = this;
             var data = editor.getData();
             self.widgets = data.widgets;
             self.originalContent = data.originalContent;
             console.log('inasdf on input!!');
             ClubhouseMessageAutoSaver.autoSave(self.channelId, 0, self.originalContent, self.widgets);
         }
     }
 };
</script>
