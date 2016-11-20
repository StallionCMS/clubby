package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.ModelBase;
import io.stallion.services.Log;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_messages")
public class Message extends ModelBase {
    private long deleteAt = 0L;
    private long fromUserId = 0L;
    private long channelId = 0L;
    private long toUserId = 0L;
    private String messageEncryptedJson;
    private long parentMessageId = 0L;

    @Column
    public long getDeleteAt() {
        return deleteAt;
    }

    public Message setDeleteAt(long deleteAt) {
        this.deleteAt = deleteAt;
        return this;
    }

    @Column
    public long getFromUserId() {
        return fromUserId;
    }

    public Message setFromUserId(long fromUserId) {
        this.fromUserId = fromUserId;
        return this;
    }

    @Column
    public long getChannelId() {
        return channelId;
    }

    public Message setChannelId(long channelId) {
        this.channelId = channelId;
        return this;
    }

    @Column
    public long getToUserId() {
        return toUserId;
    }

    public Message setToUserId(long toUserId) {
        this.toUserId = toUserId;
        return this;
    }

    @Column
    public String getMessageEncryptedJson() {
        return messageEncryptedJson;
    }

    public Message setMessageEncryptedJson(String messageEncryptedJson) {
        this.messageEncryptedJson = messageEncryptedJson;
        return this;
    }

    @Column
    public long getParentMessageId() {
        return parentMessageId;
    }

    public Message setParentMessageId(long parentMessageId) {
        this.parentMessageId = parentMessageId;
        return this;
    }
}
