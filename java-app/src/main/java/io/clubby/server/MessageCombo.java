package io.clubby.server;

import javax.persistence.Column;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;


/**
 * "SELECT m.id, m.messageEncryptedJson, m.messageJson, m.edited, m.fromUserId, m.fromUsername, m.createdAt," +
 " um.encryptedMessageDecryptionKey, um.read  " +
 "  " +
 */
public class MessageCombo {
    private Long id = null;
    private Long channelId = null;
    private ChannelType channelType = ChannelType.CHANNEL;
    private String messageEncryptedJson = null;
    private String messageEncryptedJsonVector = "";
    private String messageJson = null;
    private Long parentMessageId = 0L;
    private Long threadId = 0L;



    private String encryptedPasswordHex = "";
    private String passwordVectorHex = "";

    private boolean edited = false;
    private Long fromUserId = null;
    private Long toUserId = null;
    private String fromUsername = null;
    private ZonedDateTime createdAt = null;
    private ZonedDateTime updatedAt = null;
    private Boolean read = true;
    private Map<String, List<String>> reactions = map();
    private Long userMessageId = 0L;
    private boolean mentioned = false;
    private String title = "";
    private Boolean watched = false;

    public Long getId() {
        return id;
    }

    public MessageCombo setId(Long id) {
        this.id = id;
        return this;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public MessageCombo setChannelType(ChannelType channelType) {
        this.channelType = channelType;
        return this;
    }



    public String getMessageEncryptedJson() {
        return messageEncryptedJson;
    }

    public MessageCombo setMessageEncryptedJson(String messageEncryptedJson) {
        this.messageEncryptedJson = messageEncryptedJson;
        return this;
    }

    public String getMessageEncryptedJsonVector() {
        return messageEncryptedJsonVector;
    }

    public MessageCombo setMessageEncryptedJsonVector(String messageEncryptedJsonVector) {
        this.messageEncryptedJsonVector = messageEncryptedJsonVector;
        return this;
    }

    public String getMessageJson() {
        return messageJson;
    }

    public MessageCombo setMessageJson(String messageJson) {
        this.messageJson = messageJson;
        return this;
    }

    public String getEncryptedPasswordHex() {
        return encryptedPasswordHex;
    }

    public MessageCombo setEncryptedPasswordHex(String encryptedPasswordHex) {
        this.encryptedPasswordHex = encryptedPasswordHex;
        return this;
    }

    public String getPasswordVectorHex() {
        return passwordVectorHex;
    }

    public MessageCombo setPasswordVectorHex(String passwordVectorHex) {
        this.passwordVectorHex = passwordVectorHex;
        return this;
    }

    public boolean isEdited() {
        return edited;
    }

    public MessageCombo setEdited(boolean edited) {
        this.edited = edited;
        return this;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public MessageCombo setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
        return this;
    }


    public Long getToUserId() {
        return toUserId;
    }

    public MessageCombo setToUserId(Long toUserId) {
        this.toUserId = toUserId;
        return this;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public MessageCombo setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public MessageCombo setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public MessageCombo setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public Boolean isRead() {
        return read;
    }

    public MessageCombo setRead(Boolean read) {
        this.read = read;
        return this;
    }


    public Map<String, List<String>> getReactions() {
        return reactions;
    }

    public MessageCombo setReactions(Map<String, List<String>> reactions) {
        this.reactions = reactions;
        return this;
    }


    public Long getUserMessageId() {
        return userMessageId;
    }

    public MessageCombo setUserMessageId(Long userMessageId) {
        this.userMessageId = userMessageId;
        return this;
    }

    public Long getChannelId() {
        return channelId;
    }

    public MessageCombo setChannelId(Long channelId) {
        this.channelId = channelId;
        return this;
    }

    public Long getThreadId() {
        return threadId;
    }

    public MessageCombo setThreadId(Long threadId) {
        this.threadId = threadId;
        return this;
    }

    public boolean isMentioned() {
        return mentioned;
    }

    public MessageCombo setMentioned(boolean mentioned) {
        this.mentioned = mentioned;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MessageCombo setTitle(String title) {
        this.title = title;
        return this;
    }

    public Long getParentMessageId() {
        return parentMessageId;
    }

    public MessageCombo setParentMessageId(Long parentMessageId) {
        this.parentMessageId = parentMessageId;
        return this;
    }

    public Boolean getWatched() {
        return watched;
    }

    public MessageCombo setWatched(Boolean watched) {
        this.watched = watched;
        return this;
    }
}
