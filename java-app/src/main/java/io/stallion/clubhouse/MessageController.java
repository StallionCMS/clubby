package io.stallion.clubhouse;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.Context;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.filtering.Pager;
import io.stallion.services.Log;
import io.stallion.users.Role;
import io.stallion.utils.DateUtils;
import org.apache.commons.collections4.Get;
import org.apache.commons.lang3.StringUtils;


public class MessageController extends StandardModelController<Message> {
    public static MessageController instance() {
        return (MessageController) DataAccessRegistry.instance().get("sch_messages");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(Message.class, MessageController.class, false);
    }

    @Override
    public void onPreCreatePrepare(Message obj) {
        if (empty(obj.getId())) {
            obj.setId(DB.instance().getTickets().nextId());
        }
        if (empty(obj.getCreatedAt())) {
            obj.setCreatedAt(DateUtils.utcNow());
        }
        if (empty(obj.getThreadUpdatedAt())) {
            obj.setThreadUpdatedAt(obj.getCreatedAt());
        }
        if (empty(obj.getThreadId())) {
            if (empty(obj.getParentMessageId())) {
                obj.setThreadId(obj.getId());
            } else {
                obj.setThreadId(obj.getParentMessageId());
            }
        }

    }

    public boolean currentUserCanDelete(Message message) {
        if (message.getFromUserId() == Context.getUser().getId()) {
            return true;
        }
        if (Context.getUser().isInRole(Role.ADMIN)) {
            return true;
        }
        return !ChannelMemberController.instance()
                .filter("channelId", message.getChannelId())
                .filter("userId", Context.getUser().getId())
                .filter("owner", true)
                .empty();
    }

    public boolean currentUserCanEdit(Message message) {
        if (message.getFromUserId() == Context.getUser().getId()) {
            return true;
        }
        return false;
    }

    public void markRead(Long userId, Long messageId) {
        DB.instance().execute("UPDATE sch_user_messages SET `read`=1 WHERE userId=? AND messageId=?", userId, messageId);
    }

    public MessageCombo getMessageCombo(Long userId, Long messageId) {
        String sql = "SELECT " +
                "    m.id as id, " +
                "    m.messageEncryptedJson," +
                "    m.messageEncryptedJsonVector, " +
                "    m.messageJson as messageJson," +
                "    m.edited," +
                "    m.fromUserId," +
                "    m.fromUsername," +
                "    m.createdAt," +
                "    um.id AS userMessageId, " +
                "    um.encryptedPasswordHex, " +
                "    um.passwordVectorHex, " +
                "    um.read  " +
                "  " +
                " FROM sch_messages as m" +
                " INNER JOIN sch_user_messages AS um ON um.messageId=m.id " +
                " WHERE um.userId=? AND m.deleted=0 AND um.deleted=0 AND m.id=? AND um.messageId=?" +
                " LIMIT 1";
        List<MessageCombo> ms = DB.instance().queryBean(MessageCombo.class, sql, userId, messageId, messageId);
        if (ms.size() == 0) {
            return null;
        } else {
            return ms.get(0);
        }
    }

    public List<MessageCombo> loadUnseenMessages(ZonedDateTime since) {
        String sql = "" +
                " SELECT " +
                "    m.id as id, " +
                "    m.messageEncryptedJson," +
                "    m.messageEncryptedJsonVector, " +
                "    m.messageJson as messageJson," +
                "    m.edited," +
                "    m.fromUserId," +
                "    m.fromUsername," +
                "    m.createdAt," +
                "    um.id AS userMessageId, " +
                "    um.userId AS toUserId, " +
                "    um.encryptedPasswordHex, " +
                "    um.passwordVectorHex, " +
                "    um.read  " +
                "    " +
                "  " +
                " FROM sch_messages as m" +
                " INNER JOIN sch_user_messages AS um ON um.messageId=m.id " +
                " INNER JOIN sch_user_states AS us ON us.userId=um.userId " +
                " WHERE " +
                "     m.deleted=0 AND " +
                "     um.deleted=0 AND " +
                "     um.mentioned=1 AND" +
                "     um.emailNotifySent=0 AND " +
                "     us.state!='AWAKE' AND " +
                "     um.read=0 AND" +
                "     m.createdAt<? " +
                "" +
                " ORDER BY m.createdAt ASC ";
        List<MessageCombo> combos = DB.instance()
                .queryBean(MessageCombo.class, sql, DateUtils.SQL_FORMAT.format(since));

        return combos;
    }

