package io.stallion.clubhouse;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.ModelBase;
import io.stallion.services.Log;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_messages")
public class Message extends ModelBase {
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
    private long parentMessageId = 0L;
    private boolean edited = false;
    private ZonedDateTime editedAt;

    @Column
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public Message setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Column
    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Message setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @Column
    public ZonedDateTime getPurgeAt() {
        return purgeAt;
    }

    public Message setPurgeAt(ZonedDateTime purgeAt) {
        this.purgeAt = purgeAt;
        return this;
    }

    @Column
    public ZonedDateTime getExpiresAt() {
        return expiresAt;
    }

    public Message setExpiresAt(ZonedDateTime expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    @Column(nullable = false)
    public long getDeletedAt() {
        return deletedAt;
    }

    public Message setDeletedAt(long deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    @Column(nullable = false)
    public long getFromUserId() {
        return fromUserId;
    }

    public Message setFromUserId(long fromUserId) {
        this.fromUserId = fromUserId;
        return this;
    }

    @Column
    public String getFromUsername() {
        return fromUsername;
    }

    public Message setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
        return this;
    }

    @Column(nullable = false)
    public long getChannelId() {
        return channelId;
    }

    public Message setChannelId(long channelId) {
        this.channelId = channelId;
        return this;
    }


    @Column(columnDefinition = "longtext")
    public String getMessageEncryptedJson() {
        return messageEncryptedJson;
    }

    public Message setMessageEncryptedJson(String messageEncryptedJson) {
        this.messageEncryptedJson = messageEncryptedJson;
        return this;
    }

    @Column
    public String getMessageEncryptedJsonVector() {
        return messageEncryptedJsonVector;
    }

    public Message setMessageEncryptedJsonVector(String messageEncryptedJsonVector) {
        this.messageEncryptedJsonVector = messageEncryptedJsonVector;
        return this;
    }

    @Column(columnDefinition = "longtext")
    public String getMessageJson() {
        return messageJson;
    }

    public Message setMessageJson(String messageJson) {
        this.messageJson = messageJson;
        return this;
    }

    @Column(nullable = false)
    public long getParentMessageId() {
        return parentMessageId;
    }

    public Message setParentMessageId(long parentMessageId) {
        this.parentMessageId = parentMessageId;
        return this;
    }

    @Column(nullable = false)
    public boolean isEdited() {
        return edited;
    }

    public Message setEdited(boolean edited) {
        this.edited = edited;
        return this;
    }

    @Column
    public ZonedDateTime getEditedAt() {
        return editedAt;
    }

    public Message setEditedAt(ZonedDateTime editedAt) {
        this.editedAt = editedAt;
        return this;
    }
}
