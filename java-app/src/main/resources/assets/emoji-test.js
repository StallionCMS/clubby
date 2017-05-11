



wdtEmojiBundle.defaults.emojiSheets = {
    'apple': '/st-resource/clubhouse/emoji/sheets/sheet_apple_64_indexed_128.png'
};
wdtEmojiBundle.defaults.type = 'apple';


wdtEmojiBundle.init('.st-clubhouse-emoji-hidden-input');

function chooseEmoji() {
    debugger;
    var inputEle = $(this.$el).find('input.emoji-target-input').get(0);
    wdtEmojiBundle.openPicker.bind(inputEle)(evt, inputEle, 200, 200);    
}
