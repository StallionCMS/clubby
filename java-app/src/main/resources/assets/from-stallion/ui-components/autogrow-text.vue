<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->

<style lang="scss">
 .autogrow-text-vue {
     resize: none;
 }
</style>

<template>
    <textarea @keydown.enter.prevent.stop="onEnter" class="autogrow-textarea form-control autogrow-single-line autogrow-short autogrow-text-vue" @input="onInput" :required="required"></textarea>
</template>

<script>
 module.exports = {
     props: {
         value: '',
         required: {
             default: undefined
         }
     },
     mounted: function() {
         this.$el.value = this.value;
         stallion.autoGrow({}, $(this.$el));
     },
     methods: {
         onEnter: function(evt) {
             evt.stopPropagation();
             evt.preventDefault();
             console.log('enter clicked');
             // do nothing, eat the enter key
             return false;
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
