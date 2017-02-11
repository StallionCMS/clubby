<style lang="scss">
 .channel-sidebar-vue {
     background-color: #252a4e;
     width: 220px;
     padding: 0px;
     color: #aaadc3;
     height: 101vh;
     margin-top: -1px;
     border-top: 1px solid #252a4e;
     float: left;
     position: fixed;
     h3 {
         font-size: 12px;
         text-transform: uppercase;
         padding-left: 20px;
         margin-bottom: 0px;
     }
     h2 {
         font-size: 20px;
         color: #F8F8F8;
         padding-left: 20px;
         margin-top: 1em;
         margin-bottom: 0px;
     }
     a {
         color: #aaadc3;
         display: block;
         padding-left: 22px;
         padding-top: 4px;
         padding-bottom: 4px;
     }
     a.active {
         background-color: #5d4877;
     }
     .explain {
         font-style: italic;
         padding-left: 20px;
     }
 }
</style>

<template>
    <div class="channel-sidebar-vue">
        <h2>My Clubhouse</h2>
        <h3>Forums</h3>
        <div class="explain">No forums</div>
        <h3>Channels</h3>
        <a :class="{active: channel.id === activeChannelId}" v-for="channel in channels" :href="'#/channel/' + channel.id"># {{ channel.name }}</a>        
        <h3>Direct Messages</h3>
        <div class="explain">No message history</div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             channels: []
         }
     },
     computed: {
         activeChannelId: function() {
             return stallionClubhouseApp.store.state.activeChannelId;
         },
         channels: function() {
             return [
                 {
                     name: 'general',
                     id: 10500
                 },
                 {
                     name: 'humor',
                     id: 10501
                 }                 
             ];
         }
     },
     watch: {

     },     
     methods: {
         
     }
 }
</script>
