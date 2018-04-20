<style lang="scss">
 .emoji-popup-vue {
     .wdt-emoji-popup {
         z-index: 100;
     }
 }
</style>

<template>
    <div class="emoji-popup-vue">
        <div class="wdt-emoji-popup ">
            <a href="#" class="wdt-emoji-popup-mobile-closer"> × </a>
            <div class="wdt-emoji-menu-content">
                <div id="wdt-emoji-menu-header">
                    <a class="wdt-emoji-tab active" data-group-name="Recent"></a>
                    <a class="wdt-emoji-tab" data-group-name="People"></a>
                    <a class="wdt-emoji-tab" data-group-name="Nature"></a>
                    <a class="wdt-emoji-tab" data-group-name="Foods"></a>
                    <a class="wdt-emoji-tab" data-group-name="Activity"></a>
                    <a class="wdt-emoji-tab" data-group-name="Places"></a>
                    <a class="wdt-emoji-tab" data-group-name="Objects"></a>
                    <a class="wdt-emoji-tab" data-group-name="Symbols"></a>
                    <a class="wdt-emoji-tab" data-group-name="Flags"></a>
                    <a class="wdt-emoji-tab" data-group-name="Custom"></a>
                </div>
                <div class="wdt-emoji-scroll-wrapper">
                    <div id="wdt-emoji-menu-items">
                        <input id="wdt-emoji-search" type="text" placeholder="Search">
                        <h3 id="wdt-emoji-search-result-title">Search Results</h3>
                        <div class="wdt-emoji-sections"></div>
                        <div id="wdt-emoji-no-result">No emoji found</div>
                    </div>
                </div>
                <div id="wdt-emoji-footer">
                    <div id="wdt-emoji-preview">
                        <span id="wdt-emoji-preview-img"></span>
                        <div id="wdt-emoji-preview-text">
                            <span id="wdt-emoji-preview-name"></span><br>
                            <span id="wdt-emoji-preview-aliases"></span>
                        </div>
                    </div>
                    <div id="wdt-emoji-preview-bundle">
                        <span></span>
                    </div>
                </div>
            </div>
        </div>
        <div style="display:none" >
            <input class="emoji-target-input st-clubhouse-emoji-hidden-input" style="display:none" @input="onInput" type="text" :id="targetId">
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             targetId: 'emoji-target-' + generateUUID(),
             isOpen: false
         };
     },
     methods: {
         toggle: function(evt) {
             $(this.$el).find('input.emoji-target-input').val('');
             wdtEmojiBundle.togglePicker2(
                 '#' + this.targetId,
                 evt.clientX + 30,
                 evt.clientY + 30,
                 {closeOnClick: true}
             );
             
             /*
             
             console.log('toggle emoji picker');
             var self = this;
             self.isOpen = false
             var inputEle = $(this.$el).find('input.emoji-target-input').get(0);
             //wdtEmojiBundle.openPicker.bind(inputEle)(evt, inputEle, evt.clientX, evt.clientY);
             debugger;
             wdtEmojiBundle.openPicker(evt);
             setTimeout(function() {
                 self.isOpen = true;
             }, 50);
             */
             
             //$(this.$el).find('.wdt-emoji-picker').click();
         },
         onInput: function(e, b) {
             console.log('emoji input onInput ', e, e.target.value);
             this.isOpen = false;
             this.$emit('input', e.target.value);
         }
     },
     created: function() {
         wdtEmojiBundle.defaults.emojiSheets = {
             'apple': '/st-resource/clubby/emoji/sheets/sheet_apple_64_indexed_128.png'
         };
         wdtEmojiBundle.defaults.type = 'apple';
         
     },
     mounted: function() {
         wdtEmojiBundle.init('.st-clubhouse-emoji-hidden-input');
         var self = this;
         $('#clubhouse-main-view').click(function(e) {
             if (e.target.id == "myDiv" || $(e.target).parents('.emoji-popup-vue').size()) { 

             } else {
                 if (self.isOpen) {
                     console.log('clicked outside, close the emoji popup');
                     $(self.$el).find('.wdt-emoji-popup.open')
                           .removeClass('open');
                     $(self.$el).find('.wdt-emoji-picker-open').removeClass('wdt-emoji-picker-open');
                     self.isOpen = false;
                     self.$emit('close');
                 }
             }             
             //wdtEmojiBundle.closePickers();
             
         });         
     }
 };
</script>

