<style lang="scss">
 .open-direct-message-vue {
     .member-row {
         display: block;
         padding: 0px;
         line-height: 0px;;
     }
     .member-row.odd {
         background-color: #E7E7E7;
     }
     .member-row:hover {
     }
     .open-channel-link {
         padding-top: .7em;
         padding-bottom: .7em;
         display: inline-block;
         width: 350px;
         text-wrap: nowrap;
         overflow: hidden;
     }
     .open-channel-link:hover {
         text-decoration: none;
         background-color: #DDD;
     }
     .multi-add-button {
         display: inline-block;
         vertical-align: 100%;
     }
     
     .avatar-image {
         max-width: 30px;
         max-height: 40px;
         width: 30px;
         margin-right: 8px;
     }
     .member-row {

     }
     .member-name {
         display: inline-block;
         margin-right: 10px;
         font-size: 16px;
     }
 }
</style>

<template>
    <div class="open-direct-message-vue">
        <div class="one-column-screen">
            <h3>Direct Messages</h3>
            <div class="p">
                <input type="text" class="form-control" placeholder="Find user" autofocus="autofocus" v-model="userSearch">
            </div>
            <div class="p" v-if="chosenMembers.length">
                <em>Open group direct message with:</em>
                <span v-for="(member, index) in chosenMembers"><b>{{ member.displayName }}</b><span v-if="index!=members.length-1">, </span></span>
                <div class="p">
                    <button @click="openDirectMessage(chosenMembers)" class="btn btn-md btn-danger">Open {{ chosenMembers.length }} person chat &#187;</button>
                </div>
            </div>
            <div :class="['member-row', index % 2===0 ? 'even' : 'odd']" v-for="(member, index) in otherUsers"> 
                <a class="open-channel-link"     @click="openDirectMessage(member)" href="javascript:;">
                    <img class="avatar-image" :src="member.avatarUrl">
                    <span class="member-name">{{ member.displayName }}</span>
                    (@{{ member.username }})
                </a>
                <button v-if="!member.chosen" class="btn btn-md btn-danger multi-add-button" @click="multiAdd(member, $event)">+</button>
                <button v-if="member.chosen" class="btn btn-md btn-default multi-add-button" @click="multiRemove(member, $event)">&#8722;</button>
            </div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         var self = this;
         var members = JSON.parse(JSON.stringify(self.$store.state.allUsers));
         members.forEach(function(m) {
             m.chosen = false;
         });
         return {
             userSearch: '',
             chosenMembers: [],
             members: members,
             user: this.$store.state.user
         };
     },
     computed: {
         otherUsers: function() {
             var self = this;
             var search = self.userSearch.toLowerCase();
             return self.members.filter(function(user) {
                 if (user.id == self.user.id) {
                     return false;
                 }
                 if (self.userSearch) {
                     if (user.displayName.toLowerCase().indexOf(search) > -1) {
                         return true;
                     }
                     if (user.username.toLowerCase().indexOf(search) > -1) {
                         return true;
                     }
                     return false;
                 }
                 return true;
             });
         }
     },
     created: function() {
         this.fetchData();
     },
     watch: {
         '$route': 'fetchData',
     },     
     methods: {
         multiAdd: function(member, event) {
             member.chosen = true;
             this.chosenMembers.push(member);
         },
         multiRemove: function(member, event) {
             var chosenMembers = [];
             this.chosenMembers.forEach(function(m) {
                 if (m.id !== member.id) {
                     chosenMembers.push(m);
                 }
             });
             member.chosen = false;
             this.chosenMembers = chosenMembers;
         },
         fetchData: function() {
         },
         openDirectMessage: function(members) {
             var ids = [];
             if (members.length === undefined && members.id > 0) {
                 ids = [members.id];
             } else {
                 ids = members.map(function(m) { return m.id});
             }
             stallion.request({
                 method: 'POST',
                 data: {
                     userIds: ids,
                 },
                 url: '/clubhouse-api/messaging/open-direct-message',
                 success: function(o) {
                     window.location.hash = '/channel/' + o.channel.id;
                 }
             });
         }
     }
 }
</script>
