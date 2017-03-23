console.log("executing app.js");

(function() {
    window.stallionClubhouseApp = window.stallionClubhouseApp || {};
    var app = window.stallionClubhouseApp;

    app.init = function() {
        var Bar = { template: '<div>bar</div>' }

        var conv = new EmojiConvertor();
        conv.img_sets.apple.path = '/st-resource/clubhouse/emoji/sheets/';
        conv.img_sets.apple.sheet = '/st-resource/clubhouse/emoji/sheets/sheet_apple_64_indexed_128.png';
        // Configure this library to use the sheets defined in `img_sets` (see above)
        conv.use_sheet = true;
        conv.img_set = 'apple';
        conv.replace_mode = 'img';
        conv.text_mode = false;
        app.emojiConverter = conv;        

        var routes = [
            {
                path: '/encryption-demo',
                component: 'encryption-demo'
            },
            {
                path: '/manual-key-generation',
                component: 'manual-key-generation'
            },
            {
                path: '/login',
                component: 'clubhouse-login'
            },
            {
                path: '/my-settings',
                component: 'my-settings'
            },
            {
                path: '/my-channels',
                component: 'my-channels'
            },
            {
                path: '/channel/:channelId',
                component: 'channel-feed',
                meta: {
                    keyRequired: true
                }
            },
            {
                path: '/forum/:channelId',
                component: 'forum-top-level',
                meta: {
                    keyRequired: true
                }
            },
            {
                path: '/forum/:channelId/:parentMessageId',
                component: 'forum-thread',
                meta: {
                    keyRequired: true
                }
            },
            {
                path: '/channel-settings/:channelId',
                component: 'channel-settings'
            },
            {
                path: '/channel-settings',
                component: 'channel-settings'
            },
            {
                path: '/clubhouse-settings',
                component: 'clubhouse-settings'
            },
            {
                path: '/channel-members/:channelId',
                component: 'channel-members'
            },
            {
                path: '/open-direct-message',
                component: 'open-direct-message',
                meta: {
                    keyRequired: true
                }
            },
            {
                path: '/invite-user',
                component: 'invite-user'
            },
            {
                path: '/accept-invite',
                component: 'accept-invite'
            },
            {
                path: '/',
                component: 'app-home-v3'
            }            
        ];

        var store = ClubhouseMakeVuex();
        
        app.store = store;

        if (window.theApplicationContext.user && sessionStorage.privateKeyPassphrase) {
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
            if (to.meta.keyRequired) {
                if (store.state.privateKey) {
                    next();
                } else if (sessionStorage.privateKeyPassphrase) {
                    new VueKeyImporter(sessionStorage.privateKeyPassphrase, function() {
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

    $(document).ready(app.init);


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

    app.dmChannelName = function() {
        
    };


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
