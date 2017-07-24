
var ClubhouseMobileInterop = new function() {
    var self = this;

    self.isElectron = window.isInElectron !== undefined;

//    self.isElectron = electronInterop !== undefined;
    //alert(self.isElectron);

    
    self.isMobile = window.orientation !== undefined;

    self.isAppMode = self.isElectron || self.isMobile;

    self.isAndroidApp = window.location.search.indexOf('appBrowser=android') > -1;
    self.isIOSApp = window.location.search.indexOf('appBrowser=ios') > -1;

    self.isMobileApp = self.isIOSApp || self.isAndroidApp;

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
