<template>
    <textarea @keydown.enter="onEnter" class="autogrow-textarea form-control autogrow-single-line autogrow-short" @input="onInput"></textarea>
</template>

<script>
 module.exports = {
     props: {
         value: ''
     },
     mounted: function() {
         this.$el.value = this.value;
         stallion.autoGrow({}, $(this.$el));
     },
     methods: {
         onEnter: function(evt) {
             if (evt.shiftKey) {
                 return true;
             }
             evt.stopPropagation();
             evt.preventDefault();
             if (this.value) {
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

