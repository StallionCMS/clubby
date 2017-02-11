package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.ModelBase;
import io.stallion.services.Log;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_channels")
public class Channel extends ModelBase {
    private String name = "";
    private ChannelType channelType = ChannelType.CHANNEL;
    private int purgeAfterDays = 0;
    private boolean purgeAfterRead = false;
    private boolean hidden = false;
    private boolean defaultForNewUsers = false;
    private boolean inviteOnly = false;
    private boolean allowReactions = true;
    private boolean displayEmbeds = true;


    @Column
    public String getName() {
        return name;
    }

    public Channel setName(String name) {
        this.name = name;
        return this;
    }

    @Column
    public ChannelType getChannelType() {
        return channelType;
    }

    public Channel setChannelType(ChannelType channelType) {
        this.channelType = channelType;
        return this;
    }

    @Column
    public int getPurgeAfterDays() {
        return purgeAfterDays;
    }

    public Channel setPurgeAfterDays(int purgeAfterDays) {
        this.purgeAfterDays = purgeAfterDays;
        return this;
    }

    @Column(nullable = false)
    public boolean isPurgeAfterRead() {
        return purgeAfterRead;
    }

    public Channel setPurgeAfterRead(boolean purgeAfterRead) {
        this.purgeAfterRead = purgeAfterRead;
        return this;
    }

    @Column(nullable = false)
    public boolean isHidden() {
        return hidden;
    }

    public Channel setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    @Column(nullable = false)
    public boolean isDefaultForNewUsers() {
        return defaultForNewUsers;
    }

    public Channel setDefaultForNewUsers(boolean defaultForNewUsers) {
        this.defaultForNewUsers = defaultForNewUsers;
        return this;
    }

    @Column(nullable = false)
    public boolean isInviteOnly() {
        return inviteOnly;
    }

    public Channel setInviteOnly(boolean inviteOnly) {
        this.inviteOnly = inviteOnly;
        return this;
    }

    @Column(nullable = false)
    public boolean isAllowReactions() {
        return allowReactions;
    }

    public Channel setAllowReactions(boolean allowReactions) {
        this.allowReactions = allowReactions;
        return this;
    }

    @Column(nullable = false)
    public boolean isDisplayEmbeds() {
        return displayEmbeds;
    }

    public Channel setDisplayEmbeds(boolean displayEmbeds) {
        this.displayEmbeds = displayEmbeds;
        return this;
    }
}
