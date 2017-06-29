console.log("executing app.js");

(function() {
    window.stallionClubhouseApp = window.stallionClubhouseApp || {};
    var app = window.stallionClubhouseApp;


    
    app.init = function() {
        var Bar = { template: '<div>bar</div>' }

        // Patch stallion.request
        var originalStallionRequest = stallion.request;
        stallion.request = function(data) {
            if (data.url && data.url.indexOf('//') === -1) {
                data.url = theApplicationContext.site.siteUrl + data.url;
            }
            originalStallionRequest(data);
        };
        
        app.setupEmojiConverter();
        
        // Load global directives
        clubhouseLoadDirectives();
        
        var routes = [
            {
                path: '/encryption-demo',
                component: vueComponents['encryption-demo'],
                meta: {
                    authRequired: false
                }
            },
            {
                path: '/encryption-sandbox',
                component: vueComponents['encryption-sandbox'],
                meta: {
                    authRequired: false
                }
            },            
            {
                path: '/manual-key-generation',
                component: vueComponents['manual-key-generation'],
                meta: {
                    authRequired: false
                }                
            },
            {
                path: '/login',
                component: vueComponents['clubhouse-login'],
                meta: {
                    authRequired: false
                }                
            },
            {
                path: '/mobile-login',
                component: vueComponents['mobile-login'],
                meta: {
                    authRequired: true
                }                
            },            
            {
                path: '/first-user',
                component: vueComponents['first-user'],
                meta: {
                    authRequired: false
                }                
            },
            {
                path: '/my-settings',
                component: vueComponents['my-settings'],
                meta: {
                    authRequired: true
                }
            },
            {
                path: '/profile/:userId',
                component: vueComponents['profile-public'],
                meta: {
                    authRequired: true
                }
            },
            {
                path: '/my-channels',
                component: vueComponents['my-channels'],
                meta: {
                    authRequired: true
                }
            },
            {
                path: '/channel/:channelId',
                component: vueComponents['channel-feed'],
                meta: {
                    authRequired: true
                }
            },
            {
                path: '/forum/:channelId',
                component: vueComponents['forum-top-level'],
                meta: {
                    authRequired: true
                }
            },
            {
                path: '/forum/:channelId/new-thread',
                component: vueComponents['forum-create-or-edit-post'],
                meta: {
                    authRequired: true
                }
            },
            {
                path: '/forum/:messageId/edit',
                component: vueComponents['forum-create-or-edit-post'],
                meta: {
                    authRequired: true
                }
            },
            {
                path: '/forum/:channelId/:threadId',
                component: vueComponents['forum-thread'],
                meta: {
                    authRequired: true
                }
            },
            {
                path: '/channel-settings/:channelId',
                component: vueComponents['channel-settings'],
                meta: {
                    authRequired: true
                }
            },
            {
                path: '/channel-settings',
                component: vueComponents['channel-settings'],
                meta: {
                    authRequired: true
                }
            },
            {
                path: '/clubhouse-settings',
                component: vueComponents['clubhouse-settings'],
                meta: {
                    authRequired: true
                }
            },
            {
                path: '/channel-members/:channelId',
                component: vueComponents['channel-members'],
                meta: {
                    authRequired: true
                }
            },
            {
                path: '/open-direct-message',
                component: vueComponents['open-direct-message'],
                meta: {
                    keyRequired: true
                }
            },
            {
                path: '/invite-user',
                component: vueComponents['invite-user'],
                meta: {
                    authRequired: true
                }
            },
            {
                path: '/accept-invite',
                component: vueComponents['accept-invite'],
                meta: {
                    authRequired: false
                }
            },
            {
                path: '/',
                component: vueComponents['app-home-v4'],
                meta: {
                    authRequired: false
                }
            }            
        ];

        var store = ClubhouseMakeVuex();
        
        app.store = store;

        store.commit('site', theApplicationContext.site);



        if (theApplicationContext.defaultChannelId) {
            store.commit('defaultChannelIdChange', theApplicationContext.defaultChannelId);
        }

        // sessionStorage['private-key-passphrase-' + window.theApplicationContext.user.id] && 
        if (window.theApplicationContext.user && window.theApplicationContext.user.id) {
            store.commit('login', {
                user: window.theApplicationContext.user,
                userProfile: window.theApplicationContext.profile
            });
        }
        
        var router = new VueRouter({
            store: store,
            routes: routes
        });

        router.beforeEach(function(to, from, next) {
            if (to.meta.keyRequired || to.meta.authRequired !== false) {
                if (!app.store.state.user || !app.store.state.user.id) {
                    next('/login');
                    return;
                }
            }
            if (to.meta.keyRequired) {
                if (store.state.privateKey) {
                    next();
                } else if (app.store.state.user.id && sessionStorage['private-key-passphrase-' + app.store.state.user.id]) {
                    new VueKeyImporter(sessionStorage['private-key-passphrase-' + app.store.state.user.id], function() {
                        console.log('private key loaded from sessionStorage');
                        next();
                    }).importPublicAndPrivate();            
                } else {
                    window.location.hash = '/login';
                }
                console.log('key required!');
            } else {
                next();
            }
        });

        router.afterEach(function(to, from) {
            if (to.params.channelId) {
                store.commit('activeChannelId', parseInt(to.params.channelId, 10));
            } else {
                store.commit('activeChannelId', null);
            }
        });

        var vueApp;
        window.ClubhouseVueApp =  vueApp = new Vue({
            store: store,
            router: router
        });
        vueApp.currentChannelComponent = null;


        

        vueApp.stateManager = new ClubhouseGlobalStateManager(vueApp);
        vueApp.stateManager.start();
        
        vueApp.$mount('#vue-app');

        
        
    }

    


    app.sendNotifiction = function(text, data, link) {
        // Let's check if the browser supports notifications
        if (!("Notification" in window)) {
            return;
        }
        // Let's check whether notification permissions have already been granted
        else if (Notification.permission === "granted") {
            // If it's okay let's create a notification
            var notification = new Notification(text, data);
            notification.onclick = function() {
                if (link) {
                    window.location.href = link;
                }
                window.focus();
                notification.close();
            };
        }
        // Otherwise, we need to ask the user for permission
        else if (Notification.permission !== 'denied') {
            Notification.requestPermission(function (permission) {
                // If the user accepts, let's create a notification
                if (permission === "granted") {
                    var notification = new Notification(text, data);
                }
            });
        }

    }

    app.setupEmojiConverter = function() {
        var conv = new EmojiConvertor();
            conv.img_sets.apple.path = '/st-resource/clubhouse/emoji/sheets/';
            conv.img_sets.apple.sheet = '/st-resource/clubhouse/emoji/sheets/sheet_apple_64_indexed_128.png';
            // Configure this library to use the sheets defined in `img_sets` (see above)
            conv.use_sheet = true;
            conv.img_set = 'apple';
            conv.replace_mode = 'img';
            conv.text_mode = false;
            app.emojiConverter = conv;


    };

    app.dmChannelName = function() {
        
    };


    $(document).ready(function() {
        app.init();
    });
    
}());

