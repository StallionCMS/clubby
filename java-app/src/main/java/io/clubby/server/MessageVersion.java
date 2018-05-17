package io.clubby.server;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import com.google.api.client.util.Key;
import io.stallion.dataAccess.ModelBase;
import io.stallion.services.Log;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_message_versions")
public class MessageVersion extends ModelBase {
    private long messageId;
    private ZonedDateTime versionDate;
    private long versionUserId;


    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime purgeAt;
    private ZonedDateTime expiresAt;
    private long deletedAt = 0L;
    private long fromUserId = 0L;
    private String fromUsername = "";
    private long channelId = 0L;
    private String messageEncryptedJson;
    private String messageEncryptedJsonVector;
    private String messageJson;
    private String title = "";
    private long parentMessageId = 0L;
    private long threadId = 0L;
    private boolean edited = false;
    private ZonedDateTime editedAt;
    private boolean hereMentioned = false;
    private boolean channelMentioned = false;
    private List<String> usersMentioned = list();
    private boolean pinned = false;
    private ZonedDateTime threadUpdatedAt;
    private boolean isWiki = false;

    @Column
    public long getMessageId() {
        return messageId;
    }

    public MessageVersion setMessageId(long messageId) {
        this.messageId = messageId;
        return this;
    }

    @Column
    public ZonedDateTime getVersionDate() {
        return versionDate;
    }

    public MessageVersion setVersionDate(ZonedDateTime versionDate) {
        this.versionDate = versionDate;
        return this;
    }

    @Column
    public long getVersionUserId() {
        return versionUserId;
    }

    public MessageVersion setVersionUserId(long versionUserId) {
        this.versionUserId = versionUserId;
        return this;
    }

    @Column
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public MessageVersion setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Column
    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public MessageVersion setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @Column
    public ZonedDateTime getPurgeAt() {
        return purgeAt;
    }

    public MessageVersion setPurgeAt(ZonedDateTime purgeAt) {
        this.purgeAt = purgeAt;
        return this;
    }

    @Column
    public ZonedDateTime getExpiresAt() {
        return expiresAt;
    }

    public MessageVersion setExpiresAt(ZonedDateTime expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    @Column(nullable = false)
    public long getDeletedAt() {
        return deletedAt;
    }

    public MessageVersion setDeletedAt(long deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    @Column(nullable = false)
    public long getFromUserId() {
        return fromUserId;
    }

    public MessageVersion setFromUserId(long fromUserId) {
        this.fromUserId = fromUserId;
        return this;
    }

    @Column
    public String getFromUsername() {
        return fromUsername;
    }

    public MessageVersion setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
        return this;
    }

    @Column(nullable = false)
    public long getChannelId() {
        return channelId;
    }

    public MessageVersion setChannelId(long channelId) {
        this.channelId = channelId;
        return this;
    }


    @Column(columnDefinition = "longtext")
    public String getMessageEncryptedJson() {
        return messageEncryptedJson;
    }

    public MessageVersion setMessageEncryptedJson(String messageEncryptedJson) {
        this.messageEncryptedJson = messageEncryptedJson;
        return this;
    }

    @Column
    public String getMessageEncryptedJsonVector() {
        return messageEncryptedJsonVector;
    }

    public MessageVersion setMessageEncryptedJsonVector(String messageEncryptedJsonVector) {
        this.messageEncryptedJsonVector = messageEncryptedJsonVector;
        return this;
    }

    @Column
    public String getTitle() {
        return title;
    }

    public MessageVersion setTitle(String title) {
        this.title = title;
        return this;
    }


    @Column(columnDefinition = "longtext")
    public String getMessageJson() {
        return messageJson;
    }

    public MessageVersion setMessageJson(String messageJson) {
        this.messageJson = messageJson;
        return this;
    }

    @Column(nullable = false)
    public long getParentMessageId() {
        return parentMessageId;
    }

    public MessageVersion setParentMessageId(long parentMessageId) {
        this.parentMessageId = parentMessageId;
        return this;
    }

    @Column(nullable = false)
    public long getThreadId() {
        return threadId;
    }

    public MessageVersion setThreadId(long threadId) {
        this.threadId = threadId;
        return this;
    }

    @Column(nullable = false)
    public boolean isEdited() {
        return edited;
    }

    public MessageVersion setEdited(boolean edited) {
        this.edited = edited;
        return this;
    }

    @Column
    public ZonedDateTime getEditedAt() {
        return editedAt;
    }

    public MessageVersion setEditedAt(ZonedDateTime editedAt) {
        this.editedAt = editedAt;
        return this;
    }

    @Column(nullable = false)
    public boolean isPinned() {
        return pinned;
    }

    public MessageVersion setPinned(boolean pinned) {
        this.pinned = pinned;
        return this;
    }

    @Column
    public ZonedDateTime getThreadUpdatedAt() {
        return threadUpdatedAt;
    }

    public MessageVersion setThreadUpdatedAt(ZonedDateTime threadUpdatedAt) {
        this.threadUpdatedAt = threadUpdatedAt;
        return this;
    }

    @Column(nullable = false)
    public boolean isWiki() {
        return isWiki;
    }

    public MessageVersion setWiki(boolean wiki) {
        isWiki = wiki;
        return this;
    }

    /**
     * These columns only come from JSON, are not saved to the database
     * @return
     */
    public boolean isHereMentioned() {
        return hereMentioned;
    }

    public MessageVersion setHereMentioned(boolean hereMentioned) {
        this.hereMentioned = hereMentioned;
        return this;
    }


    public boolean isChannelMentioned() {
        return channelMentioned;
    }

    public MessageVersion setChannelMentioned(boolean channelMentioned) {
        this.channelMentioned = channelMentioned;
        return this;
    }


    public List<String> getUsersMentioned() {
        return usersMentioned;
    }

    public MessageVersion setUsersMentioned(List<String> usersMentioned) {
        this.usersMentioned = usersMentioned;
        return this;
    }


}
