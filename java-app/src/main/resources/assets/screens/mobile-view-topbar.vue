
<style lang="scss">
 .mobile-view-topbar-vue {
     margin-top: -1px;
     .topbar-link {
         position: fixed;
         z-index: 200;
         background-color: white;
         height: 50px;
         width: 100%;
         display: block;
         font-size: 28px;
         text-decoration: none;
         
         padding: 4px 0px 4px 8px;
         border-bottom: 1px solid #999;
         background-color: #333;
         color: #EEE;
         i {
             vertical-align: -10%;
         }
     }     
     .overlay {
         position: fixed;
         top: 0;
         left: 0;
         height: 100vh;
         width: 100vw;
         z-index: 245;
         background-color: rgba(100, 100, 100, .5);
     }
     
 }
</style>


<template>
    <div class="mobile-view-topbar-vue">
        <a class="topbar-link" href="javascript:;" @click="popupSidebar">
            <i class="material-icons">menu</i>
            {{ $store.state.site.name }}
        </a>
        <div v-show="$store.state.sidebarPoppedUp" class="overlay" @click="closeSidebar">

        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {

         };
     },
     created: function() {

     },
     watch: {

     },
     methods: {
         popupSidebar: function() {
             this.$store.commit('sidebarPoppedUp', true);
         },         
         closeSidebar: function() {
             this.$store.commit('sidebarPoppedUp', false);
         }
     }
 };
</script>
