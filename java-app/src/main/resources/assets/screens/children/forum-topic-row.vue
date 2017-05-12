
<style lang="scss">
 .forum-topic-row-vue {
     line-height: 1em;
     border-bottom: 1px solid #DDD;
     border-top: 1px solid #DDD;
     margin-top: -1px;
     a {
         padding-top: .5em;
         padding-bottom: .5em;
         display: inline-block;
         
     }
     .mentions-link.icon-link {
         display: inline-block;
         color: #07457b;
         margin-right: 6px;
         .material-icons {
             vertical-align: -40%;
         }
     }
     .title-topic-link {
         width: 310px;
     }
     a:hover {
         text-decoration: none;
         background-color: #DDD;
     }
     .title-and-mentions {
         width: 400px;
         display: inline-block;
     }
     .created-at {
         color: #999;
         width: 100px;
         display: inline-block;
     }
     .topic-username {
         width: 200px;
         display: inline-block;
     }
 }
</style>

<template>
    <div class="forum-topic-row-vue">
        <span class="title-and-mentions">
            <a v-if="topic.mentions || topic.unreadCount" class="mentions-link icon-link" :href="'#/forum/' +topic.channelId + '/' + topic.id + '?messageId=' + topic.firstMentionId">
                <span v-if="topic.mentions" class="label-count">{{ topic.mentions }} <i class="material-icons">announcement</i></span>
                <span v-if="topic.unreadCount" class="label-count">{{ topic.unreadCount }} <i class="material-icons">fiber_new</i></span>
            </a>
            <a class="title-topic-link" :href="'#/forum/' +topic.channelId + '/' + topic.id">
                <span class="topic-title">{{ topic.title }}</span>
            </a>
        </span>
        <span>
            <span class="created-at">{{ formatDate(topic.threadUpdatedAt) }}</span>
            <span class="topic-username">by {{ topic.fromUsername }}</span>
            <span>{{ topic.totalCount + 1 }} {{ (topic.totalCount + 1) > 1 ? 'posts' : 'post' }}</span>
        </span>

        <!-- <a v-if="topic.unreadCount" class="unread icon-link"  :href="'#/forum/' +topic.channelId + '/' + topic.id + '?goTo=first-unread'"></a>-->
    </div>
</template>

<script>
 module.exports = {
     props: {
         channel: null,
         topic: null
     },
     data: function() {
         return {
         };
     },
     methods: {
         formatDate: function(createdAt) {
             return moment.tz(createdAt * 1000, moment.tz.guess()).fromNow();//;.format('MMM d, YYYY h:mm a');
         }
     }
 };
</script>
