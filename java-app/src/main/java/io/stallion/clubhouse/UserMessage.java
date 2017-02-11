package io.stallion.clubhouse;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.ModelBase;
import io.stallion.services.Log;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_user_messages")
public class UserMessage extends ModelBase {
    private Long messageId;
    private Long channelId;
    private Long userId;
    private String encryptedMessageDecryptionKey;
    private ZonedDateTime date;
    private boolean read = false;
    private boolean mentioned = false;
    private boolean hereMentioned = false;


    @Column
    public Long getMessageId() {
        return messageId;
    }

    public UserMessage setMessageId(Long messageId) {
        this.messageId = messageId;
        return this;
    }

    @Column
    public Long getChannelId() {
        return channelId;
    }

    public UserMessage setChannelId(Long channelId) {
        this.channelId = channelId;
        return this;
    }

    @Column
    public Long getUserId() {
        return userId;
    }

    public UserMessage setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    @Column
    public String getEncryptedMessageDecryptionKey() {
        return encryptedMessageDecryptionKey;
    }

    public UserMessage setEncryptedMessageDecryptionKey(String encryptedMessageDecryptionKey) {
        this.encryptedMessageDecryptionKey = encryptedMessageDecryptionKey;
        return this;
    }

    @Column
    public ZonedDateTime getDate() {
        return date;
    }

    public UserMessage setDate(ZonedDateTime date) {
        this.date = date;
        return this;
    }

    @Column(nullable = false)
    public boolean isRead() {
        return read;
    }

    public UserMessage setRead(boolean read) {
        this.read = read;
        return this;
    }

    @Column(nullable = false)
    public boolean isMentioned() {
        return mentioned;
    }

    public UserMessage setMentioned(boolean mentioned) {
        this.mentioned = mentioned;
        return this;
    }

    @Column(nullable = false)
    public boolean isHereMentioned() {
        return hereMentioned;
    }

    public UserMessage setHereMentioned(boolean hereMentioned) {
        this.hereMentioned = hereMentioned;
        return this;
    }
}
