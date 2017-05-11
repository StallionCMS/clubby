


var ClubhouseMessageSendingMixin = {
    data: function() {
        var d = {
            title: '',
            originalContent: '',
            widgets: [],
            messageId: null,
            canSubmit: false
        };
        if (this.message) {
            var message = JSON.parse(JSON.stringify(this.message));
            d.title = message.title;
            
        }
        return d;
    },
    watch: {
        '$route': 'onMixinRoute'
    },
    methods: {
        onMixinRoute: function() {
            this.canSend = false;
            this.messageId = 0;
            this.title = '';
            this.originalContent = '';
            this.widgets = [];
        },
        afterFetchedData: function(channel, members) {
            var self = this;
            if (!channel.encrypted) {
                this.canSubmit = true;
            }
            var keysProcessed = 0;
            members.forEach(function(member) {
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
                        self.canSubmit = true;
                    }
                });
            });            
                
        },
        postMessage: function(args) {
            new ClubhouseMessageSender(this, this.$store, args).send();     
        }
    }
};


var ClubhouseMessageSender = function(component, $store, data) {
    var channelId = data.channelId;
    var title = data.title || '';
    var originalContent = data.originalContent || '';
    var widgets = data.widgets || [];
    var parentMessageId = data.parentMessageId || 0;
    var threadId = data.threadId || 0;    
    var messageId = data.messageId || null;
    var encrypted = data.encrypted;
    var channelMembers = data.channelMembers;
    var success = data.success;
    var error = data.error;
    var after = data.after;
    var message = data.message;
    

    if (!channelId) {
        throw new Error('ChannelId is required');
    }
    if (!markdown) {
        throw new Error('Message body is required');
    }

    function postNewEncryptedMessage() {
        var self = this;
        var messageJson = JSON.stringify({
            bodyMarkdown: originalContent,
            widgets: widgets
        });
        var mentions = mentionsFromText(originalContent);
        var tos = [];
        channelMembers.forEach(function(member) {
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
                        channelId: channelId,
                        threadId: threadId,
                        parentMessageId: parentMessageId,
                        title: title,
                        encryptedPasswords: result.encryptedPasswords,
                        usersMentioned: mentions.usersMentioned,
                        channelMentioned: mentions.channelMentioned,
                        hereMentioned: mentions.hereMentioned
                    },
                    success: success
                });
            }
        );
    }
    
    function postNewMessage() {
        var mentions = mentionsFromText(originalContent);
        stallion.request({
            url: '/clubhouse-api/messaging/post-message',
            method: 'POST',
            data: {
                messageJson: JSON.stringify({
                    'bodyMarkdown': originalContent,
                    widgets: widgets
                }),
                title: title,
                channelId: channelId,
                threadId: threadId,
                parentMessageId: parentMessageId,
                usersMentioned: mentions.usersMentioned,
                channelMentioned: mentions.channelMentioned,
                hereMentioned: mentions.hereMentioned
            },
            success: success,
            error: error,
            after: after
        });
    }

    function updateMessage() {
        stallion.request({
            url: '/clubhouse-api/messaging/update-message',
            method: 'POST',
            data: {
                id: messageId,
                title: title,
                messageJson: JSON.stringify({
                    bodyMarkdown: originalContent,
                    widgets: widgets
                })
            },
            success: success,
            error: error,
            after: after
        });
    }
    
    function updateEncryptedMessage() {
        new ReEncrypter().reencryptMesage(
            JSON.stringify({
                bodyMarkdown: originalContent,
                widgets: widgets
            }),
            $store.state.privateKey,
            message.encryptedPasswordHex,
            message.passwordVectorHex,
            function(result) {
                stallion.request({
                    url: '/clubhouse-api/messaging/update-encrypted-message',
                    method: 'POST',
                    data: {
                        id: messageId,
                        title: title,
                        parentMessageId: parentMessageId,
                        threadId: threadId,
                        messageEncryptedJsonVector: result.messageVectorHex,
                        messageEncryptedJson: result.encryptedMessageHex
                    },
                    success: success,
                    error: error,
                    after: after
                });             
            });
    }   
    
    function  mentionsFromText(text) {
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
    }

    function send() {
        if (messageId) {
            if (encrypted) {
                updateEncryptedMessage();
            } else {
                updateMessage();
            }

        } else {
            if (encrypted) {
                postNewEncryptedMessage();
            } else {
                postNewMessage();
            }
        }
    }

    this.send = send;
    

};
