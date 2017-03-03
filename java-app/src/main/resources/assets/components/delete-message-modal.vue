<style lang="scss">
 .delete-message-modal-vue {

 }
</style>

<template>
    <div class="delete-message-modal-vue">
        <modal-base ref="themodal" v-on:close="$emit('close')" :title="'Delete message?'">
            <div slot="body" class="modal-body">
                <div>Are you sure you want to delete this message? This cannot be undone.</div>
                <div class="well" >
                    <div class="p"><strong>{{ message.fromUsername }}</strong></div>
                    <br>
                    <div v-html="message.html"></div>
                </div>
            </div>
            <div slot="footer">
                <button @click="$refs.themodal.close()" class="btn btn-xl btn-default">Cancel</button>
                <button @click="deleteThisMessage" class="btn btn-xl btn-danger">Yes, delete this message</button>
            </div>
        </modal-base>
    </div>
</template>

<script>
 module.exports = {
     props: {
         message: null
     },
     data: function() {
         return {
             
         };
     },
     methods: {
         deleteThisMessage: function() {
             this.$emit('delete', this.message);
             this.$refs.themodal.close();
         }
     }
 };
</script>
