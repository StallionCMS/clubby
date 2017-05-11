
<style lang="scss">
 .username-auto-complete-vue {

 }
</style>

<template>
    <div class="username-auto-complete-vue">
        <div>Mention user {{ term }}</div>
        <div v-for="user in users">
            <span>{{ user.username }}</span>
        </div>
    </div>
</template>

<script>
 module.exports = {
     props: {
         term: '',
     },
     data: function() {
         return {
         };
     },
     mounted: function() {
         
     },
     computed: {
         users: function() {
             var self = this;
             var filteredUsers = [];
             this.$store.state.allUsers.forEach(function(user) {
                 if (user.username.toLowerCase().indexOf(self.term) > -1 || user.displayName.toLowerCase().indexOf(self.term) > -1) {
                     filteredUsers.push(user);
                 }
             });
             return filteredUsers;
         }
     },
     methods: {
     }
 };
</script>
