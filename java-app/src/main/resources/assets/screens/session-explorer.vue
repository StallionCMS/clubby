
<style lang="scss">
 .session-explorer-vue {

 }
</style>


<template>
    <div class="session-explorer-vue">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-if="!isLoading">
            <textarea style="width: 500px; height: 100vh;" :value="text"></textarea>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             isLoading: true,
             text: ''
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
             var self = this;
             this.fetchData();
         },
         fetchData: function() {
             var self = this;
             stallion.request({
                 url: '/clubhouse-api/admin/session-explorer?userId=' + (self.$route.query.userId || 10000),
                 success: function(o) {
                     self.isLoading = false;
                     self.text = JSON.stringify(o, null, 4);
                 }
             });
         }
     }
 };
</script>
