package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.UniqueKey;
import io.stallion.services.Log;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_user_profiles")
public class UserProfile extends ModelBase {
    private Long userId = 0L;
    private String publicKeyHex = "";
    private String encryptedPrivateKeyHex = "";
    private String encryptedPrivateKeyInitializationVectorHex = "";
    private String aboutMe = "";
    private String webSite = "";
    private String email = "";
    private boolean emailMeWhenMentioned = true;

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
    public String getPublicKeyHex() {
        return publicKeyHex;
    }

    public UserProfile setPublicKeyHex(String publicKeyHex) {
        this.publicKeyHex = publicKeyHex;
        return this;
    }

    @Column(columnDefinition = "longtext")
    public String getEncryptedPrivateKeyHex() {
        return encryptedPrivateKeyHex;
    }

    public UserProfile setEncryptedPrivateKeyHex(String encryptedPrivateKeyHex) {
        this.encryptedPrivateKeyHex = encryptedPrivateKeyHex;
        return this;
    }

    @Column(columnDefinition = "longtext")
    public String getEncryptedPrivateKeyInitializationVectorHex() {
        return encryptedPrivateKeyInitializationVectorHex;
    }

    public UserProfile setEncryptedPrivateKeyInitializationVectorHex(String encryptedPrivateKeyInitializationVectorHex) {
        this.encryptedPrivateKeyInitializationVectorHex = encryptedPrivateKeyInitializationVectorHex;
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
}
