
var ClubhouseMobileInterop = new function() {
    var self = this;

    self.isElectron = window.isInElectron !== undefined;

//    self.isElectron = electronInterop !== undefined;
    //alert(self.isElectron);

    
    self.isMobile = window.orientation !== undefined;

    self.isAndroidApp = window.AndroidInterface !== undefined;
    self.isIOSApp = window.location.search.indexOf('appBrowser=ios') > -1;

    self.isMobileApp = self.isIOSApp || self.isAndroidApp;
    self.isAppMode = self.isElectron || self.isMobileApp;
    

    if (self.isMobileApp) {
        $('#st-clubhouse-body').addClass('clubhouse-mobile-app')
    }
    if (self.isAndroidApp) {
        $('#st-clubhouse-body').addClass('clubhouse-android-app')
    }
    if (self.isIOSApp) {
        $('#st-clubhouse-body').addClass('clubhouse-ios-app')
    }

    self.toggleNavigation = function() {
        var store = window.ClubhouseVueApp.$store;
        console.log('toggle navigation ', store);
        store.commit('sidebarPoppedUp', !store.state.sidebarPoppedUp);
    }

    self.redirectToLogin = function() {
        if (window.webkit) {
            webkit.messageHandlers.requiresLoginHandler.postMessage("redirect-to-login");
        } else if (self.isElectron) {
            electronInterop.redirectToLogin();
        }
    }

    self.tellAppToUpdateMentions = function() {
        if (window.webkit && webkit.messageHandlers.pollForMentions) {
            webkit.messageHandlers.pollForMentions.postMessage("{}");
        } else if (self.isAndroidApp) {
            AndroidInterface.onUnreadStatusChanged();
        } else if (self.isElectron) {
            electronInterop.tellAppToUpdateMentions();
        }
    }

    self.markRouteLoaded = function() {
        if (window.webkit && webkit.messageHandlers.markRouteLoaded) {
            webkit.messageHandlers.markRouteLoaded.postMessage("{}");
        } else if (self.isElectron) {
            electronInterop.markRouteLoaded();
        }
    }

    self.onLoggedIn = function(userId, username, authcookie, password) {
        var message = {
                userId: userId,
                username: username,
                authcookie: authcookie,
                password: password
        }
        if (self.isElectron) {
            electronInterop.onLoggedIn(userId, username, authcookie, password);
        } else if (self.isAndroidApp) {
            AndroidInterface.onLoggedIn(theApplicationContext.site.siteUrl, userId, username, authcookie, password);
        } else if (window.webkit && webkit.messageHandlers.onLoggedIn) {
            webkit.messageHandlers.onLoggedIn.postMessage(JSON.stringify(message))
        }
    }
    
    self.updateNameAndIcon = function() {
        //alert('hi!');
        //alert('' + ClubhouseVueApp);
        var state = ClubhouseVueApp.$store.state;
        var message = {
            iconBase64: state.site.iconBase64,
            siteName: state.site.name,
            siteUrl: state.site.siteUrl,
            username: state.user.username
        }
        
        if (window.webkit && webkit.messageHandlers.updateNameAndIcon) {
            webkit.messageHandlers.updateNameAndIcon.postMessage(JSON.stringify(message));
        } else if (self.isElectron) {
            electronInterop.updateNameAndIcon();
        }

    }
    
    
}();
