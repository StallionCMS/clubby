
<style lang="scss">
 .thread-edit-modal-vue {
     .modal-dialog {
         width: 90%;
         max-width: 880px;
     }
 }
</style>

<template>
    <div class="thread-edit-modal-vue">
        <modal-base ref="themodal" v-on:close="$emit('close')" :title="'Edit post: ' + message.title">
            <div slot="body">
                <forum-create-or-edit-post @close="$refs.themodal.close();$emit('close')" :message="message" :the-channel="channel" :the-members="members"></forum-create-or-edit-post>
            </div><!-- end .body -->
            <div slot="footer">
            </div>
        </modal-base>
    </div>
</template>

<script>
 module.exports = {
     props: {
         message: null,
         members: null,
         channel: null
     },
     data: function() {
         return {

         }
     },
     created: function() {
         
     },
     methods: {

     }
 }
</script>
