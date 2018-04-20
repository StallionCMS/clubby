package io.clubby.server;

import java.time.ZonedDateTime;

import io.stallion.users.IUser;
import io.stallion.utils.DateUtils;
import sun.plugin.util.*;


public class ChannelUserWrapper {
    private String username;
    private String email;
    private String aboutMe = "";
    private String webSite = "";
    private String contactInfo = "";
    private String publicKeyJwkJson = "";
    private Long id = 0L;
    private String displayName = "";
    private ZonedDateTime lastActiveAt = DateUtils.utcNow();
    private String state = "";
    private String avatarUrl = "";
    private Long channelMemberId = 0L;
    private boolean approved = false;

    public static ChannelUserWrapper fromUserAndProfile(IUser user, UserProfile profile, boolean onlineNow) {
        ChannelUserWrapper w = new ChannelUserWrapper()
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setAboutMe(profile.getAboutMe())
                .setWebSite(profile.getWebSite())
                .setContactInfo(profile.getContactInfo())
                .setPublicKeyJwkJson(profile.getPublicKeyJwkJson())
                .setDisplayName(user.getDisplayName())
                .setAvatarUrl(profile.getAvatarUrl())
                .setApproved(user.getApproved())
                .setId(user.getId());
        if (onlineNow) {
            w.setLastActiveAt(DateUtils.utcNow());
            w.setState("AWAKE");
        }
        return w;
    }

    public String getUsername() {
        return username;
    }

    public ChannelUserWrapper setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public ChannelUserWrapper setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public ChannelUserWrapper setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
        return this;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public ChannelUserWrapper setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
        return this;
    }

    public String getWebSite() {
        return webSite;
    }

    public ChannelUserWrapper setWebSite(String webSite) {
        this.webSite = webSite;
        return this;
    }

    public String getPublicKeyJwkJson() {
        return publicKeyJwkJson;
    }

    public ChannelUserWrapper setPublicKeyJwkJson(String publicKeyJwkJson) {
        this.publicKeyJwkJson = publicKeyJwkJson;
        return this;
    }

    public Long getId() {
        return id;
    }

    public ChannelUserWrapper setId(Long id) {
        this.id = id;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChannelUserWrapper setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ZonedDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public ChannelUserWrapper setLastActiveAt(ZonedDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
        return this;
    }


    public String getState() {
        return state;
    }

    public ChannelUserWrapper setState(String state) {
        this.state = state;
        return this;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public ChannelUserWrapper setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public Long getChannelMemberId() {
        return channelMemberId;
    }

    public ChannelUserWrapper setChannelMemberId(Long channelMemberId) {
        this.channelMemberId = channelMemberId;
        return this;
    }

    public boolean isApproved() {
        return approved;
    }

    public ChannelUserWrapper setApproved(boolean approved) {
        this.approved = approved;
        return this;
    }
}


