
<style lang="scss">
 .forum-topic-row-vue {
     line-height: 1em;
     border-bottom: 1px solid #DDD;
     border-top: 1px solid #DDD;
     margin-top: -1px;
     a {
         padding-top: .5em;
         padding-bottom: .5em;
         display: block;
         
     }
     a:hover {
         text-decoration: none;
         background-color: #DDD;
     }
     .topic-title {
         width: 400px;
         display: inline-block;
     }
     .created-at {
         color: #999;
     }
 }
</style>

<template>
    <div class="forum-topic-row-vue">
        <a class="topic-link" :href="'#/forum/' +topic.channelId + '/' + topic.id">
            <span class="topic-title">{{ topic.title }}</span>
            <span class="created-at">{{ formatDate(topic.threadUpdatedAt) }}</span>
            <span class="created-at">by {{ topic.fromUsername }}</span>
        </a>
        <a v-if="topic.mentions" class="mentions">{{ topic.mentions }} mentions</a>
        <a v-if="topic.unreadCount" class="unread">{{ topic.unreadCount }} unread</a>
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
