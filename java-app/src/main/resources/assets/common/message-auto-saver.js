(function() {

    window.ClubhouseMessageAutoSaver = {};
    var saver = window.ClubhouseMessageAutoSaver;

    saver.clearInProgress = function(channelId, threadId) {
        delete localStorage['in-progress-' + channelId + '-' + threadId];
    };

    saver.isInProgress = function(channelId, threadId) {
        return localStorage['in-progress-' + channelId + '-' + threadId];
    }

    saver.autoSave = function(channelId, threadId, text, widgets) {
        threadId = threadId || 0;
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
        var key = 'autosave-' + channelId + '-' + threadId
        console.log('autosave ', key, values);
        localStorage[key] = JSON.stringify(values);
        localStorage['in-progress-' + channelId + '-' + threadId] = 'true';
        console.log('saved key ', key);
    };

    saver.loadRecentAutoSaves = function(channelId, threadId) {
        var saves = saver.loadAutoSaves(channelId, threadId);
        if (saves.length > 0) {
            return saves[saves.length -1];
        }
        return null;
    }

    saver.loadAutoSaves = function(channelId, threadId) {
        var key = 'autosave-' + channelId + '-' + threadId;
        var value = localStorage[key];
        console.log('load ', key, value);
        if (!value) {
            return []
        } else {
            return JSON.parse(value);
        }
    }
    


})();