    public ThreadContext loadMessagesForForumThread(Long userId, Long channelId, Long parentMessageId, Integer page, boolean markRead) {
        page = or(page, 1);
        page = page - 1;
        int limit = 50;
        int offset = page * limit;

        Channel channel = ChannelController.instance().forId(channelId);

        Message parent = MessageController.instance().forId(parentMessageId);
        ForumTopic topic = new ForumTopic()
                .setId(parent.getId())
                .setChannelId(channelId)
                .setCreatedAt(parent.getCreatedAt())
                .setFromUserId(parent.getFromUserId())
                .setFromUsername(parent.getFromUsername())
                .setTitle(parent.getTitle())
                ;


        String sql = "SELECT " +
                "    m.id as id," +
                "    m.channelId, " +
                "    m.messageEncryptedJson," +
                "    m.messageEncryptedJsonVector, " +
                "    m.messageJson as messageJson," +
                "    m.edited," +
                "    m.fromUserId," +
                "    m.fromUsername," +
                "    m.createdAt," +
                "    um.id AS userMessageId, " +
                "    um.encryptedPasswordHex, " +
                "    um.passwordVectorHex, " +
                "    um.read  " +
                "    " +
                "  " +
                " FROM sch_messages as m ";
        if (!channel.isEncrypted() && channel.isNewUsersSeeOldMessages()) {
            sql += " LEFT OUTER JOIN sch_user_messages AS um ON um.messageId=m.id AND um.userId=? ";
        } else {
            sql += " INNER JOIN sch_user_messages AS um ON um.messageId=m.id AND um.userId=? ";
        }
        sql +=  " WHERE " +
                "       m.channelId=? " +
                "   AND threadId=? " +
                "   AND (um.userId=? OR um.userId IS NULL) " +
                "   AND m.deleted=0 AND (um.deleted=0 OR um.deleted IS NULL)" +
                " ORDER BY m.createdAt DESC " +
                " LIMIT " + offset + ", " + limit;

        List<MessageCombo> messages = DB.instance().queryBean(MessageCombo.class, sql, userId, channelId, parentMessageId, userId);


        ThreadContext ctx = new ThreadContext()
                .setMessages(messages)
                .setTopic(topic)
                .setPage(page)
                ;
        return ctx;
    }

    public static class ThreadContext {
        private ForumTopic topic;
        private List<MessageCombo> messages;
        private int page;
        private int pageCount;

        public ForumTopic getTopic() {
            return topic;
        }

        public ThreadContext setTopic(ForumTopic topic) {
            this.topic = topic;
            return this;
        }

        public List<MessageCombo> getMessages() {
            return messages;
        }

        public ThreadContext setMessages(List<MessageCombo> messages) {
            this.messages = messages;
            return this;
        }

        public int getPage() {
            return page;
        }

        public ThreadContext setPage(int page) {
            this.page = page;
            return this;
        }

        public int getPageCount() {
            return pageCount;
        }

        public ThreadContext setPageCount(int pageCount) {
            this.pageCount = pageCount;
            return this;
        }
    }



