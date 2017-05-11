package io.stallion.clubhouse;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.ModelBase;
import io.stallion.services.Log;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_message_reactions")
public class MessageReaction extends ModelBase {
    private Long messageId;
    private Long userId;
    private String emoji;
    private String displayName;
    private ZonedDateTime createdAt;
    private Long recipientUserId;
    private String username;

    @Column
    public Long getMessageId() {
        return messageId;
    }

    public MessageReaction setMessageId(Long messageId) {
        this.messageId = messageId;
        return this;
    }

    @Column
    public Long getUserId() {
        return userId;
    }

    public MessageReaction setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    @Column
    public String getEmoji() {
        return emoji;
    }

    public MessageReaction setEmoji(String emoji) {
        this.emoji = emoji;
        return this;
    }

    @Column
    public String getDisplayName() {
        return displayName;
    }

    public MessageReaction setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    @Column
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public MessageReaction setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Column
    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public MessageReaction setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
        return this;
    }


    public String getUsername() {
        return username;
    }

    public MessageReaction setUsername(String username) {
        this.username = username;
        return this;
    }
}
