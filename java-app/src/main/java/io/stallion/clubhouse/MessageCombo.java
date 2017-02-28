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
    private String messageEncryptedJsonVector = "";
    private String messageJson = null;

    private String encryptedPasswordHex = "";
    private String passwordVectorHex = "";

    private boolean edited = false;
    private Long fromUserId = null;
    private String fromUsername = null;
    private ZonedDateTime createdAt = null;
    private boolean read = true;

    public Long getId() {
        return id;
    }

    public MessageCombo setId(Long id) {
        this.id = id;
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

    public boolean isRead() {
        return read;
    }

    public MessageCombo setRead(boolean read) {
        this.read = read;
        return this;
    }
}
