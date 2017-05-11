<template>
    <textarea data-emojiable="true" @keydown.enter="onEnter" class="autogrow-textarea form-control autogrow-single-line autogrow-short" @input="onInput" v-user-autocomplete></textarea>
</template>

<script>
 module.exports = {
     props: {
         value: ''
     },
     data: function() {
         return {

         };
     },
     mounted: function() {
         this.$el.value = this.value;
         stallion.autoGrow({}, $(this.$el));
         //new EmojiPicker({
         //    emojiable_selector: this.$el,
         //    assetsPath: '/st-resource/clubhouse/emoji/img'
         //}).discover();
         //$(this.$el).emojiarea();
     },
     methods: {
         onEnter: function(evt) {
             if (evt.shiftKey) {
                 return true;
             }
             evt.stopPropagation();
             evt.preventDefault();
             if (this.value && !$(this.$el).data('autocompleting')) {
                 this.$emit('submit');
             }
         },
         onInput: function(a, b) {
             this.$emit('input', this.$el.value);
         }
     },
     watch: {
         value: function(newVal) {
             this.$el.value = newVal;
         }
     }
 };
</script>

