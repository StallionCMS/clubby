
<style lang="scss">
 .forum-topic-row-vue {
     line-height: 1em;
     border-bottom: 1px solid #DDD;
     border-top: 1px solid #DDD;
     margin-top: -1px;
     display: flex;
     color: #333;
     text-decoration: none;
     
     a {
         padding-top: .5em;
         padding-bottom: .5em;
         display: inline-block;
         
     }
     .mentions-link {
         padding: 0px;
         .material-icons {
             font-size: 20px;
         }
     }
     .mentions-link.icon-link {
         display: inline-block;
         color: #07457b;
         margin-right: 6px;
         margin-top: -10px;
         .label-count {
             display: inline-block;
             width: 50px;
             text-align: right;
         }
         .material-icons.fiber-icon {
             vertical-align: -27%;
         }
         .material-icons.announcement-icon {
             vertical-align: -35%;
         }
     }
     .title-and-mentions {
         display: block;
         line-height: 1.2em;
     }
     .vertically-centered {
         position: relative;
         top: 50%;
         transform: translateY(-50%);         
     }
     .topic-username {
         display: block;
         height: 18px;
     }
     
     .title-topic-link {
     }
     a:hover {
         text-decoration: none;
         background-color: #DDD;
     }
     .created-at {
         color: #999;
         display: block;
         
     }
     .topic-username {
         width: 200px;
         display: inline-block;
         font-size: 11px;
     }
     .topic-counts {
         margin-bottom: 11px;
         margin-top: 3px;
     }
     .forum-avatar-icon-wrapper {
         float: left;
         display: inline-block;
         padding: 6px 6px 6px 0px;
         width: 80px;
         text-align: left;
         .forum-avatar-icon {
             width: 25px;
             height: 25px;
             margin-right: 4px;
             border-radius: 5px;
         }
     }
     .badge-wrapper {
         padding-top: 3px;
     }
     .row-middle {
         padding-left: 8px;
         width: calc(100% - 110px);
     }
     .row-right {
         width: 70px;
         text-align: right;
         font-size: 18px;
         padding-top: 6px;
         padding-right: 6px;
     }
     .push-pin-icon {
         height: 20px;
         width: 20px;
         border: 2px solid white;
         opacity: .3;
     }
     .topic-row-edge-bar {
         width: 24px;
         background-color: #fff;
         margin-right: 10px;
         .material-icons {
             font-size: 16px;
             color: #AAA;
         }
     }
     .label-mentions-count {
         background-color: #a22727;
         display: inline-block;
         width: 20px;
         height: 20px;
         border-radius: 10px;
         border: 1px solid white;
         font-size: 10px;
         text-align: center;
         padding-top: 1px;
         color: white;
     }
     .new-unread-label .material-icons {
         color: #943;
     }
     .maybe-last-link {
         width: 24px;
         padding: 4px;
         display: block;
         background-color: transparent;
     }
     .maybe-last-link:hover {
         background-color: transparent;
     }
     .maybe-last-link.yes-last-link {
     }
     .maybe-last-link.yes-last-link:hover {
         background-color: F9F9F9;
     }
     
 }
 .forum-topic-row-vue:hover, .forum-topic-row-vue:active, .row-middle:active, .row-right:active, .row-right:hover {
     color: #333;
     background-color: none;
     text-decoration: none;
 }
</style>

<template>
    <a class="forum-topic-row-vue" :href="'#/forum/' +topic.channelId + '/' + topic.id">
        <div class="topic-row-edge-bar ">
            <a :href="'#/forum/' + topic.channelId + '/' + topic.id + ((topic.mentions || topic.unreadCount) ? '?messageId=' + topic.firstMentionId : '') " :class="['maybe-last-link', (topic.mentions || topic.unreadCount) ? 'yes-last-link': '', 'vertically-centered']">
                <span v-if="pinned" class="push-pin-icon"></span>
                <i v-if="watched" class="material-icons watched-icon">remove_red_eye</i>
                <i v-if="newish" class="material-icons new-icon">new_releases</i>
                <span v-if="topic.mentions" class="label-count label-mentions-count">{{ topic.mentions }}</span>
                <span v-if="topic.unreadCount && !topic.mentions" class="label-count new-unread-label"><i class="material-icons fiber-icon">fiber_new</i></span>
            </div>
        </div>
        <div class="row-middle">
            <span class="title-and-mentions vertically-centered">
                {{ topic.title }}
            </span>
        </div>
        <div class="forum-avatar-icon-wrapper">
            <div class="vertically-centered">
                <img class="forum-avatar-icon" :src="topic.avatarUrl" :title="'Original poster was ' + topic.fromUsername">
                <img v-if="topic.latestUsername && topic.latestUsername != topic.fromUsername" class="forum-avatar-icon" :src="topic.latestAvatarUrl" :title="'Last poster was ' + topic.latestUsername">
            </div>
        </div>
        <a :href="'#/forum/' + topic.channelId + '/' + topic.id + ((topic.mentions || topic.unreadCount) ? '?messageId=' + topic.firstMentionId : '?messageId=' + topic.latestId)" class="row-right">
            <div :class="[(topic.mentions || topic.unreadCount) ? 'topic-counts-has-new': '', 'topic-counts']">
                {{ topic.totalCount + 1 }}
            </div>
            <div class="badge-wrapper">
                
            </div>
            <div class="created-at">{{ formatDate(topic.threadUpdatedAt) }}</div>
        </a>
    </a>
    <!-- <a v-if="topic.unreadCount" class="unread icon-link"  :href="'#/forum/' +topic.channelId + '/' + topic.id + '?goTo=first-unread'"></a>-->
</template>

<script>
 module.exports = {
     props: {
         channel: null,
         topic: null,
         pinned: false,
         newish: false,
         watched: false
     },
     data: function() {
         return {
             thirtyAgo: moment().subtract(26, 'days'),
             yearAgo: moment().subtract(300, 'days')
         };
     },
     methods: {
         formatDate: function(createdAt) {
             var self = this;
             var m = moment.tz(createdAt * 1000, moment.tz.guess());
             if (self.yearAgo.isAfter(m)) {
                 return m.format('MMM [\']YY');
             } else if (self.thirtyAgo.isAfter(m)) {
                 return m.format('MMM D');
             } else {
                 return m.fromNow(true);//;.format('MMM d, YYYY h:mm a');
             }
         }
     }
 };
</script>
