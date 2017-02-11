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
                component: 'channel-feed'
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
                },
                activeChannelId: function(state, channelId) {
                    console.log('set active channelid ', channelId);
                    state.activeChannelId = channelId;
                }
            }            
        });


        app.store = store;

        
        var router = new VueRouter({
            store: store,
            routes: routes
        });

        router.afterEach(function(to, from) {
            if (to.params.channelId) {
                store.commit('activeChannelId', parseInt(to.params.channelId, 10));
            } else {
                store.commit('activeChannelId', null);
            }
        });

        
        var vueApp = new Vue({
            store: store,
            router: router
        }).$mount('#vue-app')

    }

    $(document).ready(app.init);
     
}());
    
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