    public ForumTopicContext loadMessagesForForumTopLevel(Long userId, Long channelId, Integer page) {
        // load recent messages
        page = or(page, 1);
        page = page - 1;
        int limit = 50;
        int offset = page * limit;

        Channel channel = ChannelController.instance().forId(channelId);

        List<ForumTopic> topics1 = list();



        String sql = "SELECT " +
                "    m.id as id," +
                "    m.channelId, " +
                "    m.title," +
                "    m.fromUserId," +
                "    m.fromUsername," +
                "    m.createdAt, " +
                "    m.threadUpdatedAt, " +
                "    m.pinned, " +
                "    um.id AS userMessageId, " +
                "    um.encryptedPasswordHex, " +
                "    um.passwordVectorHex, " +
                "    um.watched, " +
                "    um.read  " +
                "    " +
                "  " +
                " FROM sch_messages as m ";
        if (!channel.isEncrypted() && channel.isNewUsersSeeOldMessages()) {
            sql += " LEFT OUTER JOIN sch_user_messages AS um ON um.messageId=m.id AND um.userId=? ";
        } else {
            sql += " INNER JOIN sch_user_messages AS um ON um.messageId=m.id AND um.userId=? ";
        }

        // Only get watched and pinned if we are looking at the first page
        if (offset == 0) {
            // Get pinned AND watched AND new topics in the last 10 days
            String sqlFirst = sql + " " +
                    " WHERE " +
                    "       m.channelId=? " +
                    "  AND parentMessageId = 0 " +
                    "  AND (um.userId=? OR um.userId IS NULL) " +
                    "  AND  m.deleted=0 " +
                    "  AND (um.deleted=0 OR um.deleted IS NULL) " +
                    "  AND (m.pinned=1 OR um.watched=1 OR m.createdAt>=?) " +
                    " ORDER BY m.pinned DESC, um.watched DESC ";
            topics1 = DB.instance().queryBean(ForumTopic.class, sqlFirst, userId, channelId, userId,
                    DateUtils.SQL_FORMAT.format(DateUtils.utcNow().minusDays(10)));
        }

        String sqlSecond = sql + " " +
                " WHERE m.channelId=? AND (um.userId=? OR um.userId IS NULL) " +
                "  AND m.deleted=0 AND (um.deleted=0 OR um.deleted IS NULL) " +
                "  AND m.pinned=0 AND (um.watched=0 OR um.watched IS NULL) AND m.createdAt<? " +
                "  AND parentMessageId = 0 " +
                " ORDER BY m.threadUpdatedAt DESC " +
                " LIMIT " + offset + "," + limit;
        // Then get all other topics ordered by threadUpdatedAt time
        List<ForumTopic> topics2 = DB.instance().queryBean(ForumTopic.class, sqlSecond, userId, channelId, userId,
                   DateUtils.SQL_FORMAT.format(DateUtils.utcNow().minusDays(10)));
        List allTopicIds = list();
        Map<Long, ForumTopic> topicMap = map();

        for(List<ForumTopic> topics: list(topics1, topics2)) {
            for(ForumTopic topic: topics) {
                allTopicIds.add(topic.getId());
                topicMap.put(topic.getId(), topic);
            }
        }

        // Get unread a
        if (allTopicIds.size() > 0) {
            DB.SqlAndParams inSql = DB.instance().toInQueryParams(allTopicIds);
            String umSql = " " +
                    " SELECT parentMessageId as parentId, COUNT(*) AS unreadCount, COUNT(mentioned) as mentions FROM sch_user_messages AS um " +
                    "  INNER JOIN sch_messages AS m ON m.id=um.messageID " +
                    " WHERE " +
                    "    `parentMessageId` IN  " + inSql.getSql() + " " +
                    "    AND `read`=0 " +
                    "    AND userId=?" +
                    " GROUP BY parentMessageId ";
            inSql.getParamsList().add(userId);
            List<ForumTopicCounts> counts = DB.instance().queryBean(
                    ForumTopicCounts.class,
                    umSql,
                    inSql.getParams()
            );

            for (ForumTopicCounts count : counts) {
                ForumTopic topic = topicMap.get(count.getParentId());
                topic.setMentions(or(count.getMentions(), 0L));
                topic.setUnreadCount(or(count.getUnreadCount(), 0L));
            }
        }

        ForumTopicContext ctx = new ForumTopicContext();
        for(ForumTopic topic: topics1) {
            if (topic.isPinned()) {
                ctx.getPinnedTopics().add(topic);
            } else if (topic.getWatched() != null && topic.getWatched() == true) {
                ctx.getWatchedTopics().add(topic);
            } else {
                ctx.getNewTopics().add(topic);
            }
        }
        ctx.setUpdatedTopics(topics2);



        return ctx;
    }

    public static class ForumTopicContext {
        private List<ForumTopic> pinnedTopics = list();
        private List<ForumTopic> watchedTopics = list();
        private List<ForumTopic> newTopics = list();
        private List<ForumTopic> updatedTopics = list();

        public List<ForumTopic> getPinnedTopics() {
            return pinnedTopics;
        }

        public ForumTopicContext setPinnedTopics(List<ForumTopic> pinnedTopics) {
            this.pinnedTopics = pinnedTopics;
            return this;
        }

