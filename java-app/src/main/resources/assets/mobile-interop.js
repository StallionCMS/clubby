
var ClubhouseMobileInterop = new function() {
    var self = this;

    self.isMobile = window.orientation !== undefined;

    self.isAndroidApp = navigator.userAgent === 'clubhouse-mobile-app-android';
    self.isIOSApp = navigator.userAgent === 'clubhouse-mobile-app-ios';

    self.isMobileApp = navigator.userAgent.indexOf('clubhouse-mobile-app') === 0


    if (self.isMobileApp) {
        $('#st-clubhouse-body').addClass('clubhouse-mobile-app')
    }

    self.toggleNavigation = function() {
        var store = window.ClubhouseVueApp.$store;
        console.log('toggle navigation ', store);
        store.commit('sidebarPoppedUp', !store.state.sidebarPoppedUp);
    }
    
    
}();
