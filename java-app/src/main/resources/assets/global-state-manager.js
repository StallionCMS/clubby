function ClubhouseMakeVuex() {
        var store = new Vuex.Store({
            state: {
                user: null,
                userProfile: null,
                publicKey: null,
                privateKey: null,
                activeChannelId: null,
                forumChannels: [],
                directMessageChannels: [],
                standardChannels: [],
                allUsers: [],
                allUsersById: {},
                channelById: {}
            },
            mutations: {
                publicKey: function(state, publicKey) {
                    state.publicKey = publicKey;
                },
                privateKey: function(state, privateKey) {
                    state.privateKey = privateKey;
                },
                userStateChange: function(state, data) {
                    console.log('commit state change ', data.userId, data.newState);
                    if (state.user && data.userId === state.user.id) {
                        return;
                    }
                    if (state.allUsersById[data.userId]) {
                        state.allUsersById[data.userId].state = data.newState;
                    }
                },
                userAdded: function(state, user) {
                    state.allUsersById[user.id] = user;
                    state.allUsers.push(user);
                },
                userUpdated: function(state, user) {
                    var existing = state.allUsersById[user.id];
                    Object.keys(user).forEach(function(key) {
                        existing[key] = user[key];
                    });
                },
                channelAdded: function(state, channel) {
                    state.channelById[channel.id] = channel;
                    if (channel.type === 'DIRECT_MESSAGE') {
                        state.directMessageChannels.push(channel);
                    } else if (channel.type === 'FORUM') {
                        state.forumChannels.push(channel);
                    } else {
                        state.standardChannels.push(channel);
                    }
                    
                },
                channelUpdated: function(state, channel) {
                    var existing = state.channelById[channel.id];
                    Object.keys(channel).forEach(function(key) {
                        existing[key] = channel[key];
                    });
                },                
                generalContext: function(state, ctx) {
                    state.standardChannels = ctx.standardChannels;
                    state.forumChannels = ctx.forumChannels;
                    state.directMessageChannels = ctx.directMessageChannels;
                    var channelById = {};
                    var allChannels = ctx.standardChannels.concat(ctx.forumChannels).concat(ctx.directMessageChannels);
                    allChannels.forEach(function(channel) {
                        channelById[channel.id] = channel;
                    });
                    state.channelById = channelById;
                    var usersById = {};
                    ctx.users.forEach(function(user) {
                        usersById[user.id] = user;
                    });
                    state.allUsersById = usersById;
                    state.allUsers = ctx.users;
                },
                newChannelMessage: function(state, message) {
                    state.channelById[message.channelId].hasNew = true;
                    if (message.mentioned || message.hereMentioned) {
                        state.channelById[message.channelId].mentionsCount++;
                    }
                },
                markChannelSeen: function(state, channelId) {
                    state.channelById[channelId].mentionsCount = 0;
                    state.channelById[channelId].hasNew = false;
                },
                updateCurrentUser: function(state, o) {
                    state.user = o.user;
                    state.userProfile = o.userProfile;
                },
                login: function(state, o) {
                    state.user = o.user;
                    state.userProfile = o.userProfile;
                    /*
                    new PrivateKeyFetcher().fetch(
                        o.userProfile.encryptedPrivateKeyHex,
                        o.userProfile.encryptedPrivateKeyInitializationVectorHex,
                        sessionStorage.privateKeyPassphrase,
                        function(privateKey) {
                            console.log('loaded private key!');
                            store.commit('privateKey', privateKey);
                        }
                    );
                    */
                },
                activeChannelId: function(state, channelId) {
                    console.log('set active channelid ', channelId);
                    state.activeChannelId = channelId;
                }
            }            
        });

    return store;
}
var ClubhouseGlobalStateManager = function(vueApp) {

    var manager = this;
    manager.reconnectWait = 200;
    manager.reconnectFailCount = 0;

    
    manager.start = function() {
        if (vueApp.$store.state.user) {
            manager.setupWebSocket();
        }
        manager.setupIframeResizeListener();
    };
    

    

    manager.loadContext = function() {
        stallion.request({
            url: '/clubhouse-api/messaging/general-context',
            success: function(o) {
                vueApp.$store.commit('generalContext', o);
            }
        });
    };

    manager.setupWebSocket = function() {
        manager.clubhouseSocket = new WebSocket("wss://clubhouse.local/st-wsroot/events/?stUserSession=" + encodeURIComponent(stallion.getCookie("stUserSession")));
        console.log('setup web socket.');
        manager.clubhouseSocket.onopen = function(event) {
            manager.reconnectFailCount = 0;
            manager.loadContext();
            if (vueApp.currentChannelComponent && vueApp.currentChannelComponent.isLoaded) {
                vueApp.currentChannelComponent.refresh();
            }
        };
        
        manager.clubhouseSocket.onmessage = function (event) {
            
            console.log(event.data);
            var data = JSON.parse(event.data);
            if (data.type === 'new-channel') {
                vueApp.$store.commit('channelAdded', data.channel);
            } else if (data.type === 'channel-updated') {
                vueApp.$store.commit('channelUpdated', data.channel);
            } else if (data.type === 'new-member') {
                vueApp.$store.commit('userAdded', data.member);
            } else if (data.type === 'member-updated') {
                vueApp.$store.commit('userUpdated', data.member);
            } else if (data.type === 'state-change') {
                console.log('state change!', data.userId, data.newState);
                vueApp.$store.commit('userStateChange', {userId: data.userId, newState: data.newState});
            } else if (data.type === 'new-message' || data.type === 'message-edited') {
                console.log('new-message');
                if (window.ClubhouseVueApp.currentChannelComponent) {
                    if (window.ClubhouseVueApp.currentChannelComponent.channel &&
                        window.ClubhouseVueApp.currentChannelComponent.channel.id === data.message.channelId) {
                        console.log('call handleIncomingMessage on channel');
                        window.ClubhouseVueApp.currentChannelComponent.handleIncomingMessage(data.message, data.type, data, event);
                        return;
                    }
                }
                if (data.type === 'message-edited') {
                    return;
                }
                console.log('message on non-open channel', data.message);
                if (!data.message.read) {
                    vueApp.$store.commit('newChannelMessage', data.message);
                }
                if (data.message.mentioned || data.message.hereMentioned && !data.message.read) {
                    var text = '<click to go to message>';
                    if (data.message.messageJson) {
                        var d = JSON.parse(data.message.messageJson);
                        if (d && d.bodyMarkdown) {
                            text = d.bodyMarkdown;
                        }
                    }
                    var iconUrl = '';
                    var u = window.ClubhouseVueApp.$store.state.allUsersById[data.message.fromUserId];
                    if (u && u.avatarUrl) {
                        iconUrl = u.avatarUrl;
                    }
                    stallionClubhouseApp.sendNotifiction(
                        'Message from ' + data.message.fromUsername,
                        {
                            body: text,
                            icon: iconUrl,
                            silent: false
                        },
                        'https://clubhouse.local/#/channel/' + data.message.channelId
                    );
                }
            }
        }
        manager.clubhouseSocket.onclose = function(event) {
            manager.reconnectFailCount++;
            var wait = parseInt(manager.reconnectWait * Math.pow(manager.reconnectFailCount, 1.8), 10);
            console.log('web socket closed, reconnect in ', wait);
            setTimeout(manager.setupWebSocket, wait);
            if (manager.reconnectFailCount > 5) {
                var retrySeconds = parseInt(wait / 1000, 10);
                stallion.showError('Error connecting to server. Retrying in ' + retrySeconds + ' seconds.');
            }
        }
    };

    manager.setupIframeResizeListener = function() {
        window.addEventListener("message", function(e) {
            console.log('received message!', e);
            if (!e.data.iframeId) {
                return;
            }
            var $frame = $('#' + e.data.iframeId);
            var newHeight = e.data.iframeHeight + 30;
            var oldHeight = $frame.height();
            
            if ($frame.css('display') === 'none') {
                oldHeight = 0;
            }
            var diff = newHeight - oldHeight;
            if (diff < 0) {
                return;
            }
            if (e.data.iframeHeight === 0) {
                debugger;
            }
            
            $frame.height(newHeight);
            $frame.css({display: 'block'});
            console.log('new height for frame ', newHeight, oldHeight, $frame.attr('src'), e.data.iframeId);
            setTimeout(function() {
                var objDiv = $('.channel-messages').get(0);
                if (objDiv) {
                    console.log('diff is ', diff);
                    if (objDiv.scrollTop > (objDiv.scrollHeight / 2)) {
                        var newTop = objDiv.scrollTop + diff;
                        if (newTop < 0) {
                            newTop = 0;
                        }
                        objDiv.scrollTop = newTop;
                        console.log('iframe updated scroll to ', newTop);                        
                    }

                }
            }, 20);
            
        }, false);        

    };
    

};
