package io.stallion.clubhouse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.filtering.Pager;
import io.stallion.services.Log;


public class MessageController extends StandardModelController<Message> {
    public static MessageController instance() {
        return (MessageController) DataAccessRegistry.instance().get("sch_messages");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(Message.class, MessageController.class, false);
    }

    public List<MessageCombo> loadMessagesForChannel(Long userId, Long channelId, Integer page) {
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
                "    um.encryptedPasswordHex, " +
                "    um.passwordVectorHex, " +
                "    um.read  " +
                "  " +
                " FROM sch_messages as m" +
                " INNER JOIN sch_user_messages AS um ON um.messageId=m.id " +
                " WHERE m.channelId=? AND um.userId=? AND m.deleted=0 AND um.deleted=0" +
                " ORDER BY m.createdAt DESC " +
                " LIMIT " + offset + ", " + limit;
        List<MessageCombo> messages = DB.instance().queryBean(MessageCombo.class, sql, channelId, userId);
        Collections.reverse(messages);
        // join on reactions
        return messages;
    }
}