function generateUUID() {
    var d = new Date().getTime();
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = (d + Math.random()*16)%16 | 0;
        d = Math.floor(d/16);
        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
    });
    return uuid;
};
    
function arrayBufferToHexString(arrayBuffer) {
    var byteArray = new Uint8Array(arrayBuffer);
    var hexString = "";
    var nextHexByte;
    
    for (var i=0; i<byteArray.byteLength; i++) {
        nextHexByte = byteArray[i].toString(16);
        if (nextHexByte.length < 2) {
            nextHexByte = "0" + nextHexByte;
        }
        hexString += nextHexByte;
    }
    return hexString;
}

function hexToArrayBuffer(hex) {
    var buffer = new ArrayBuffer(hex.length / 2);    
    for (var c = 0; c < hex.length; c += 2) {
        buffer[c /2] = parseInt(hex.substr(c, 2), 16);
    }
    return buffer;
}


function hexToArray(hex) {
    var bytes = new Uint8Array(hex.length / 2);    
    for (var c = 0; c < hex.length; c += 2) {
        bytes[c /2] = parseInt(hex.substr(c, 2), 16);
    }
    return bytes;
}

function arrayToHex(byteArray) {
    var hexString = "";
    var nextHexByte;
    
    for (var i=0; i<byteArray.byteLength; i++) {
        nextHexByte = byteArray[i].toString(16);
        if (nextHexByte.length < 2) {
            nextHexByte = "0" + nextHexByte;
        }
        hexString += nextHexByte;
    }
    return hexString;
}


function stringToArrayBuffer(string) {
    var encoder = new TextEncoder("utf-8");
    return encoder.encode(string);
}

function convertStringToArrayBufferView(str)
{
    var bytes = new Uint8Array(str.length);
    for (var iii = 0; iii < str.length; iii++) 
    {
        bytes[iii] = str.charCodeAt(iii);
    }

    return bytes;
}   

function convertArrayBufferViewtoString(buffer)
{
    if (buffer.byteLength !== undefined) {
        buffer = new Uint8Array(buffer);
    }
    var str = "";
    for (var iii = 0; iii < buffer.byteLength; iii++) 
    {
        str += String.fromCharCode(buffer[iii]);
    }

    return str;
}



