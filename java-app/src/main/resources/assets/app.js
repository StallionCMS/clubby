console.log("executing app.js");

(function() {
    window.stallionClubhouseApp = window.stallionClubhouseApp || {};
    var app = window.stallionClubhouseApp;
    var sentRouteLoadedMessage = false;

    
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
                component: vueComponents['clubhouse-login-v3'],
                meta: {
                    authRequired: false
                }                
            },
            /*
            {
                path: '/key-login',
                component: vueComponents['login-with-key'],
                meta: {
                    authRequired: false
                }                
            },
            */
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
                path: '/encryption-test',
                component: vueComponents['encryption-test'],
                meta: {
                    authRequired: false
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
                    keyRequired: true,
                    authRequired: true
                }
            },
            {
                path: '/forum/:channelId',
                component: vueComponents['forum-top-level'],
                meta: {
                    keyRequired: true,
                    authRequired: true
                }
            },
            {
                path: '/forum/:channelId/new-thread',
                component: vueComponents['forum-create-or-edit-post'],
                meta: {
                    keyRequired: true,
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
                    keyRequired: true,
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
                path: '/clubhouse-settings/:tab',
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
                path: '/draggable-test',
                component: vueComponents['draggable-test'],
                meta: {
                    authRequired: false
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

        setTimeout(10000, function() {
            $store.state.appLoading = false;
        });

        router.beforeEach(function(to, from, next) {
            // No auth required for the view? Carry on.
            console.log('go to ', to);
            if (!to.meta.keyRequired && to.meta.authRequired === false) {
                if (to.path.indexOf('/login') === 0 && to.query.password && to.query.username) {

                } else {
                    store.commit('appLoading', false);
                }
                
                next();
                return;
            }
            
            if (app.store.state.user && app.store.state.user.id) {
                if (store.state.privateKey) {
                    next();
                    return;
                } else if (sessionStorage['private-key-passphrase-' + app.store.state.user.id]) {
                    clubhouseImportPublicAndPrivateKey(
                        sessionStorage['private-key-passphrase-' + app.store.state.user.id],
                        app.store.state.userProfile
                    ).then(function() {
                        next();
                    }).catch(function(err) {
                        
                        console.error('PRIVATE KEY LOGIN ERROR ', err);
                        if (ClubhouseMobileInterop.isAppMode) {
                            ClubhouseMobileInterop.redirectToLogin(to.fullPath);
                        } else {
                            next('/login?returnPath=' + encodeURIComponent(to.fullPath));
                        }
                    });
                    return;
                }
            }
            if (ClubhouseMobileInterop.isAppMode) {
                ClubhouseMobileInterop.redirectToLogin(to.fullPath);
            } else {
                next('/login?returnPath=' + encodeURIComponent(to.fullPath));
            }
            return;
        });

        router.afterEach(function(to, from) {
            if (to.params.channelId) {
                store.commit('activeChannelId', parseInt(to.params.channelId, 10));
            } else {
                store.commit('activeChannelId', null);
            }
            if (to.path.indexOf('/login') === 0 && to.query.password && to.query.username) {

            } else {
                store.commit('appLoading', false);
            }

            // Always do this in case there is an error in the normal mark route loaded and
            // we want to see what happened.
            console.log('routed to ', to);

            if (!sentRouteLoadedMessage && store.state.privateKey) {
                
                sentRouteLoadedMessage = true;
                setTimeout(function() {
                    ClubhouseMobileInterop.markRouteLoaded();
                }, 200);                
            }
            

        });


        var vueApp;
        window.ClubhouseVueApp =  vueApp = new Vue({
            store: store,
            router: router,
            watch: {
                '$store.state.websocketStatus': function(newStatus) {
                    console.log('new web socket status', newStatus);
                }
            }
        });
        vueApp.currentChannelComponent = null;
        

        vueApp.stateManager = new ClubhouseGlobalStateManager(vueApp);
        vueApp.stateManager.start();
        
        vueApp.$mount('#vue-app');


        
        
    }

    


    app.sendNotifiction = function(text, data, link) {
        var silent = true;
        var pref = 'SILENT';
        if (ClubhouseMobileInterop.isMobile) {
            pref = ClubhouseVueApp.$store.state.userProfile.mobileNotifyPreference;
        } else {
            pref = ClubhouseVueApp.$store.state.userProfile.desktopNotifyPreference;
        }
        if (pref === 'NONE') {
            return;
        }
        if (pref === 'SOUND') {
            silent = false;
        }
        data.silent = silent;
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
            conv.img_sets.apple.path = '/st-resource/clubby/emoji/sheets/';
            conv.img_sets.apple.sheet = '/st-resource/clubby/emoji/sheets/sheet_apple_64_indexed_128.png';
            // Configure this library to use the sheets defined in `img_sets` (see above)
            conv.use_sheet = true;
        conv.img_set = 'apple';
            conv.replace_mode = 'img';
            conv.text_mode = false;
            app.emojiConverter = conv;


    };

    app.isIdleOrHidden = function() {
        if (window.electronInterop) {
            if (electronInterop.isHidden) {
                return true;
            }
        }
        if (!ifvisible.now()) {
            return true;
        }
        if (ifvisible.now('hidden')) {
            return true;
        }
        if (ifvisible.now('hidden')) {
            return true;
        }
        return false;
    }

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


// From https://gist.github.com/pascaldekloe/62546103a1576803dade9269ccf76330

// Marshals a string to Uint8Array.
function encodeUTF8(s) {
	var i = 0;
	var bytes = new Uint8Array(s.length * 4);
	for (var ci = 0; ci != s.length; ci++) {
		var c = s.charCodeAt(ci);
		if (c < 128) {
			bytes[i++] = c;
			continue;
		}
		if (c < 2048) {
			bytes[i++] = c >> 6 | 192;
		} else {
			if (c > 0xd7ff && c < 0xdc00) {
				if (++ci == s.length) throw 'UTF-8 encode: incomplete surrogate pair';
				var c2 = s.charCodeAt(ci);
				if (c2 < 0xdc00 || c2 > 0xdfff) throw 'UTF-8 encode: second char code 0x' + c2.toString(16) + ' at index ' + ci + ' in surrogate pair out of range';
				c = 0x10000 + ((c & 0x03ff) << 10) + (c2 & 0x03ff);
				bytes[i++] = c >> 18 | 240;
				bytes[i++] = c>> 12 & 63 | 128;
			} else { // c <= 0xffff
				bytes[i++] = c >> 12 | 224;
			}
			bytes[i++] = c >> 6 & 63 | 128;
		}
		bytes[i++] = c & 63 | 128;
	}
	return bytes.subarray(0, i);
}

// Unmarshals an Uint8Array to string.
function decodeUTF8(bytes) {
	var s = '';
	var i = 0;
	while (i < bytes.length) {
		var c = bytes[i++];
		if (c > 127) {
			if (c > 191 && c < 224) {
				if (i >= bytes.length) throw 'UTF-8 decode: incomplete 2-byte sequence';
				c = (c & 31) << 6 | bytes[i] & 63;
			} else if (c > 223 && c < 240) {
				if (i + 1 >= bytes.length) throw 'UTF-8 decode: incomplete 3-byte sequence';
				c = (c & 15) << 12 | (bytes[i] & 63) << 6 | bytes[++i] & 63;
			} else if (c > 239 && c < 248) {
				if (i+2 >= bytes.length) throw 'UTF-8 decode: incomplete 4-byte sequence';
				c = (c & 7) << 18 | (bytes[i] & 63) << 12 | (bytes[++i] & 63) << 6 | bytes[++i] & 63;
			} else throw 'UTF-8 decode: unknown multibyte start 0x' + c.toString(16) + ' at index ' + (i - 1);
			++i;
		}

		if (c <= 0xffff) s += String.fromCharCode(c);
		else if (c <= 0x10ffff) {
			c -= 0x10000;
			s += String.fromCharCode(c >> 10 | 0xd800)
			s += String.fromCharCode(c & 0x3FF | 0xdc00)
		} else throw 'UTF-8 decode: code point 0x' + c.toString(16) + ' exceeds UTF-16 reach';
	}
	return s;
}


