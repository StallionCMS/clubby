package io.stallion.clubhouse;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.services.Log;
import io.stallion.utils.DateUtils;


public class ChannelUserWrapper {
    private String username;
    private String email;
    private String aboutMe = "";
    private String webSite = "";
    private String publicKeyHex = "";
    private Long id = 0L;
    private String displayName = "";
    private ZonedDateTime lastActiveAt = DateUtils.utcNow();

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

    public String getWebSite() {
        return webSite;
    }

    public ChannelUserWrapper setWebSite(String webSite) {
        this.webSite = webSite;
        return this;
    }

    public String getPublicKeyHex() {
        return publicKeyHex;
    }

    public ChannelUserWrapper setPublicKeyHex(String publicKeyHex) {
        this.publicKeyHex = publicKeyHex;
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
}
