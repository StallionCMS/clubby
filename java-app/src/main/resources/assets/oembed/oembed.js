


$(document).ready(function() {
    console.log('embed');
    
    function updateParentHeight() {
        $('a').each(function() {
            this.setAttribute('target', '_blank');
        });
        parent.postMessage({
            iframeHeight: $('body').outerHeight(true),
            iframeId: iframeId
        }, 'https://clubhouse.local');
        console.log('message posted!');
    }
    var watchedCount = 0;
    var theHeight = 0;
    var theInterval = setInterval(function() {
        watchedCount++;
        var newHeight = $('body').outerHeight(true);
        if ($('#container').html() === '') {
            return;
        }
        if (newHeight !== theHeight) {
            theHeight = newHeight;
            updateParentHeight();
        }
        if (watchedCount > 100) {
            clearInterval(theInterval);
        }
    }, 2000);
    $("#container").oembed(embedUrl, {embedMethod: 'fill'});
});    
