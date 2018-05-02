package io.clubby.server;

import java.time.ZonedDateTime;

import io.stallion.dataAccess.ModelBase;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_user_messages")
public class UserMessage extends ModelBase {
    private Long messageId;
    private Long channelId;
    private Long userId;
    private String encryptedPasswordHex = "";
    private String passwordVectorHex = "";
    private ZonedDateTime date;
    private boolean read = false;
    private boolean mentioned = false;
    private boolean hereMentioned = false;
    private boolean emailNotifySent = false;
    private boolean mobileNotifyPending = false;
    private Boolean watched = false;


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

    @Column(columnDefinition = "text")
    public String getEncryptedPasswordHex() {
        return encryptedPasswordHex;
    }

    public UserMessage setEncryptedPasswordHex(String encryptedPasswordHex) {
        this.encryptedPasswordHex = encryptedPasswordHex;
        return this;
    }

    @Column
    public String getPasswordVectorHex() {
        return passwordVectorHex;
    }

    public UserMessage setPasswordVectorHex(String passwordVectorHex) {
        this.passwordVectorHex = passwordVectorHex;
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

    @Column(nullable = false)
    public boolean isEmailNotifySent() {
        return emailNotifySent;
    }

    public UserMessage setEmailNotifySent(boolean emailNotifySent) {
        this.emailNotifySent = emailNotifySent;
        return this;
    }


    @Column(nullable = false)
    public boolean isMobileNotifyPending() {
        return mobileNotifyPending;
    }

    public UserMessage setMobileNotifyPending(boolean mobileNotifyPending) {
        this.mobileNotifyPending = mobileNotifyPending;
        return this;
    }

    @Column
    public Boolean isWatched() {
        return watched;
    }

    public UserMessage setWatched(Boolean watched) {
        this.watched = watched;
        return this;
    }
}
