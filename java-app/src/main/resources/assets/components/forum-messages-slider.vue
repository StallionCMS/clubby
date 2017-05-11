
<style lang="scss">
 .forum-messages-slider-vue {
     .cb-slider-vertical {
         height: 220px;
         width: 10px;
         border-top: 1px solid transparent;
         border-left: 1px solid #CCC;
         border-radius: 3px;
         .slider-handle {
             cursor: pointer;
             height: 40px;
             width: 100px;
             margin-left: -3px;
             border-left: 6px solid #999;
             padding-left: 4px;
         }
     }
     
 }
</style>

<template>
    <div class="forum-messages-slider-vue " style="">
        <div v-if="messagesIdDates && messagesIdDates.length" class="cb-slider-vertical">
            <div class="slider-handle" :style="{'marginTop': handleY + 'px'}" draggable="true" @drag="drag($event)" @dragstart="dragStart($event)" @dragend="dragEnd($event)">{{ targetPostIndex }} / {{ messagesIdDates.length }}<br>{{targetPostDate }}</div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     props: {
         messagesIdDates: {
             type: Array
         },
         viewingMessageIndex: {
             default: 0
         },
         viewingMessage: {
             default: function() {
                 return {};
             }
         }
             
     },
     data: function() {
         return {
             startY: 0,
             handleY: 0,
             targetPostIndex: 1,
             targetPostDate: 'Feb 9th',
         };
     },
     mounted: function() {
         return {

         }
     },
     watch: {
         'viewingMessageIndex': function(newIndex) {
             console.log('new index ', newIndex);
             var newY = parseInt(200 * newIndex / this.messagesIdDates.length, 10);
             console.log('handleY ', newY);
             this.handleY = newY;
             this.targetPostIndex = (newIndex + 1);
         }
     },
     methods: {
         drag: function(evt) {
             console.log('drag' , evt);
             if (evt.screenY === 0) {
                 return;
             }
             var y = evt.screenY - this.startY;
             if (y > 200) {
                 y = 200;
             }
             if (y < 0) {
                 y = 0;
             }
             this.handleY = y;
             console.log('handleY ', this.handleY);

             var percent = this.handleY / 200.0;
             var i = Math.round(percent * (this.messagesIdDates.length-1));
             console.log(percent, i);
             var idTimestamp = this.messagesIdDates[i];
             console.log(percent, i, idTimestamp[0], idTimestamp[1]);

             var date = moment.tz(idTimestamp[1], moment.tz.guess()).format('h:mm a');
             this.targetPostIndex = i + 1;
             this.targetPostDate = date;
         },
         dragEnd: function(evt) {
             console.log('dragEnd' , evt);
             var percent = this.handleY / 200.0;
             var i = Math.round(percent * (this.messagesIdDates.length-1));
             console.log(percent, i);
             var idTimestamp = this.messagesIdDates[i];
             console.log(percent, i, idTimestamp[0], idTimestamp[1]);
             // Index is one less than the viewable index
             this.$emit('scrollpost', this.targetPostIndex - 1);
         },
         dragStart: function(evt) {
             console.log('drag start', evt.screenY, evt);
             this.startY = evt.screenY - this.handleY;
             var dragIcon = document.createElement('img');
             dragIcon.src = 'https://upload.wikimedia.org/wikipedia/commons/5/52/Spacer.gif';
             
             evt.dataTransfer.setDragImage(dragIcon, -10, -10);
         }
     }
 };
</script>
