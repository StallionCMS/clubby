package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.UniqueKey;
import io.stallion.dataAccess.db.Converter;
import io.stallion.dataAccess.db.converters.JsonListConverter;
import io.stallion.dataAccess.db.converters.JsonMapConverter;
import io.stallion.dataAccess.db.converters.JsonTypedListConverter;
import io.stallion.services.Log;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_user_profiles")
public class UserProfile extends ModelBase {
    private Long userId = 0L;


    private String publicKeyJwkJson = "";
    private String privateKeyJwkEncryptedHex = "";
    private String privateKeyVectorHex = "";



    private String aboutMe = "";
    private String webSite = "";
    private String contactInfo = "";
    private String email = "";
    private boolean emailMeWhenMentioned = true;
    private boolean notifyWhenMentioned = true;
    private String avatarUrl = "";

    private String googleAuthenticatorKey = "";
    private List<String> googleAuthenticatorScratchCodes = list();
    private Boolean twoFactorEnabled = false;
    private String twoFactorCookieSecret = "";
    private List<TwoFactorSession> twoFactorSessions = list();


    @Column
    @UniqueKey
    public Long getUserId() {
        return userId;
    }

    public UserProfile setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    @Column(columnDefinition = "longtext")
    public String getPublicKeyJwkJson() {
        return publicKeyJwkJson;
    }

    public UserProfile setPublicKeyJwkJson(String publicKeyJwkJson) {
        this.publicKeyJwkJson = publicKeyJwkJson;
        return this;
    }

    @Column(columnDefinition = "longtext")
    public String getPrivateKeyJwkEncryptedHex() {
        return privateKeyJwkEncryptedHex;
    }

    public UserProfile setPrivateKeyJwkEncryptedHex(String privateKeyJwkEncryptedHex) {
        this.privateKeyJwkEncryptedHex = privateKeyJwkEncryptedHex;
        return this;
    }

    @Column
    public String getPrivateKeyVectorHex() {
        return privateKeyVectorHex;
    }

    public UserProfile setPrivateKeyVectorHex(String privateKeyVectorHex) {
        this.privateKeyVectorHex = privateKeyVectorHex;
        return this;
    }


    @Column
    public String getAboutMe() {
        return aboutMe;
    }

    public UserProfile setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
        return this;
    }

    @Column
    public String getContactInfo() {
        return contactInfo;
    }

    public UserProfile setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
        return this;
    }

    @Column
    public String getWebSite() {
        return webSite;
    }

    public UserProfile setWebSite(String webSite) {
        this.webSite = webSite;
        return this;
    }

    @Column
    public String getEmail() {
        return email;
    }

    public UserProfile setEmail(String email) {
        this.email = email;
        return this;
    }

    @Column(nullable = false)
    public boolean isEmailMeWhenMentioned() {
        return emailMeWhenMentioned;
    }

    public UserProfile setEmailMeWhenMentioned(boolean emailMeWhenMentioned) {
        this.emailMeWhenMentioned = emailMeWhenMentioned;
        return this;
    }

    @Column(nullable = false)
    public boolean isNotifyWhenMentioned() {
        return notifyWhenMentioned;
    }

    public UserProfile setNotifyWhenMentioned(boolean notifyWhenMentioned) {
        this.notifyWhenMentioned = notifyWhenMentioned;
        return this;
    }

    @Column
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public UserProfile setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    @Column
    public String getGoogleAuthenticatorKey() {
        return googleAuthenticatorKey;
    }

    public UserProfile setGoogleAuthenticatorKey(String googleAuthenticatorKey) {
        this.googleAuthenticatorKey = googleAuthenticatorKey;
        return this;
    }

    @Column
    @Converter(cls= JsonListConverter.class)
    public List<String> getGoogleAuthenticatorScratchCodes() {
        return googleAuthenticatorScratchCodes;
    }

    public UserProfile setGoogleAuthenticatorScratchCodes(List<String> googleAuthenticatorScratchCodes) {
        this.googleAuthenticatorScratchCodes = googleAuthenticatorScratchCodes;
        return this;
    }

    @Column
    public Boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public UserProfile setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
        return this;
    }

    @Column
    public String getTwoFactorCookieSecret() {
        return twoFactorCookieSecret;
    }

    public UserProfile setTwoFactorCookieSecret(String twoFactorCookieSecret) {
        this.twoFactorCookieSecret = twoFactorCookieSecret;
        return this;
    }

    @Column
    @Converter(cls=JsonTypedListConverter.class, elementClass = TwoFactorSession.class)
    public List<TwoFactorSession> getTwoFactorSessions() {
        return twoFactorSessions;
    }

    public UserProfile setTwoFactorSessions(List<TwoFactorSession> twoFactorSessions) {
        this.twoFactorSessions = twoFactorSessions;
        return this;
    }

    public class TwoFactorSession {
        private String name = "";
        private Long id = 0L;
        private String ip = "";
        private String userAgent = "";
        private String key = "";


        public String getName() {
            return name;
        }

        public TwoFactorSession setName(String name) {
            this.name = name;
            return this;
        }


        public Long getId() {
            return id;
        }

        public TwoFactorSession setId(Long id) {
            this.id = id;
            return this;
        }


        public String getIp() {
            return ip;
        }

        public TwoFactorSession setIp(String ip) {
            this.ip = ip;
            return this;
        }


        public String getUserAgent() {
            return userAgent;
        }

        public TwoFactorSession setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }


        public String getKey() {
            return key;
        }

        public TwoFactorSession setKey(String key) {
            this.key = key;
            return this;
        }
    }
}
