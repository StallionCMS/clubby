package io.clubby.server;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.clubby.server.webSockets.WebSocketEventHandler;
import io.stallion.Context;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.filtering.FilterOperator;
import io.stallion.dataAccess.filtering.SortDirection;
import io.stallion.settings.Settings;
import io.stallion.users.IUser;
import io.stallion.users.Role;
import io.stallion.users.UserController;
import io.stallion.utils.DateUtils;
import io.stallion.utils.json.JSON;
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
        if (empty(obj.getUpdatedAt())) {
            obj.setUpdatedAt(DateUtils.utcNow());
        }
        if (empty(obj.getThreadUpdatedAt())) {
            obj.setThreadUpdatedAt(obj.getCreatedAt());
        }
        if (empty(obj.getThreadId())) {
            if (!empty(obj.getParentMessageId())) {
                obj.setThreadId(obj.getParentMessageId());
            }
        }

    }

    @Override
    public void onPreSavePrepare(Message obj) {
        obj.setUpdatedAt(DateUtils.utcNow());
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
        Channel channel = ChannelController.instance().forId(message.getChannelId());
        if (channel.isWikiStyle() && message.getId().equals(message.getThreadId())) {
            return true;
        }
        return false;
    }

    public long countMessagesUpdatedSinceTime(Long userId, Long channelId, Long since) {
        ChannelMember cm = ChannelMemberController.instance().forUserChannel(userId, channelId);
        if (cm == null) {
            return 0;
        }
        String sinceFormatted = DateUtils.SQL_FORMAT.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(since), UTC));
        Long c = DB.instance().queryScalar("" +
                " SELECT COUNT(*) FROM sch_user_messages AS um" +
                "   INNER JOIN sch_messages as m ON um.messageId=m.id " +
                " WHERE" +
                "    m.row_updated_at>=? OR " +
                "    um.row_updated_at>=? AND " +
                "    um.userId=? AND " +
                "    m.channelId=? ",
                sinceFormatted, sinceFormatted, userId, channelId
        );
        return c;
    }


    public void notifyOfNewMessage(Message message, UserMessage um, Channel channel) {
        MessageCombo combo = messageUserMessageToCombo(message, um);

        WebSocketEventHandler.sendMessageToUser(um.getUserId(), JSON.stringify(map(val("message", combo), val("type", "new-message"))));

        if (combo.isMentioned() || channel.getChannelType().equals(ChannelType.DIRECT_MESSAGE)) {
            if (combo.getFromUserId() != combo.getToUserId()) {
                checkSendMobileNotificationForNewMessageMaybe(combo, channel);
            }
        }

    }

    public void checkSendMobileNotificationForNewMessageMaybe(MessageCombo combo, Channel channel) {
        boolean pending = false;
        boolean shouldSend = false;

        // If notifications are off, return without doing anything
        UserProfile up = UserProfileController.instance().forStallionUser(combo.getToUserId());
        if (up != null && UserNotifyPreference.NONE.equals(up.getMobileNotifyPreference())) {
            return;
        }

        // If user is awake, no mobile notifications. If user is IDLE, and they have a mobile session, we send
        // them a notification if they don't read the new message within 1 to 2 minutes.
        // If user is disconnected entirely, send a notification immediately.
        UserState state = UserStateController.instance().filter("userId", combo.getToUserId()).first();
        if (state != null && state.getState().equals(UserStateType.AWAKE)) {
            return;
        } else if (state != null && state.getState().equals(UserStateType.IDLE)) {
            int c = MobileSessionController.instance().filter("userId", combo.getToUserId()).count();
            if (c > 0) {
                pending = true;
            }
            if (Settings.instance().getSiteUrl().startsWith("https://macbook.clubby.io")) {
                pending = false;
                shouldSend = true;
            }
        } else {
            shouldSend = true;
        }

        if (shouldSend) {
            sendMobileNotificationOfMessage(combo, channel);
        } else if (pending) {
            DB.instance().execute(
                    "UPDATE sch_user_messages SET mobileNotifyPending=1 WHERE id = ? ",
                    combo.getUserMessageId()
            );
        }



    }

    public void sendMobileNotificationOfMessage(MessageCombo combo) {
        Channel channel = ChannelController.instance().forId(combo.getChannelId());
        sendMobileNotificationOfMessage(combo, channel);
    }

    public void sendMobileNotificationOfMessage(MessageCombo combo, Channel channel) {

        Map messageData = map();
        messageData.put("siteUrl", Settings.instance().getSiteUrl());
        messageData.put("channelId", combo.getChannelId());
        messageData.put("channelType", channel.getChannelType().toString());
        messageData.put("messageId", combo.getId());
        messageData.put("threadId", combo.getThreadId());
        messageData.put("userId", combo.getToUserId());
        String path = "/#/channel/" + combo.getChannelId() + "&messageId=" + combo.getId();
        if (channel.getChannelType().equals(ChannelType.FORUM)) {
            path = "/#/forum/" + combo.getChannelId() + "/" + combo.getThreadId() + "?messageId=" + combo.getId();
        }
        messageData.put("path", path);
        messageData.put("fromUsername", combo.getFromUsername());
        messageData.put("siteName", ClubbyDynamicSettings.getSiteName());


        Notifier.sendNotification(combo.getToUserId(), "New message from " + combo.getFromUsername(), "", messageData);

        DB.instance().execute(
                "UPDATE sch_user_messages SET mobileNotifyPending=0 WHERE id = ? ",
                combo.getUserMessageId()
        );
    }


    public MessageCombo messageUserMessageToCombo(Message message, UserMessage um) {
        Channel channel = ChannelController.instance().forId(message.getChannelId());
        MessageCombo combo = new MessageCombo()
                .setId(message.getId())
                .setChannelType(channel.getChannelType())
                .setMessageJson(message.getMessageJson())
                .setMessageEncryptedJson(message.getMessageEncryptedJson())
                .setMessageEncryptedJsonVector(message.getMessageEncryptedJsonVector())
                .setCreatedAt(message.getCreatedAt())
                .setUpdatedAt(message.getUpdatedAt())
                .setEdited(message.isEdited())
                .setParentMessageId(message.getParentMessageId())
                .setThreadId(message.getThreadId())
                .setTitle(message.getTitle())
                .setEncryptedPasswordHex(um.getEncryptedPasswordHex())
                .setPasswordVectorHex(um.getPasswordVectorHex())
                .setFromUserId(message.getFromUserId())
                .setFromUsername(message.getFromUsername())
                .setToUserId(um.getUserId())
                .setRead(um.isRead())
                .setUserMessageId(um.getId())
                .setChannelId(message.getChannelId())
                ;

        if (um.isHereMentioned() || um.isMentioned()) {
            combo.setMentioned(true);
        }
        return combo;
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
                "    m.parentMessageId, " +
                "    m.edited," +
                "    m.fromUserId," +
                "    m.fromUsername," +
                "    m.createdAt," +
                "    m.channelId, " +
                "    m.threadId, " +
                "    um.id AS userMessageId, " +
                "    um.encryptedPasswordHex, " +
                "    um.passwordVectorHex, " +
                "    um.watched, " +
                "    um.read" +
                "  " +
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

    public List<MessageCombo> loadUnseenMessagesForEmailNotify(ZonedDateTime before) {
        ZonedDateTime after = before.minusDays(3);
        String sql = "" +
                " SELECT " +
                "    m.id as id, " +
                "    m.channelId AS channelId, " +
                "    m.messageEncryptedJson," +
                "    m.messageEncryptedJsonVector, " +
                "    m.messageJson as messageJson," +
                "    m.parentMessageId,  " +
                "    m.edited," +
                "    m.fromUserId," +
                "    m.fromUsername," +
                "    m.createdAt," +
                "    m.title, " +
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
                "     m.createdAt<? AND " +
                "     m.createdAt>? AND " +
                "     m.fromUserId!=um.userId " +
                "" +
                " ORDER BY m.createdAt ASC ";
        List<MessageCombo> combos = DB.instance()
                .queryBean(MessageCombo.class, sql, DateUtils.SQL_FORMAT.format(before), DateUtils.SQL_FORMAT.format(after));

        return combos;
    }

    public List<MessageCombo> loadUnseenMessagesForMobileNotify(ZonedDateTime before) {
        ZonedDateTime after = before.minusDays(3);
        String sql = "" +
                " SELECT " +
                "    m.id as id, " +
                "    m.messageEncryptedJson," +
                "    m.messageEncryptedJsonVector, " +
                "    m.messageJson as messageJson, " +
                "    m.channelId as channelId, " +
                "    m.parentMessageId,  " +
                "    m.edited," +
                "    m.fromUserId," +
                "    m.fromUsername," +
                "    m.createdAt," +
                "    m.title, " +
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
                "     um.mobileNotifyPending=1 AND " +
                "     us.state!='AWAKE' AND " +
                "     um.read=0 AND" +
                "     m.createdAt<? AND " +
                "     m.createdAt>? " +
                "" +
                " ORDER BY m.createdAt ASC ";
        List<MessageCombo> combos = DB.instance()
                .queryBean(MessageCombo.class, sql, DateUtils.SQL_FORMAT.format(before), DateUtils.SQL_FORMAT.format(after));

        return combos;
    }

    public ThreadContext loadMessagesForForumThread(Long userId, Long channelId, Long threadId, Integer page, boolean markRead) {
        page = or(page, 1);
        page = page - 1;
        int limit = 50;
        int offset = page * limit;
        ThreadContext ctx = new ThreadContext();

        Channel channel = ChannelController.instance().forId(channelId);

        Message parent = MessageController.instance().forId(threadId);


        ForumTopic topic = new ForumTopic()
                .setId(parent.getId())
                .setChannelId(channelId)
                .setCreatedAt(parent.getCreatedAt())
                .setFromUserId(parent.getFromUserId())
                .setFromUsername(parent.getFromUsername())
                .setTitle(parent.getTitle())
                ;

        if (channel.isWikiStyle()) {
            MessageVersion mv = MessageVersionController.instance()
                    .filter("messageId", threadId).sortBy("versionDate", SortDirection.DESC)
                    .first();
            if (mv != null) {
                IUser user = UserController.instance().forIdWithDeleted(mv.getVersionUserId());
                if (user != null) {
                    topic.setLastEditedTopicUsername(user.getUsername());
                    topic.setLastEditedTopicUserId(user.getId());
                }
            }
        }

        UserMessage userMessage = UserMessageController.instance()
                .filter("messageId", threadId)
                .filter("userId", userId)
                .first();
        if (userMessage != null && userMessage.isWatched()) {
            topic.setWatched(true);
        }

        String sql = "SELECT " +
                "    m.id as id," +
                "    m.channelId, " +
                "    m.messageEncryptedJson," +
                "    m.messageEncryptedJsonVector, " +
                "    m.messageJson as messageJson, " +
                "    m.parentMessageId, " +
                "    m.edited," +
                "    m.fromUserId," +
                "    m.fromUsername," +
                "    m.createdAt, " +
                "    m.threadId, " +
                "    m.title, " +
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
                " ORDER BY m.createdAt ASC " +
                " LIMIT " + offset + ", " + limit;

        List<MessageCombo> messages = DB.instance().queryBean(
                MessageCombo.class, sql, userId, channelId, threadId, userId);

        // Find reactions
        if (messages.size() > 0) {
            Map<Long, MessageCombo> messageMap = map();
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

        Map countsRecord = DB.instance().findRecord(
                "SELECT COUNT(`read`) AS unreadCount, COUNT(IF(mentioned = '0', NULL, 1)) as mentionedCount" +
                        " FROM sch_user_messages AS um" +
                        "   INNER JOIN sch_messages AS m ON m.id=um.messageId " +
                        "   LEFT OUTER JOIN sch_messages AS tm ON m.threadId=tm.id  " +
                        " WHERE " +
                        "  um.channelId=? AND" +
                        "  userId=? AND" +
                        "  `read`=0 AND" +
                        "  um.deleted=0 AND" +
                        "  m.deleted=0 AND" +
                        "  m.fromUserId!=? AND " +
                        "  (m.threadId IS NULL OR m.threadId=0 OR tm.deleted=0)   " +
                        "" +
                        "",
                channelId, userId, userId
        );
        ctx.setUnreadCount(((Long)countsRecord.get("unreadCount")).intValue());
        ctx.setUnreadMentionsCount(((Long)countsRecord.get("mentionedCount")).intValue());





        // Load all ID's and dates for righthand slider
        String allMessagesSql = " SELECT " +
                "    m.id as id," +
                "    m.createdAt " +
                " FROM sch_messages as m \n";
        if (!channel.isEncrypted() && channel.isNewUsersSeeOldMessages()) {
            allMessagesSql += " LEFT OUTER JOIN sch_user_messages AS um ON um.messageId=m.id AND um.userId=? ";
        } else {
            allMessagesSql += " INNER JOIN sch_user_messages AS um ON um.messageId=m.id AND um.userId=? ";
        }
        allMessagesSql += " WHERE " +
                "       m.channelid=? " +
                "   AND threadId=? " +
                "   AND (um.userId=? OR um.userId IS NULL) " +
                "   AND m.deleted=0 AND (um.deleted=0 OR um.deleted IS NULL) " +
                "   ORDER BY createdAt ASC ";

        List<Map<String, Object>> records = DB.instance().findRecords(
                allMessagesSql,
                userId, channelId, threadId, userId
        );

        int pageCount = records.size() / limit;
        if (records.size() % limit > 0) {
            pageCount++;
        }
        ctx
                .setMessages(messages)
                .setTopic(topic)
                .setPage(page)
                .setPageCount(pageCount)
                ;
        for (Map<String, Object> record: records) {
            ctx.getAllIdsDates().add(new Long[]{
                    ((BigInteger)record.get("id")).longValue(),
                    ((Timestamp)record.get("createdAt")).getTime()
            });
        }
        return ctx;
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
                "    um.read," +
                "    up.avatarUrl  " +
                "    " +
                "  " +
                " FROM sch_messages as m ";
        if (!channel.isEncrypted() && channel.isNewUsersSeeOldMessages()) {
            sql += " LEFT OUTER JOIN sch_user_messages AS um ON um.messageId=m.id AND um.userId=? ";
        } else {
            sql += " INNER JOIN sch_user_messages AS um ON um.messageId=m.id AND um.userId=? ";
        }
        sql += " LEFT OUTER JOIN sch_user_profiles AS up ON up.userId=m.fromUserId ";


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
                    " ORDER BY m.pinned DESC, um.watched DESC, m.createdAt DESC ";
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


        // Get unread and mentioned counts
        if (allTopicIds.size() > 0) {
            DB.SqlAndParams inSql = DB.instance().toInQueryParams(allTopicIds);
            List idParams = new ArrayList<>(inSql.getParamsList());
            inSql.getParamsList().add(userId);
            inSql.getParamsList().add(userId);

            {
                String umSql = " " +
                        " SELECT threadId as parentId, MIN(m.id) as minId, COUNT(`read`) AS unreadCount, COUNT(IF(mentioned = '0', NULL, 1)) as mentions FROM sch_user_messages AS um " +
                        "  INNER JOIN sch_messages AS m ON m.id=um.messageID " +
                        " WHERE " +
                        "    `threadId` IN  " + inSql.getSql() + " " +
                        "    AND `read`=0 " +
                        "    AND userId=? " +
                        "    AND m.deleted=0 " +
                        "    AND m.fromUserId!=? " +
                        "    AND um.deleted=0 " +
                        " GROUP BY threadId ";

                List<ForumTopicCounts> counts = DB.instance().queryBean(
                        ForumTopicCounts.class,
                        umSql,
                        inSql.getParams()
                );

                for (ForumTopicCounts count : counts) {
                    ForumTopic topic = topicMap.get(count.getParentId());
                    topic.setMentions(or(count.getMentions(), 0L));
                    topic.setUnreadCount(or(count.getUnreadCount(), 0L));
                    topic.setFirstMentionId(count.getMinId());
                }

            }
            {
                String umSql2 = " " +
                        " SELECT parentMessageId as parentId, count(*) as totalCount, max(id) as latestId FROM sch_messages AS m ";
                if (!channel.isNewUsersSeeOldMessages()) {
                    umSql2 += "  INNER JOIN sch_user_messages AS um ON m.id=um.messageID ";
                }
                umSql2 +=
                        " WHERE " +
                        "    `parentMessageId` IN  " + inSql.getSql() + " ";
                if (!channel.isNewUsersSeeOldMessages()) {
                    umSql2 += "    AND userId=?";
                }
                umSql2 +=
                        "    AND m.deleted=0 " +
                        " GROUP BY parentMessageId ";
                List params = idParams;
                if (!channel.isNewUsersSeeOldMessages()) {
                    params = inSql.getParamsList();
                }
                List<ForumTopicCounts> totalCounts = DB.instance().queryBean(
                        ForumTopicCounts.class,
                        umSql2,
                        asArray(params, Object.class)
                );
                for (ForumTopicCounts count : totalCounts) {
                    ForumTopic topic = topicMap.get(count.getParentId());
                    topic.setTotalCount(count.getTotalCount());
                    topic.setLatestId(count.getLatestId());
                }
            }
        }

        // Get the latest messages for each topic
        if (topicMap.size() > 0) {
            List<Long> latestIds = list();
            for(ForumTopic ft: topicMap.values()) {
                latestIds.add(ft.getLatestId());
            }
            DB.SqlAndParams inSql = DB.instance().toInQueryParams(latestIds);
            List<Message> messages = filterBy("id", latestIds, FilterOperator.ANY).all();
            for (Message message: messages) {
                ForumTopic topic = topicMap.getOrDefault(message.getThreadId(), null);
                if (topic == null) {
                    continue;
                }
                IUser user = UserController.instance().forId(message.getFromUserId());
                if (user == null) {
                    continue;
                }
                UserProfile up = UserProfileController.instance().forStallionUser(user.getId());
                if (up == null) {
                    continue;
                }
                topic.setLatestUsername(user.getUsername());
                topic.setLatestAvatarUrl(up.getAvatarUrl());
                topic.setLatestAt(message.getCreatedAt());

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

    public MessageContext loadMessagesForChannel(Long userId, Long channelId, Integer page, boolean markRead) {
        MessageContext ctx = new MessageContext();
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
                "    m.messageJson as messageJson, " +
                "    m.parentMessageId, " +
                "    m.edited, " +
                "    m.title, " +
                "    m.fromUserId," +
                "    m.fromUsername," +
                "    m.createdAt, " +
                "    m.threadId, " +
                "    um.id AS userMessageId, " +
                "    um.encryptedPasswordHex, " +
                "    um.passwordVectorHex," +
                "    um.watched,  " +
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

        ctx.setMessages(messages);

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

    public static class MessageContext {
        private List<MessageCombo> messages;
        private int page;
        private int unreadCount = 0;
        private int unreadMentionsCount = 0;


        public List<MessageCombo> getMessages() {
            return messages;
        }

        public MessageContext setMessages(List<MessageCombo> messages) {
            this.messages = messages;
            return this;
        }


        public int getPage() {
            return page;
        }

        public MessageContext setPage(int page) {
            this.page = page;
            return this;
        }


        public int getUnreadCount() {
            return unreadCount;
        }

        public MessageContext setUnreadCount(int unreadCount) {
            this.unreadCount = unreadCount;
            return this;
        }


        public int getUnreadMentionsCount() {
            return unreadMentionsCount;
        }

        public MessageContext setUnreadMentionsCount(int unreadMentionsCount) {
            this.unreadMentionsCount = unreadMentionsCount;
            return this;
        }
    }


    public static class ThreadContext {
        private ForumTopic topic;
        private List<MessageCombo> messages;
        private int page;
        private int pageCount;
        private List<Long[]> allIdsDates = list();
        private int unreadCount = 0;
        private int unreadMentionsCount = 0;


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

        public List<Long[]> getAllIdsDates() {
            return allIdsDates;
        }

        public ThreadContext setAllIdsDates(List<Long[]> allIdsDates) {
            this.allIdsDates = allIdsDates;
            return this;
        }


        public int getUnreadCount() {
            return unreadCount;
        }

        public ThreadContext setUnreadCount(int unreadCount) {
            this.unreadCount = unreadCount;
            return this;
        }

        public int getUnreadMentionsCount() {
            return unreadMentionsCount;
        }

        public ThreadContext setUnreadMentionsCount(int unreadMentionsCount) {
            this.unreadMentionsCount = unreadMentionsCount;
            return this;
        }
    }

}
