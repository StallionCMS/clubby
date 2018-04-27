
<style lang="scss">
 .ws-connection-toast-vue {

 }
</style>

<template>
    <div class="ws-connection-toast-vue">
        <div id="ws-connection-toast" v-if="$store.state.websocketStatus.state !== 'OPEN' && $store.state.websocketStatus.state !== 'PENDING' ">
            <div v-if="$store.state.websocketStatus.state === 'OPEN'">
                Web socket connected.
            </div>
            <div v-if="$store.state.websocketStatus.state === 'PENDING'">
                Setting up web socket connection...
            </div>
            <div v-if="$store.state.websocketStatus.state === 'ERROR'">
                Error setting up listening connection to server.
            </div>
            <div v-if="$store.state.websocketStatus.state === 'CLOSED'">
                Connection to server lost. Reconnecting in {{ $store.state.websocketStatus.reconnectIn }} seconds. Reconnect has failed {{ $store.state.websocketStatus.failedCount }} times.
            </div>                        
        </div>
    </div>
</template>

<script>
 module.exports = {
     props: {

     },
     data: function() {
         return {
         };
     },
     methods: {
     }
 };
</script>
