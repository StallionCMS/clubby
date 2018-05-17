(function() {

    window.ClubhouseMessageAutoSaver = {};
    var saver = window.ClubhouseMessageAutoSaver;

    saver.clearInProgress = function(channelId, threadId, postId) {
        console.debug('clearInProgress');
        delete localStorage['in-progress-' + channelId + '-' + threadId + '-' + postId];
    };

    saver.isInProgress = function(channelId, threadId, postId) {
        var inProgressKey = 'in-progress-' + channelId + '-' + threadId + '-' + postId;
        var found = localStorage[inProgressKey];
        return found;
    }

    saver.autoSave = function(channelId, threadId, postId, text, widgets) {
        threadId = threadId || 0;
        postId = postId || 0;
        var values = saver.loadAutoSaves();
        var newItem = {
            savedAt: new Date().getTime(),
            text: text,
            widgets: widgets
        };
        if (values.length > 1) {
            var diff = newItem.savedAt - values[values.length - 2].savedAt;
            // If more than three minutes elapsed, we push a new item, else
            // we replace the latest one
            if (diff > 3 * 60 * 1000) {
                values.push(newItem);
            } else {
                values[values.length - 1] = newItem;
            }
        } else {
            values.push(newItem);
        }
        // We don't store more than 50 elements total.
        if (values.length > 50) {
            values = values.slice(1);
        }
        var key = 'autosave-' + channelId + '-' + threadId + '-' + postId
        console.debug('autosave ', key, values);
        localStorage[key] = JSON.stringify(values);
        var inProgressKey = 'in-progress-' + channelId + '-' + threadId + '-' + postId;
        localStorage[inProgressKey] = 'true';
        console.debug('saved key ', key, inProgressKey);
    };

    saver.loadRecentAutoSaves = function(channelId, threadId, postId) {
        var saves = saver.loadAutoSaves(channelId, threadId, postId);
        console.debug('found save count was ', saves.length);        
        if (saves.length > 0) {
            return saves[saves.length -1];
        }
        return null;
    }

    saver.loadAutoSaves = function(channelId, threadId, postId) {
        var key = 'autosave-' + channelId + '-' + threadId + '-' + postId;
        var value = localStorage[key];
        console.debug('load ', key, value);
        if (!value) {
            return []
        } else {
            return JSON.parse(value);
        }
    }
    


})();
