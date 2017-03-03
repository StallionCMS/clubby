package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import com.fasterxml.jackson.core.type.TypeReference;
import io.stallion.Context;
import io.stallion.services.Log;
import io.stallion.utils.json.JSON;

import javax.persistence.Column;


public class ChannelCombo {
    private Long id;
    private String name;
    private boolean allowReactions = true;
    private boolean displayEmbeds = true;
    private ChannelType channelType;
    private boolean hasNew = false;
    private int mentionsCount = 0;
    private String directMessageUserIds = "";
    private List<Long> directMessageUserIdsList = list();
    private boolean owner;
    private boolean canPost;


    public Long getId() {
        return id;
    }

    public ChannelCombo setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ChannelCombo setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isAllowReactions() {
        return allowReactions;
    }

    public ChannelCombo setAllowReactions(boolean allowReactions) {
        this.allowReactions = allowReactions;
        return this;
    }

    public boolean isDisplayEmbeds() {
        return displayEmbeds;
    }

    public ChannelCombo setDisplayEmbeds(boolean displayEmbeds) {
        this.displayEmbeds = displayEmbeds;
        return this;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public ChannelCombo setChannelType(ChannelType channelType) {
        this.channelType = channelType;
        return this;
    }

    public boolean isHasNew() {
        return hasNew;
    }

    public ChannelCombo setHasNew(boolean hasNew) {
        this.hasNew = hasNew;
        return this;
    }

    public int getMentionsCount() {
        return mentionsCount;
    }

    public ChannelCombo setMentionsCount(int mentionsCount) {
        this.mentionsCount = mentionsCount;
        return this;
    }

    public List<Long> getDirectMessageUserIdsList() {
        if (empty(directMessageUserIds)) {
            return list();
        } else {
            TypeReference<List<Long>> typeRef = new TypeReference<List<Long>>() {};
            List<Long> userIds = JSON.parse(directMessageUserIds, typeRef);
            return filter(userIds, uid->!Context.getUser().getId().equals(uid));
        }
    }

    public String getDirectMessageUserIds() {
        return directMessageUserIds;
    }

    public ChannelCombo setDirectMessageUserIds(String directMessageUserIds) {
        this.directMessageUserIds = directMessageUserIds;
        return this;
    }


    public boolean isOwner() {
        return owner;
    }

    public ChannelCombo setOwner(boolean owner) {
        this.owner = owner;
        return this;
    }

    public boolean isCanPost() {
        return canPost;
    }

    public ChannelCombo setCanPost(boolean canPost) {
        this.canPost = canPost;
        return this;
    }
}
