var ClubhouseMessagingMixin = {
    data: function() {
        return {
            allIdsDates: [],
            channel: null,
            channelId: null,
            channelName: '',
            fetching: false,
            hasMore: false,
            hasNext: false,
            hasPrevious: false,
            isLoaded: false,
            isChannelOwner: false,
            isEncrypted: null,
            messageAreaDisabled: false,
            messages: [],
            messagesDecrypted: [],
            messageToDelete: null,
            page: 1,
            pageSize: 50,
            pages: [],
            publicKeysAvailable: false,
            newMessage: '',
            reactToMessage: null,
            scrollToMessageId: '',
            showDeleteModal: false,
            threadId: 0,
            topic: null,
        }
    },
    created: function() {
        this.onRoute();
    },
    watch: {
        '$route': 'onRoute',
    },
    methods: {
        /////// LOADING CHANNEL & MESSAGES
        onRoute: function() {
            var self = this;
            localStorage.lastChannelPath = window.location.hash;
            ClubhouseVueApp.currentChannelComponent = this;
            self.hasMore = false;
            self.messages = [];

            self.hasNext = false;
            self.hasPrevious = false;
            self.isLoaded = false;
            self.isFirstFetch = true;
            self.messagesDecrypted = false;
            self.channelId = parseInt(self.$route.params.channelId);
            self.threadId = parseInt(self.$route.params.threadId, 10) || 0;

            self.channel = self.$store.state.channelById[self.channelId] || null;
            if (self.channel) {
                self.channelName = self.channel.name;
            } else {
                self.channelName = '';
            }
            self.members = null;
            self.page = 1;
            self.pages = [];
            self.topic = {};
            self.fetchPage(self.page);
            
        },
        refresh: function() {
            console.log('refresh channel feed');
            // refresh endpoint
            // Find all messages from (startpoint and greater)
            // If any changed, update it
        },
        fetchNextPage: function() {
            this.fetchPage(self.page + 1);
        },
        fetchPreviousPage: function() {
            this.fetchPage(self.page - 1);
        },
        fetchPage: function(page) {
            var self = this;
            if (page === undefined) {
                page = self.page;
            }
            var self = this;
            if (self.fetching) {
                return;
            }
            self.fetching = true;
            stallion.request({
                url: '/clubhouse-api/messaging/channel-messages-context/' + self.channelId + '?page=' + page + '&threadId=' + self.threadId + '&pageSize=' + self.pageSize, 
                success: function(o) {
                    var pageIndex = page - 1;
                    self.isChannelOwner = o.channelMembership.owner;
                    self.fetching = false;
                    self.page = page;
                    self.topic = o.topic || {};
                    self.allIdsDates = o.allIdsDates;
                    if (o.messages.length >= self.pageSize) {
                        self.hasPrevious = true;
                    } else {
                        self.hasPrevious = false;
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
                        message.html = self.markdownToHtml(info.bodyMarkdown || '', info, message);
                        message.text = info.bodyMarkdown;
                    });

                    // If we are tracking the total number of pages, store messages to the
                    // paging array
                    if (o.totalPageCount) {
                        while (self.pages.length < o.totalPageCount) {
                            self.pages.push(null);
                        }
                        console.log('page ', page, 'pageIndex ', pageIndex);
                        self.pages[pageIndex] = messages;
                        for (var i = 0; i<self.pages.length; i++) {
                            if (i < (pageIndex-1) || i > (pageIndex+1)) {
                                self.pages[i] = null;
                            }
                        }
                        self.hasPrevious = false;
                        self.hasNext = false;
                        if (self.pages.length > 0) {
                            if (self.pages[0] === null) {
                                self.hasPrevious = true;
                            }
                            if (self.pages[self.pages.length - 1] === null) {
                                self.hasNext = true;
                            }
                        }
                    }
                    
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
                        var k = 0;
                        var notMeMembers = o.members.filter(function(m) {
                            return m.id !== self.$store.state.user.id
                        });
                        notMeMembers.forEach(function(member) {
                            k++;
                            name += ' ' + (member.displayName || member.username);
                            if (notMeMembers.length > 1 && k < (notMeMembers.length)) {
                                if (k === (notMeMembers.length - 1)) {
                                    name += ' and';
                                } else {
                                    name += ',';
                                }
                            }
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
                                'jwk',
                                JSON.parse(member.publicKeyJwkJson),
                                {
                                    name: "RSA-OAEP",
                                    modulusLength: 2048,
                                    publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
                                    hash: {name: "SHA-1"}
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
                            clubhouseDecryptMessage(
                                self.$store.state.privateKey,
                                hexToArray(message.messageEncryptedJson),//info.encryptedMessageBytes,
                                hexToArray(message.messageEncryptedJsonVector),//info.messageVector,
                                hexToArray(message.encryptedPasswordHex),//ep.encryptedPasswordBytes,
                                hexToArray(message.passwordVectorHex)//ep.passwordVector,
                            ).then(function(bodyJson) {
                                    console.log('decrypted message ', bodyJson);
                                    var data = JSON.parse(bodyJson);
                                    message.html = self.markdownToHtml(data.bodyMarkdown, data, message);
                                    message.text = data.bodyMarkdown;
                                    console.log('message.html! ', message.html);
                                    Vue.set(self.messages, message.$index, message);
                                    leftToDecrypt--;
                                    if (leftToDecrypt <= 0) {
                                        self.messagesDecrypted = true;
                                        self.alwaysAfterFetchingFinished();
                                        if (self.onAfterFetchingFinished) {
                                            self.onAfterFetchingFinished();
                                        }
                                        if (self.afterFetchingFinished) {
                                            Vue.nextTick(self.afterFetchingFinished);
                                        }
                                    }
                                    self.$store.commit('updateChannelSeen', {channelId: self.channelId, mentionsCount: o.unreadMentionsCount, hasNew: o.unreadCount>0});
                            }).catch(function(err) {
                                console.error(err);
                            });
                        });
                    } else {
                        self.messagesDecrypted = true;
                        self.alwaysAfterFetchingFinished();                        
                        if (self.onAfterFetchingFinished) {
                            self.onAfterFetchingFinished();
                        }
                        if (self.afterFetchingFinished) {
                            Vue.nextTick(self.afterFetchingFinished);
                        }
                        self.$store.commit('updateChannelSeen', {channelId: self.channelId, mentionsCount: o.unreadMentionsCount, hasNew: o.unreadCount>0});
                    }
                    self.isLoaded = true;
                }
            });
        },
        ///////// MESSAGE PROCESSING & FORMATTERS //////////
        initMessageForFeed: function(messageCombo) {
            var self = this;
            var message = {
                edited: messageCombo.edited,
                editing: false,
                encryptedPasswordHex: messageCombo.encryptedPasswordHex,                
                html: '',
                id: messageCombo.id,
                '$index': null,
                messageJson: messageCombo.messageJson,
                messageEncryptedJson: messageCombo.messageEncryptedJson,
                messageEncryptedJsonVector: messageCombo.messageEncryptedJsonVector,
                parentMessageId: messageCombo.parentMessageId,
                passwordVectorHex: messageCombo.passwordVectorHex,
                showUser: true,
                
                saving: false,
                deleted: false,
                createdAt: messageCombo.createdAt,
                createdAtFormatted: self.formatDate(messageCombo.createdAt),
                createdAtFullFormatted: self.formatFullDate(messageCombo.createdAt),
                userHash: self.hashUser(messageCombo.fromUsername),
                fromUsername: messageCombo.fromUsername,
                fromUserId: messageCombo.fromUserId,
                reactionsProcessed: {},
                reactions: messageCombo.reactions || [],
                text: '',
                threadId: messageCombo.threadId,
                title: messageCombo.title,
                threadIndex: 0,
                userHash: self.hashUser(messageCombo.fromUsername),
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
        alwaysAfterFetchingFinished: function() {
            setTimeout(function() {
                ClubhouseMobileInterop.markRouteLoaded();
            }, 20);
        },
        markdownToHtml: function(markdown, data, message) {
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
            if (data.widgets) {
                data.widgets.forEach(function(widget) {
                    if (widget.type === 'image') {
                        var img = new Image();
                        var width = widget.width;
                        var height = widget.height;
                        // TODO use actual div width
                        var maxWidth = 700;
                        if (width > maxWidth)  {
                            var scaler = maxWidth / width;
                            width = 700;
                            height = scaler * height;
                        }
                        img.style.width = width + 'px';
                        img.style.height = height + 'px';
                        img.src = widget.src;
                    }
                    html += '<br>' + img.outerHTML;
                });
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
        

        //////// INCOMING EVENTS & MESSAGES //////
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
                clubhouseDecryptMessage(
                    self.$store.state.privateKey,
                    hexToArray(incoming.messageEncryptedJson),//info.encryptedMessageBytes,
                    hexToArray(incoming.messageEncryptedJsonVector),//info.messageVector,
                    hexToArray(incoming.encryptedPasswordHex),//ep.encryptedPasswordBytes,
                    hexToArray(incoming.passwordVectorHex)//ep.passwordVector,
                ).then(function(bodyJson) {
                        console.log('decrypted message ', bodyJson);
                        var data = JSON.parse(bodyJson);
                        message.html = self.markdownToHtml(data.bodyMarkdown, data, message);
                        message.text = data.bodyMarkdown;
                        message.widgets = data.widgets || [];
                        console.log('message.html! ', message.html);
                        if (isEdit) {
                            $(self.$el).find('#channel-message-' + message.id + ' .message-html').html(message.html);
                            Vue.set(self.messages, message.$index, message);
                        } else {
                            self.messages.push(message);
                            if (self.pages && self.pages.length && self.pages[self.pages.length-1]) {
                                self.pages[self.pages.length-1].push(message);
                                Vue.set(self.pages, self.page-1, self.pages[self.page-1]);
                                // Add to offsets dictionary
                            }
                            
                            self.showMessageNotificationMaybe(incoming, message);
                            if (!message.read) {
                                stallion.request({
                                    url: '/clubhouse-api/messaging/mark-read',
                                    method: 'POST',
                                    data: {messageId: message.id}
                                });
                                if (self.afterIncomingMessage) {
                                    Vue.nextTick(self.afterIncomingMessage);
                                }
                            }
                        }
                }).catch(function(err) {
                    console.error(err);
                });
            } else {
                var messageData = JSON.parse(incoming.messageJson);
                message.html = self.markdownToHtml(messageData.bodyMarkdown, messageData, message);
                message.text = messageData.bodyMarkdown;
                message.widgets = messageData.widgets || [];
                if (isEdit) {
                    Vue.set(self.messages, message.$index, message);
                    $(self.$el).find('#channel-message-' + message.id + ' .message-html').html(message.html);

                } else {
                    self.messages.push(message);
                    if (self.pages && self.pages.length && self.pages[self.pages.length-1] !== null) {
                        self.pages[self.pages.length-1].push(message);
                        Vue.set(self.pages, self.page-1, self.pages[self.page-1]);
                        // Add to offsets dictionary
                    }
                    self.showMessageNotificationMaybe(incoming, message);
                    if (self.afterIncomingMessage) {
                        Vue.nextTick(self.afterIncomingMessage);
                    }
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
            var link = 'https://clubhouse.local/#/channel/' + message.channelId;
            if (message.threadId) {
                link = 'https://clubhouse.local/#/forum/' + message.channelId + '/' + message.threadId + '?messageId=' + message.id;
            }
            

            stallionClubhouseApp.sendNotifiction(
                'Message from ' + incoming.fromUsername,
                {
                    body: message.text,
                    icon: 'https://www.gravatar.com/avatar/' + self.hashUser(incoming.fromUsername) + '?d=retro',
                    silent: false
                },
                link
            );
        },
        /////// SAVE/POST NEW MESSAGE
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
                                window.scrollTo(0,document.body.scrollHeight);
                                if (!ClubhouseMobileInterop.isMobile) {
                                    $('#post-message-box').focus();
                                }
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
            var messageData = {'bodyMarkdown': self.newMessage, widgets: []};
            
            function doPostRequest() {
                stallion.request({
                    url: '/clubhouse-api/messaging/post-message',
                    method: 'POST',
                    data: {
                        messageJson: JSON.stringify(messageData),
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
                        Vue.nextTick(function() {
                            window.scrollTo(0,document.body.scrollHeight);
                            if (!ClubhouseMobileInterop.isMobile) {
                                $('#post-message-box').focus();
                            }
                        });
                    }
                });
            }
            var m = /http(s|):\/\/[\S]*(.jpg|.svg|.png|.gif)/ig
            var result;
            var lastImage = null;
            while (result = m.exec(self.newMessage)) {
                lastImage = result[0];
            }
            if (!lastImage) {
                doPostRequest();
            } else {
                var img = new Image();
                img.onload = function() {
                    messageData.widgets.push({
                        type: 'image',
                        src: lastImage,
                        height: this.height,
                        width: this.width
                    });
                    doPostRequest();
                }
                img.onerror = function() {
                    doPostRequest();
                }
                img.src = lastImage;
            }
            //var matches = m.exec(self.newMessage);
            
        },
        
        
        /////// EDITING A MESSAGE ///////////
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
                    message.html = self.markdownToHtml(message.text, {}, message);
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
                            message.html = self.markdownToHtml(message.text, {}, message);
                            message.saving = false;
                            message.editing = false;
                            message.edited = true;
                        }
                    });             
                });
            
        },
        ////// DELETE A MESSAGE //////////////
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
        ////// MESSAGE REACTIONS ////////////
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
                    if (!o.added) {
                        return;
                    }
                    self.addPersonToReactionEmoji(message, emoji, self.$store.state.user.username);
                    Vue.set(self.messages, message.$index, message);
                    if (self.pages && self.pages.length && self.pages[self.page-1]) {
                        Vue.set(self.pages, self.page-1, self.pages[self.page-1]);
                    }
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
                    if (!o.removed) {
                        return;
                    }
                    self.removePersonFromReactionEmoji(message, emoji, self.$store.state.user.username);
                    Vue.set(self.messages, message.$index, message);
                    if (self.pages && self.pages.length && self.pages[self.page-1]) {
                        Vue.set(self.pages, self.page-1, self.pages[self.page-1]);
                    }
                }
            });

        },
        handleIncomingNewReaction: function(reaction) {
            var self = this;
            var message;
            this.messages.forEach(function(m) {
                if (m.id === reaction.messageId) {
                    message = m;
                    return false;
                }
            });
            if (message) {
                this.addPersonToReactionEmoji(message, reaction.emoji, reaction.displayName);
                Vue.set(self.messages, message.$index, message);
                if (self.pages && self.pages.length && self.pages[self.page-1]) {
                    Vue.set(self.pages, self.page-1, self.pages[self.page-1]);
                }
            }
        },
        handleIncomingRemovedReaction: function(reaction) {
            var self = this;
            var message;
            this.messages.forEach(function(m) {
                if (m.id === reaction.messageId) {
                    message = m;
                    return false;
                }
            });
            if (message) {
                this.removePersonFromReactionEmoji(message, reaction.emoji, reaction.displayName);
                Vue.set(self.messages, message.$index, message);
                if (self.pages && self.pages.length && self.pages[self.page-1]) {
                    Vue.set(self.pages, self.page-1, self.pages[self.page-1]);
                }
            }
        },
        addPersonToReactionEmoji: function(message, emoji, username) {
            var self = this;
            if (!message.reactions[emoji]) {
                message.reactions[emoji] = [];
            }
            var people = message.reactions[emoji];
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
        
        ////////// HELPERS ///////////
        messageTextToJson: function(text) {
            return JSON.stringify({'bodyMarkdown': text});
        },
        hashUser: function(username) {
            return md5(username);
        },
        formatDate: function(secs) {
            return moment.tz(secs * 1000, moment.tz.guess()).format('h:mm a');
        },
        formatFromNow: function(createdAt) {
            return moment.tz(createdAt * 1000, moment.tz.guess()).fromNow();//;.format('MMM d, YYYY h:mm a');
        },
        formatFullDate: function(createdAt) {
             return moment.tz(createdAt * 1000, moment.tz.guess()).format('MMM D, YYYY h:mm a');
        },    
    }        
};
    

