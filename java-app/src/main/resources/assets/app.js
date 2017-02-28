console.log("executing app.js");

(function() {
    window.stallionClubhouseApp = window.stallionClubhouseApp || {};
    var app = window.stallionClubhouseApp;

    app.init = function() {
        var Bar = { template: '<div>bar</div>' }


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
                path: '/channel/:channelId',
                component: 'channel-feed',
                meta: {
                    keyRequired: true
                }
            },
            {
                path: '/open-direct-message',
                component: 'open-direct-message',
                meta: {
                    keyRequired: true
                }
            },
            {
                path: '/',
                component: 'app-home-v3'
            }            
        ];

        var store = new Vuex.Store({
            state: {
                user: null,
                userProfile: null,
                publicKey: null,
                privateKey: null,
                activeChannelId: null
            },
            mutations: {
                publicKey: function(state, publicKey) {
                    state.publicKey = publicKey;
                },
                privateKey: function(state, privateKey) {
                    state.privateKey = privateKey;
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
        vueApp.$mount('#vue-app');

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
        

        function setupWebSocket() {
            window.clubhouseSocket = new WebSocket("wss://clubhouse.local/st-wsroot/events/?stUserSession=" + encodeURIComponent(stallion.getCookie("stUserSession")));
            console.log('setup web socket.');
            window.clubhouseSocket.onmessage = function (event) {
                console.log(event.data);
                var data = JSON.parse(event.data);
                if (data.type === 'new-message') {
                    console.log('new-message');
                    if (window.ClubhouseVueApp.currentChannelComponent) {
                        console.log('handleIncomingMessage');
                        window.ClubhouseVueApp.currentChannelComponent.handleIncomingMessage(data.message, data, event);
                    }
                }
            }
            window.clubhouseSocket.onclose = function(event) {
                console.log('web socket closed, re-setup');
                setTimeout(setupWebSocket, 200);
            }
        }
        setupWebSocket();

    
        
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
