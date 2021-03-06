function ClubhouseMakeVuex() {
        var store = new Vuex.Store({
            state: {
                appLoading: true,
                defaultChannelId: theApplicationContext.defaultChannelId,
                channelCount: 0,
                user: null,
                userProfile: null,
                publicKey: null,
                privateKey: null,
                activeChannelId: null,
                idleStatus: 'AWAKE', // AWAKE, IDLE
                forumChannels: [],
                sidebarPoppedUp: false,
                websocketStatus: {state: 'PENDING', reconnectIn: 0, failedCount: 0},
                directMessageChannels: [],
                standardChannels: [],
                allUsers: [],
                allUsersById: {},
                channelById: {},
                site: {}
            },
            mutations: {
                appLoading: function(state, appLoading) {
                    state.appLoading = appLoading;
                },
                site: function(state, site) {
                    state.site = site;
                },
                publicKey: function(state, publicKey) {
                    state.publicKey = publicKey;
                },
                websocketStatus(state, websocketStatus) {
                    state.websocketStatus = websocketStatus;
                },                
                privateKeyAndPassword(state, data) {
                    state.privateKey = data.privateKey;
                    sessionStorage['private-key-passphrase-' + state.user.id] = data.encryptionPassword;
                },
                privateKey: function(state, privateKey) {
                    state.privateKey = privateKey;
                },
                defaultChannelIdChange: function(state, defaultChannelId) {
                    state.defaultChannelId = defaultChannelId;
                },
                sidebarPoppedUp: function(state, isActive) {
                    state.sidebarPoppedUp = isActive;
                },
                userStateChange: function(state, data) {
                    console.debug('commit state change ', data.userId, data.newState);
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
                    if (channel.channelType === 'DIRECT_MESSAGE') {
                        state.directMessageChannels.push(channel);
                    } else if (channel.channelType === 'FORUM') {
                        state.forumChannels.push(channel);
                    } else {
                        state.standardChannels.push(channel);
                    }
                    checkUpdateFavicon(state.channelById);
                },
                channelUpdated: function(state, channel) {
                    var existing = state.channelById[channel.id];
                    Object.keys(channel).forEach(function(key) {
                        existing[key] = channel[key];
                    });
                    if (channel.channelType === 'FORUM') {
                        state.forumChannels = state.forumChannels;
                    } else {
                        state.standardChannels = state.standardChannels;
                    }                    
                    checkUpdateFavicon(state.channelById);
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
                    checkUpdateFavicon(state.channelById);
                },
                newChannelMessage: function(state, message) {
                    if (state.channelById[message.channelId]) {
                        state.channelById[message.channelId].hasNew = true;
                        if (message.mentioned || message.hereMentioned) {
                            state.channelById[message.channelId].mentionsCount++;
                        }
                    }
                    checkUpdateFavicon(state.channelById);
                },
                updateChannelSeen: function(state, data) {
                    if (state.channelById[data.channelId]) {
                        state.channelById[data.channelId].hasNew = data.hasNew;
                        state.channelById[data.channelId].mentionsCount = data.mentionsCount;
                    }
                    checkUpdateFavicon(state.channelById);
                },
                markChannelSeen: function(state, channelId) {
                    if (state.channelById[channelId]) {
                        state.channelById[channelId].mentionsCount = 0;
                        state.channelById[channelId].hasNew = false;
                    }
                    checkUpdateFavicon(state.channelById);
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
                            console.debug('loaded private key!');
                            store.commit('privateKey', privateKey);
                        }
                    );
                    */
                },
                activeChannelId: function(state, channelId) {
                    console.debug('set active channelid ', channelId);
                    state.activeChannelId = channelId;
                },
                idleStatus: function(state, status) {
                    console.debug('commit new idle status: ', status, new Date().getTime());
                    if (status === 'AWAKE' && state.idleStatus !== status) {
                        setTimeout(function() {
                            var $box = $('#post-message-box');
                            if ($box.length > 0 && $box.is(':visible')) {
                                $box.get(0).focus();
                            }
                        }, 700);
                    }
                    state.idleStatus = status;
                }
            }            
        });

    var previousMentionCount = null;
    var previousHasNew = false;
    
    function checkUpdateFavicon(channelById) {
        var hasMentions = false;
        var hasNew = false;
        var mentionsCount = 0;
        console.debug('checkUpdateFavicon ' , channelById);
        if (!channelById) {
            return;
        }
        Object.keys(channelById).forEach(function(channelId) {
            var channel = channelById[channelId];
            if (channel.deleted) {
                return;
            }
            if (channel.mentionsCount > 0) {
                hasMentions = true;
                mentionsCount += channel.mentionsCount;
            }
            if (channel.hasNew) {
                hasNew = true;
            }
        });

        if (hasNew === previousHasNew && mentionsCount === previousMentionCount) {
            console.debug('no updates to mentions or hasNew');
            return
        }
        previousHasNew = hasNew;
        previousMentionCount = mentionsCount;
        
        
        if (ClubhouseMobileInterop.isAppMode) {
            ClubhouseMobileInterop.tellAppToUpdateMentions();
            return;
        }
        try {
            var favicon= new Favico({
                animation: 'none',
                //type : 'rectangle',
                //bgColor : '#5CB85C',
                //textColor : '#ff0',
            });
            var image = document.getElementById('chat-off-image');
            if (hasMentions || hasNew || mentionsCount > 0) {
                image = document.getElementById('chat-active-image');
            }
            favicon.image(image);

            if (mentionsCount > 0) {
                favicon.badge(mentionsCount);
            } else {
                //favicon.badge(3);
            }

            //

            console.debug('updated favicon');
        } catch (err) {
            console.error('error updating favicon ', err);
        }
        
    }

    return store;
}
var ClubhouseGlobalStateManager = function(vueApp) {

    var manager = this;
    manager.reconnectWait = 200;
    manager.reconnectFailCount = 0;
    manager.started = false;
    manager.madeFirstConnection = false;
    console.debug('new global state manager');

    
    manager.start = function() {
        if (manager.started) {
            console.debug('manager already started');
            return;
        }
        if (vueApp.$store.state.user && vueApp.$store.state.user.id) {
            manager.started = true;
            if (stallion.getCookie("stUserSession")) {
                manager.setupWebSocket();
            }
            manager.setupIframeResizeListener();
            manager.setupIfVisible();
        }
    };

    manager.containerShown = function() {
        if (electronInterop) {
            electronInterop.isHidden = false;
        }
        console.debug('conatiner shown, awake now');
        vueApp.$store.commit('idleStatus', 'AWAKE');
        manager.updateUserState('AWAKE');
    }

    manager.containerHidden = function() {
        if (electronInterop) {
            electronInterop.isHidden = true;
        }
        vueApp.$store.commit('idleStatus', 'IDLE');
        manager.updateUserState('IDLE');
    }
    

    manager.setupIfVisible = function() {

        ifvisible.on("idle", function(){
            console.debug('now idle');
            vueApp.$store.commit('idleStatus', 'IDLE');
            manager.updateUserState('IDLE');
        });
        
        ifvisible.on("wakeup", function(){
            console.debug('on wakeup', new Date().getTime());
            vueApp.$store.commit('idleStatus', 'AWAKE');
            manager.updateUserState('AWAKE');
        });

        ifvisible.on("blur", function(){
            console.debug('now hidden');
            vueApp.$store.commit('idleStatus', 'IDLE');
            manager.updateUserState('IDLE');
        });
        
        ifvisible.on("focus", function(){
            console.debug('on focus');
            vueApp.$store.commit('idleStatus', 'AWAKE');
            manager.updateUserState('AWAKE');
        });        
    
    }
    

    

    manager.loadContext = function(callback) {
        stallion.request({
            url: '/clubhouse-api/general-context',
            success: function(o) {
                vueApp.$store.commit('generalContext', o);
                if (callback) {
                    callback(o);
                }
            }
        });
    };

    manager.updateUserState = function(state) {
        if (!manager.clubhouseSocket || !manager.clubhouseSocket.readyState === 1) {
            return;
        }
        console.debug('manager.updateUserState', state, new Date().getTime());
        manager.clubhouseSocket.send('updateUserState\n\n' + JSON.stringify({
            state: state
        }));
    }

    manager.setupWebSocket = function() {

        var scheme = 'wss://';
        if (window.location.protocol === 'http:') {
            scheme = 'ws://';
        }
    
        //manager.clubhouseSocket = new WebSocket(scheme + window.location.host + "/st-wsroot/events/?stUserSession=" + encodeURIComponent(stallion.getCookie("stUserSession")));
        var wsUrl = scheme + window.location.host + "/st-wsroot/events/?stUserSession=" + encodeURIComponent(stallion.getCookie("stUserSession") || '') + "&userState=" + (vueApp.$store.state.idleStatus || 'AWAKE');
        manager.clubhouseSocket = new WebSocket(wsUrl);
        
        console.debug('setup web socket.');

        
        manager.clubhouseSocket.onopen = function(event) {
            console.debug('websocket opened ', event);
            vueApp.$store.commit('websocketStatus', {state: 'OPEN'});
            manager.loadContext();
            if (vueApp.currentChannelComponent && vueApp.currentChannelComponent.isLoaded) {
                vueApp.currentChannelComponent.refresh();
            }
        };

        function eventMatchesCurrentChannel(channelId) {
            if (!window.ClubhouseVueApp.currentChannelComponent) {
                return false;
            }
            if (!window.ClubhouseVueApp.currentChannelComponent.channel) {
                return false;
            }
            if (window.ClubhouseVueApp.currentChannelComponent.channel.id !== channelId) {
                return false;
            }
            return true;
            
        }

        manager.clubhouseSocket.onerror = function(event) {
            console.log('websocket onerror ', event);
            vueApp.$store.commit('websocketStatus', {state: 'ERROR'});
        }

        
        manager.clubhouseSocket.onmessage = function (event) {
            
            console.debug(event.data);
            var data = JSON.parse(event.data);
            if (data.type === 'channel-changes') {
                manager.loadContext();
            } else if (data.type === 'confirmed-ws-open') {
                console.log('confirmed web socket opened');
                manager.reconnectFailCount = 0;
            } else if (data.type === 'new-channel') {
                vueApp.$store.commit('channelAdded', data.channel);
            } else if (data.type === 'channel-updated') {
                vueApp.$store.commit('channelUpdated', data.channel);
            } else if (data.type === 'new-member') {
                vueApp.$store.commit('userAdded', data.member);
            } else if (data.type === 'member-updated') {
                vueApp.$store.commit('userUpdated', data.member);
            } else if (data.type === 'state-change') {
                console.debug('state change!', data.userId, data.newState);
                vueApp.$store.commit('userStateChange', {userId: data.userId, newState: data.newState});
            } else if (data.type === 'new-reaction') {
                if (eventMatchesCurrentChannel(data.channelId)) {
                    window.ClubhouseVueApp.currentChannelComponent.handleIncomingNewReaction(data.reaction);
                }
            } else if (data.type === 'removed-reaction') {
                if (eventMatchesCurrentChannel(data.channelId)) {
                    window.ClubhouseVueApp.currentChannelComponent.handleIncomingRemovedReaction(data.reaction);
                }
            } else if (data.type === 'new-message' || data.type === 'message-edited') {
                console.debug('new-message');
                if (eventMatchesCurrentChannel(data.message.channelId)) {
                    window.ClubhouseVueApp.currentChannelComponent.handleIncomingMessage(data.message, data.type, data, event);
                    return;
                }
                if (data.type === 'message-edited') {
                    return;
                }
                console.debug('message on non-open channel', data.message);
                if (!data.message.read) {
                    if (!vueApp.$store.state.channelById[data.message.channelId]) {
                        ClubhouseVueApp.stateManager.loadContext();
                    } else {
                        vueApp.$store.commit('newChannelMessage', data.message);
                    }
                }
                if (data.message.mentioned || data.message.hereMentioned && !data.message.read) {
                    
                    
                    console.debug('message is mentioned and not read');
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
                    var link = theApplicationContext.site.siteUrl + '/#/channel/' + data.message.channelId;

                    if (data.message.channelType === 'FORUM') {
                        link = theApplicationContext.site.siteUrl + '/#/forum/' + data.message.channelId + '/' + data.message.threadId + '?messageId=' + data.message.id;
                    }

                    console.debug('call sendNotification');
                    stallionClubhouseApp.sendNotifiction(
                        'Message from ' + data.message.fromUsername,
                        {
                            body: text,
                            icon: iconUrl,
                            silent: false
                        },
                        link
                    );
                }
            }
        }
        manager.clubhouseSocket.onclose = function(event) {
            if (event.code === 1008) {
                vueApp.$store.commit('websocketStatus', {state: 'PENDING', reconnectIn: 0, failedCount: 0});
                stallion.showError('You are not logged in.');
                if (ClubhouseMobileInterop.isAppMode) {
                    ClubhouseMobileInterop.redirectToLogin();
                } else {
                    window.location.hash = '/login';
                    setTimeout(function() {
                        vueApp.$store.commit('updateCurrentUser', {user: {}, userProfile: {}});
                    }, 20);
                    
                }
                return;
            }
            manager.reconnectFailCount = manager.reconnectFailCount + 1;
            console.log('failCount ', manager.reconnectFailCount, manager);
            var wait = parseInt(manager.reconnectWait * Math.pow(manager.reconnectFailCount, 1.8), 10);
            console.log('web socket closed, reconnect in ', wait, event);
            vueApp.$store.commit('websocketStatus', {state: 'CLOSED', reconnectIn: parseInt(wait / 1000.0, 10), failedCount: manager.reconnectFailCount});
            setTimeout(manager.setupWebSocket, wait);
            if (manager.reconnectFailCount > 5) {
                var retrySeconds = parseInt(wait / 1000, 10);
                //stallion.showError('Error connecting to server. Retrying in ' + retrySeconds + ' seconds.');
            }
        }
    };

    manager.setupIframeResizeListener = function() {
        window.addEventListener("message", function(e) {
            console.debug('received message!', e);
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

            }
            
            $frame.height(newHeight);
            $frame.css({display: 'block'});
            console.log('new height for frame ', newHeight, oldHeight, $frame.attr('src'), e.data.iframeId);
            setTimeout(function() {
                var objDiv = $('.channel-messages').get(0);
                if (objDiv) {
                    console.debug('diff is ', diff);
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
