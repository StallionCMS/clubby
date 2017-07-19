
<style lang="scss">
 .mobile-login-vue {

 }
</style>


<template>
    <div class="mobile-login-vue">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-if="!isLoading">
            
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             isLoading: true,
         };
     },
     created: function() {
         this.onRoute();
     },
     watch: {
         '$route': 'onRoute'
     },
     methods: {
         onRoute: function() {
             alert('he');
             console.log('mobile login onRoute');
             var self = this;
             var passphrase = self.$route.query.passphrase;
             if (!passphrase || !self.$store.state.userProfile) {
                 console.log('no passphrase or profile passphrase: ' + passphrase + 'profile: ' + self.$store.state.userProfile + ' location: ' + window.location.hash + ' full location ' + window.location.href);
                 window.location.hash = '/login';
                 return;
             }
             clubhouseImportPublicAndPrivateKey(
                 passphrase,
                 self.$store.state.userProfile
             ).then(function() {
                 console.log('mobile-login private key successful');
                 sessionStorage['private-key-passphrase-' + self.$store.state.user.id] = passphrase;
                 ClubhouseVueApp.stateManager.start();
                 ClubhouseVueApp.stateManager.loadContext(function(ctx) {
                     Vue.nextTick(function() {
                         console.log('navigate home after tick');
                         window.location.hash = '/';
                     });
                 });
                 console.log('nagivate home');
                 ClubhouseMobileInterop.updateNameAndIcon();
                 window.location.hash = '/';
             }).catch(function(err) {
                 console.log('error importing private key' , err);
                 stallion.showError('Error importing private key. Please check your passphrase');
                 window.location.hash = '/login';
             });
         }
     }
 };
</script>
