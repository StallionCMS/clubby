package io.clubby.server;

import javax.persistence.Column;
import java.time.ZonedDateTime;


public class ForumTopic {
    private Long id = 0L;
    private String title = "";
    private ZonedDateTime createdAt = null;
    private Long fromUserId = null;
    private String fromUsername = null;
    private Long channelId = 0L;
    private ZonedDateTime threadUpdatedAt = null;
    private String lastEditedTopicUsername = null;
    private Long lastEditedTopicUserId = null;



    private Long firstMentionId = 0L;
    private Long firstUnreadId = 0L;

    private Boolean watched = false;
    private boolean pinned = false;

    private Long unreadCount = 0L;
    private Long mentions = 0L;
    private Long totalCount = 0L;
    private String avatarUrl = "";


    private Long latestId = 0L;
    private String latestUsername;
    private String latestAvatarUrl;
    private ZonedDateTime latestAt;

    public Long getId() {
        return id;
    }

    public ForumTopic setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getChannelId() {
        return channelId;
    }

    public ForumTopic setChannelId(Long channelId) {
        this.channelId = channelId;
        return this;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public ForumTopic setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ForumTopic setTitle(String title) {
        this.title = title;
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ForumTopic setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public ForumTopic setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
        return this;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public ForumTopic setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
        return this;
    }

    public Long getMentions() {
        return mentions;
    }

    public ForumTopic setMentions(Long mentions) {
        this.mentions = mentions;
        return this;
    }

    public ZonedDateTime getThreadUpdatedAt() {
        return threadUpdatedAt;
    }

    public ForumTopic setThreadUpdatedAt(ZonedDateTime threadUpdatedAt) {
        this.threadUpdatedAt = threadUpdatedAt;
        return this;
    }


    public String getLastEditedTopicUsername() {
        return lastEditedTopicUsername;
    }

    public ForumTopic setLastEditedTopicUsername(String lastEditedTopicUsername) {
        this.lastEditedTopicUsername = lastEditedTopicUsername;
        return this;
    }

    public Long getLastEditedTopicUserId() {
        return lastEditedTopicUserId;
    }

    public ForumTopic setLastEditedTopicUserId(Long lastEditedTopicUserId) {
        this.lastEditedTopicUserId = lastEditedTopicUserId;
        return this;
    }

    public Boolean getWatched() {
        return watched;
    }

    public ForumTopic setWatched(Boolean watched) {
        this.watched = watched;
        return this;
    }

    public boolean isPinned() {
        return pinned;
    }

    public ForumTopic setPinned(boolean pinned) {
        this.pinned = pinned;
        return this;
    }

    public Long getFirstMentionId() {
        return firstMentionId;
    }

    public ForumTopic setFirstMentionId(Long firstMentionId) {
        this.firstMentionId = firstMentionId;
        return this;
    }

    public Long getFirstUnreadId() {
        return firstUnreadId;
    }

    public ForumTopic setFirstUnreadId(Long firstUnreadId) {
        this.firstUnreadId = firstUnreadId;
        return this;
    }


    public Long getTotalCount() {
        return totalCount;
    }

    public ForumTopic setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public ForumTopic setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }


    public Long getLatestId() {
        return latestId;
    }

    public ForumTopic setLatestId(Long latestId) {
        this.latestId = latestId;
        return this;
    }


    public String getLatestUsername() {
        return latestUsername;
    }

    public ForumTopic setLatestUsername(String latestUsername) {
        this.latestUsername = latestUsername;
        return this;
    }


    public String getLatestAvatarUrl() {
        return latestAvatarUrl;
    }

    public ForumTopic setLatestAvatarUrl(String latestAvatarUrl) {
        this.latestAvatarUrl = latestAvatarUrl;
        return this;
    }

    public ZonedDateTime getLatestAt() {
        return latestAt;
    }

    public ForumTopic setLatestAt(ZonedDateTime latestAt) {
        this.latestAt = latestAt;
        return this;
    }
}