        public List<ForumTopic> getWatchedTopics() {
            return watchedTopics;
        }

        public ForumTopicContext setWatchedTopics(List<ForumTopic> watchedTopics) {
            this.watchedTopics = watchedTopics;
            return this;
        }

        public List<ForumTopic> getNewTopics() {
            return newTopics;
        }

        public ForumTopicContext setNewTopics(List<ForumTopic> newTopics) {
            this.newTopics = newTopics;
            return this;
        }

        public List<ForumTopic> getUpdatedTopics() {
            return updatedTopics;
        }

        public ForumTopicContext setUpdatedTopics(List<ForumTopic> updatedTopics) {
            this.updatedTopics = updatedTopics;
            return this;
        }
    }

    public List<MessageCombo> loadMessagesForChannel(Long userId, Long channelId, Integer page, boolean markRead) {
        // load recent messages
        page = or(page, 1);
        page = page - 1;
        int limit = 50;
        int offset = page * limit;

        Channel channel = ChannelController.instance().forId(channelId);

        String sql = "SELECT " +
                "    m.id as id," +
                "    m.channelId, " +
                "    m.messageEncryptedJson," +
                "    m.messageEncryptedJsonVector, " +
                "    m.messageJson as messageJson," +
                "    m.edited," +
                "    m.fromUserId," +
                "    m.fromUsername," +
                "    m.createdAt," +
                "    um.id AS userMessageId, " +
                "    um.encryptedPasswordHex, " +
                "    um.passwordVectorHex, " +
                "    um.read  " +
                "    " +
                "  " +
                " FROM sch_messages as m ";
        if (!channel.isEncrypted() && channel.isNewUsersSeeOldMessages()) {
            sql += " LEFT OUTER JOIN sch_user_messages AS um ON um.messageId=m.id AND um.userId=? ";
        } else {
            sql += " INNER JOIN sch_user_messages AS um ON um.messageId=m.id AND um.userId=? ";
        }
        sql +=  " WHERE m.channelId=? AND (um.userId=? OR um.userId IS NULL) AND m.deleted=0 AND (um.deleted=0 OR um.deleted IS NULL)" +
                " ORDER BY m.createdAt DESC " +
                " LIMIT " + offset + ", " + limit;

        List<MessageCombo> messages = DB.instance().queryBean(MessageCombo.class, sql, userId, channelId, userId);
        Collections.reverse(messages);

        Map<Long, MessageCombo> messageMap = map();

        if (messages.size() > 0) {
            StringBuffer buf = new StringBuffer();
            buf.append("(");
            List<Long> messageIds = list();
            int x = 0;
            for (MessageCombo message : messages) {
                messageIds.add(message.getId());
                buf.append("?");
                if (x < (messages.size() - 1)) {
                    buf.append(",");
                }
                messageMap.put(message.getId(), message);
                x++;
            }
            buf.append(")");

            // join on reactions
            List<MessageReaction> reactions = DB.instance().queryBean(
                    MessageReaction.class,
                    "" +
                            " SELECT messageId, emoji, displayName FROM sch_message_reactions " +
                            " WHERE messageId IN " + buf.toString(),
                    asArray(messageIds, Long.class)
            );
            for(MessageReaction reaction: reactions) {
                MessageCombo msg = messageMap.get(reaction.getMessageId());
                if (!msg.getReactions().containsKey(reaction.getEmoji())) {
                    msg.getReactions().put(reaction.getEmoji(), list());
                }
                msg.getReactions().get(reaction.getEmoji()).add(reaction.getDisplayName());
            }
        }

        if (markRead) {
            List<Long> params = list();
            StringBuffer whereSql = new StringBuffer(" id IN (");
            for (MessageCombo mc : messages) {
                if (empty(mc.getUserMessageId())) {
                    continue;
                }
                if (mc.isRead() == true) {
                    continue;
                }
                params.add(mc.getUserMessageId());
                whereSql.append("?,");
            }
            if (params.size() > 0) {
                params.add(userId);
                String sql2 = "UPDATE sch_user_messages SET `read`=1 WHERE " + StringUtils.stripEnd(whereSql.toString(), ",") + ") " +
                        " AND userId=?";
                DB.instance().execute(sql2, asArray(params, Long.class));
            }
        }


        return messages;
    }
}
