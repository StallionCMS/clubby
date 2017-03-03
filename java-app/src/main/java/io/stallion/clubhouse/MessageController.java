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
import org.apache.commons.lang3.StringUtils;


public class MessageController extends StandardModelController<Message> {
    public static MessageController instance() {
        return (MessageController) DataAccessRegistry.instance().get("sch_messages");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(Message.class, MessageController.class, false);
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

    public List<MessageCombo> loadMessagesForChannel(Long userId, Long channelId, Integer page, boolean markRead) {
        // load recent messages
        page = or(page, 1);
        page = page - 1;
        int limit = 50;
        int offset = page * limit;


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
                "    " +
                "  " +
                " FROM sch_messages as m" +
                " INNER JOIN sch_user_messages AS um ON um.messageId=m.id " +
                " WHERE m.channelId=? AND um.userId=? AND m.deleted=0 AND um.deleted=0" +
                " ORDER BY m.createdAt DESC " +
                " LIMIT " + offset + ", " + limit;
        List<MessageCombo> messages = DB.instance().queryBean(MessageCombo.class, sql, channelId, userId);
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
                if (mc.isRead()) {
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
