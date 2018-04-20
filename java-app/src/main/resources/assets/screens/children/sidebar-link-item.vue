
<style lang="scss">
 .sidebar-link-item-vue {
 }
</style>

<template>
    <div class="sidebar-link-item-vue">
        <a v-if="channel.channelType=='DIRECT_MESSAGE'" :class="{'channel-unread': channel.hasNew, 'direct-message-link': true, 'channel-link': true, active: channel.id === activeChannelId}" :href="'#/channel/' + channel.id">
            <span v-if="channel.directMessageUserIdsList.length===1" class="username-span" ><span class="user-awake-bubble" v-if="$store.state.allUsersById[channel.directMessageUserIdsList[0]].state==='AWAKE'">&#9679;</span><span class="user-offline-bubble" v-else>&#9675;</span>{{$store.state.allUsersById[channel.directMessageUserIdsList[0]].username}}</span>
            <span v-else>
                <span class="dm-user-count">{{ channel.directMessageUserIdsList.length }}</span>
                <span class="username-span" v-for="userId,index in channel.directMessageUserIdsList" v-if="userId != $store.state.user.id">{{ $store.state.allUsersById[userId].username }}<span v-if="index < (channel.directMessageUserIdsList.length-1)">,</span></span>
            </span>
            <span v-if="channel.mentionsCount>0" class="unread-mentions-count">{{ channel.mentionsCount }}</span>
        </a>        
        <a v-if="channel.channelType!=='DIRECT_MESSAGE'" :class="{'channel-unread': channel.hasNew, 'channel-link': true, active: channel.id === activeChannelId}" :href="link">
            <span v-if="!channel.inviteOnly && channel.channelType ==='CHANNEL'" style="font-size:12px;" class="material-icons">#</span>
            <span v-if="!channel.inviteOnly && channel.channelType ==='DIRECT_MESSAGE'" style="font-size:12px;" class="material-icons"></span>
            <i v-if="!channel.inviteOnly && channel.channelType ==='FORUM'" style="font-size:12px;" class="material-icons">forum</i>
            <i v-if="channel.encrypted && channel.inviteOnly" style="font-size:12px;" class="material-icons">enhanced_encryption</i>
            <i v-if="!channel.encrypted && channel.inviteOnly" style="font-size:12px;" class="material-icons">lock</i>
            <span>{{ channel.name }}</span>
            <span v-if="channel.mentionsCount>0" class="unread-mentions-count">{{ channel.mentionsCount }}</span>
        </a>
    </div>
</template>

<script>
 module.exports = {
     props: {
         activeChannelId: null,
         channel: {
             type: Object,
             required: true
         }
     },
     data: function() {
         return {
         };
     },
     computed: {
         link: function() {
             if (this.channel.channelType === 'FORUM') {
                 return '#/forum/' + this.channel.id
             } else {
                 return '#/channel/' + this.channel.id
             }
         }
     },
     methods: {
     }
 };
</script>
