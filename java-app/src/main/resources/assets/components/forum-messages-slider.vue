
<style lang="scss">
 .forum-messages-slider-vue {
     .cb-slider-vertical {
         height: 220px;
         width: 10px;
         border-top: 1px solid transparent;
         border-left: 1px solid #CCC;
         border-radius: 3px;
         z-index: 100;
         .slider-handle {
             z-index: 200;
             cursor: pointer;
             height: 40px;
             width: 100px;
             margin-left: -3px;
             border-left: 6px solid #337ab7;
             padding-left: 4px;
             font-weight: 600;
         }
     }
     
 }
</style>

<template>
    <div class="forum-messages-slider-vue " style="">
        <div v-if="messagesIdDates && messagesIdDates.length" class="cb-slider-vertical" @dragover="dragOver($event)">
            <div class="slider-handle" :style="{'marginTop': handleY + 'px'}" draggable="true" @drag="drag($event)" @dragstart="dragStart($event)" @touchmove="touchMove($event)" @dragend="dragEnd($event)">{{ targetPostIndex }} / {{ messagesIdDates.length }}<br>{{targetPostDate }}</div>
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
             dragging: false,
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
             this.handleY = newY || 0;
             this.targetPostIndex = (newIndex + 1);
         }
     },
     methods: {
         dragOver: function(evt) {
             console.log('drag over');
         },
         touchMove: function(evt) {
             console.log('touch move');
             this.drag(evt);
         },
         drag: function(evt) {
             if (!this.dragging) {
                 console.log('drag not started yet');
                 return;
             }
             var eventY = evt.screenY;
             if (evt.screenY === undefined) {
                 eventY = evt.touches[0].clientY;
             }
             console.log('drag' , eventY, this.startY, evt);
             if (eventY === 0) {
                 return;
             }
             if (isNaN(this.startY) || isNaN(eventY) || isNaN(this.handleY)) {
                 console.log('exit drag function');
                 return;
             }
             var y = eventY - this.startY;
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

             
             var date = moment.tz(idTimestamp[1], moment.tz.guess());
             var dateFormatted = date.format('h:mm a');
             if (date.isBefore(moment().subtract(300, 'days'))) {
                 dateFormatted = date.format('MMM D, YYYY');
             } else if (date.isBefore(moment().subtract(30, 'days'))) {
                 dateFormatted = date.format('MMM D');
             } else if (date.isBefore(moment().subtract(1, 'days'))) {
                 dateFormatted = date.format('MMM D');
             }
             this.targetPostIndex = i + 1;
             this.targetPostDate = dateFormatted;
         },
         dragEnd: function(evt) {
             console.log('dragEnd' , evt);
             this.dragging = false;
             var percent = this.handleY / 200.0;
             var i = Math.round(percent * (this.messagesIdDates.length-1));
             console.log(percent, i);
             var idTimestamp = this.messagesIdDates[i];
             console.log(percent, i, idTimestamp[0], idTimestamp[1]);
             // Index is one less than the viewable index
             this.$emit('scrollpost', this.targetPostIndex - 1);
         },
         dragStart: function(evt) {
             //this.startY = evt.screenY - this.handleY;
             if (!evt.sourceCapabilities || evt.sourceCapabilities.firesTouchEvents) {
                 this.startY = evt.pageY - this.handleY;
             } else {
                 this.startY = evt.screenY - this.handleY;
             }
             console.log('drag start', evt.pageY, evt.screenY, this.startY, evt);
             var dragIcon = document.createElement('img');
             dragIcon.src = 'https://upload.wikimedia.org/wikipedia/commons/5/52/Spacer.gif';
             
             evt.dataTransfer.setDragImage(dragIcon, -10, -10);
             this.dragging = true;
         }
     }
 };
</script>
