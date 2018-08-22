package io.clubby.server;

import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.UniqueKey;
import io.stallion.dataAccess.db.Converter;
import io.stallion.dataAccess.db.converters.JsonLongListConverter;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.List;

import static io.stallion.utils.Literals.list;

@Table(name="sch_channels")
public class Channel extends ModelBase {
    private String name = "";
    private ChannelType channelType = ChannelType.CHANNEL;
    private int purgeAfterDays = 0;
    private boolean purgeAfterRead = false;
    private boolean hidden = false;
    private boolean defaultForNewUsers = false;
    private boolean newUsersSeeOldMessages = true;
    private boolean inviteOnly = false;
    private boolean allowReactions = true;
    private boolean displayEmbeds = true;
    private boolean encrypted = false;
    private String uniqueHash = null;
    private List<Long> directMessageUserIds = list();
    private boolean wikiStyle = false;


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
    public boolean isNewUsersSeeOldMessages() {
        return newUsersSeeOldMessages;
    }

    public Channel setNewUsersSeeOldMessages(boolean newUsersSeeOldMessages) {
        this.newUsersSeeOldMessages = newUsersSeeOldMessages;
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

    @Column(nullable = false)
    public boolean isEncrypted() {
        return encrypted;
    }

    public Channel setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    @Column
    @UniqueKey
    public String getUniqueHash() {
        return uniqueHash;
    }

    public Channel setUniqueHash(String uniqueHash) {
        this.uniqueHash = uniqueHash;
        return this;
    }


    @Column
    @Converter(cls= JsonLongListConverter.class)
    public List<Long> getDirectMessageUserIds() {
        return directMessageUserIds;
    }

    public Channel setDirectMessageUserIds(List<Long> directMessageUserIds) {
        this.directMessageUserIds = directMessageUserIds;
        return this;
    }

    @Column(nullable = false)
    public boolean isWikiStyle() {
        return wikiStyle;
    }

    public Channel setWikiStyle(boolean wikiStyle) {
        this.wikiStyle = wikiStyle;
        return this;
    }
}
