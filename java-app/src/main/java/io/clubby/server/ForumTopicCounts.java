package io.clubby.server;


public class ForumTopicCounts {
    private Long parentId = 0L;
    private Long unreadCount = 0L;
    private Long mentions = 0L;
    private Long minId = 0L;
    private Long totalCount = 0L;
    private Long latestId = 0L;


    public Long getParentId() {
        return parentId;
    }

    public ForumTopicCounts setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public ForumTopicCounts setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
        return this;
    }

    public Long getMentions() {
        return mentions;
    }

    public ForumTopicCounts setMentions(Long mentions) {
        this.mentions = mentions;
        return this;
    }

    public Long getMinId() {
        return minId;
    }

    public ForumTopicCounts setMinId(Long minId) {
        this.minId = minId;
        return this;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public ForumTopicCounts setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    public Long getLatestId() {
        return latestId;
    }

    public ForumTopicCounts setLatestId(Long latestId) {
        this.latestId = latestId;
        return this;
    }
}
