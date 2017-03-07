package io.stallion.clubhouse;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.services.Log;

import javax.persistence.Column;


public class ForumTopicCounts {
    private Long parentId = 0L;
    private Long unreadCount = 0L;
    private Long mentions = 0L;


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

}
