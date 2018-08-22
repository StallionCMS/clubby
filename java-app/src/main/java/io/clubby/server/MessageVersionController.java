package io.clubby.server;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.dataAccess.filtering.SortDirection;

import java.time.ZonedDateTime;
import java.util.List;


public class MessageVersionController extends StandardModelController<MessageVersion> {
    public static MessageVersionController instance() {
        return (MessageVersionController) DataAccessRegistry.instance().get("sch_message_versions");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(MessageVersion.class, MessageVersionController.class, false);
    }

    public List<MessageVersion> listVersionsForMessage(Long messageId) {
        return filter("messageId", messageId).sortBy("versionDate", SortDirection.DESC).all();
    }

    public void loadVersionIntoMessage(Long versionId, Message message) {

        MessageVersion mv = filter("id", versionId).filter("messageId", message.getId()).first();
        message
                .setMessageEncryptedJson(message.getMessageEncryptedJson())
                .setHereMentioned(message.isHereMentioned())
                .setChannelMentioned(message.isChannelMentioned())
                .setUsersMentioned(message.getUsersMentioned())
                .setMessageEncryptedJsonVector(message.getMessageEncryptedJsonVector())
                .setMessageJson(message.getMessageJson())
                .setTitle(message.getTitle())
                ;

    }

    public MessageVersion saveVersion(Message message, Long currentUserId, ZonedDateTime versionDate) {
        MessageVersion mv = new MessageVersion()

                .setChannelId(message.getChannelId())
                .setChannelMentioned(message.isChannelMentioned())
                .setEdited(message.isEdited())
                .setExpiresAt(message.getExpiresAt())
                .setUpdatedAt(message.getUpdatedAt())
                .setExpiresAt(message.getExpiresAt())
                .setEdited(message.isEdited())
                .setCreatedAt(message.getCreatedAt())
                .setUpdatedAt(message.getUpdatedAt())
                .setHereMentioned(message.isHereMentioned())
                .setMessageId(message.getId())
                .setMessageEncryptedJson(message.getMessageEncryptedJson())
                .setMessageJson(message.getMessageJson())
                .setMessageEncryptedJsonVector(message.getMessageEncryptedJsonVector())
                .setParentMessageId(message.getParentMessageId())
                .setPinned(message.isPinned())
                .setPurgeAt(message.getPurgeAt())
                .setThreadId(message.getThreadId())
                .setThreadUpdatedAt(message.getThreadUpdatedAt())
                .setTitle(message.getTitle())
                .setUsersMentioned(message.getUsersMentioned())
                .setWiki(message.isWiki())
                .setFromUsername(message.getFromUsername())
                .setFromUserId(message.getFromUserId())
                .setVersionUserId(currentUserId)
                .setVersionDate(versionDate)
                .setCreatedAt(message.getCreatedAt())
                ;
        save(mv);
        return mv;

    }
}
