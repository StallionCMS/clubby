package io.clubby.server;

import java.time.ZonedDateTime;

import io.stallion.dataAccess.ModelBase;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_channel_members")
public class ChannelMember extends ModelBase {
    private Long userId = 0L;
    private Long channelId = 0L;
    private boolean owner = false;
    private ZonedDateTime joinedAt;
    private boolean canPost = true;
    private boolean hidden = false;
    private boolean favorite = false;


    @Column
    public Long getUserId() {
        return userId;
    }

    public ChannelMember setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    @Column
    public Long getChannelId() {
        return channelId;
    }

    public ChannelMember setChannelId(Long channelId) {
        this.channelId = channelId;
        return this;
    }


    @Column(nullable = false)
    public boolean isOwner() {
        return owner;
    }

    public ChannelMember setOwner(boolean owner) {
        this.owner = owner;
        return this;
    }

    @Column
    public ZonedDateTime getJoinedAt() {
        return joinedAt;
    }

    public ChannelMember setJoinedAt(ZonedDateTime joinedAt) {
        this.joinedAt = joinedAt;
        return this;
    }

    @Column(nullable = false)
    public boolean isCanPost() {
        return canPost;
    }

    public ChannelMember setCanPost(boolean canPost) {
        this.canPost = canPost;
        return this;
    }


    @Column(nullable = false)
    public boolean isHidden() {
        return hidden;
    }


    public ChannelMember setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    @Column(nullable = false)
    public boolean isFavorite() {
        return favorite;
    }

    public ChannelMember setFavorite(boolean favorite) {
        this.favorite = favorite;
        return this;
    }
}
