package io.stallion.clubhouse;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.services.Log;


/**
 * "SELECT m.id, m.messageEncryptedJson, m.messageJson, m.edited, m.fromUserId, m.fromUsername, m.createdAt," +
 " um.encryptedMessageDecryptionKey, um.read  " +
 "  " +
 */
public class MessageCombo {
    private Long id = null;
    private String messageEncryptedJson = null;
    private String messageJson = null;
    private boolean edited = false;
    private Long fromUserId = null;
    private String fromUsername = null;
    private Timestamp createdAt = null;
    private boolean read = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;

    }

    public String getMessageEncryptedJson() {
        return messageEncryptedJson;
    }

    public void setMessageEncryptedJson(String messageEncryptedJson) {
        this.messageEncryptedJson = messageEncryptedJson;

    }

    public String getMessageJson() {
        return messageJson;
    }

    public void setMessageJson(String messageJson) {
        this.messageJson = messageJson;

    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;

    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;

    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;

    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;

    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;

    }
}
