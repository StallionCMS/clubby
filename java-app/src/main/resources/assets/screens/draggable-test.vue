
<style lang="scss">
 .draggable-test-vue {
     padding: 50px;
 }
</style>


<template>
    <div class="draggable-test-vue  one-column-screen">
        <div draggable="true" @drag="drag($event)" @dragstart="dragStart($event)" @dragend="dragEnd($event)" style="width: 50px; height: 50px; background-color: #888;">dfasdf</div>
    </div>
</template>

<script>

 
 module.exports = {
     data: function() {
         return {
             
         };
     },
     created: function() {
         this.onRoute();
     },
     watch: {
         '$route': 'onRoute'
     },
     methods: {
         onRoute: function() {
             alert('onRoute');
             console.log('on route');
         },
         dragStart: function(evt) {
             console.log('dragStart', evt);
         },
         drag: function(evt) {
             console.log('drag', evt);
         },
         dragEnd: function(evt) {
             alert('drag end');
             console.log('dragEnd', evt);
         }
     }
 };


</script>
