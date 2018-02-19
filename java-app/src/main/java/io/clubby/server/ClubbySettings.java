package io.clubby.server;

import io.stallion.plugins.BasePluginSettings;
import io.stallion.settings.SettingMeta;

import javax.persistence.Column;

public class ClubbySettings extends BasePluginSettings {

    public static ClubbySettings getInstance() {
        return getInstance(ClubbySettings.class, "clubby");
    }


    @SettingMeta
    private String firebaseSettingsJson;
    @SettingMeta
    private String firebaseSubdomain;
    @SettingMeta
    private String firebaseServerKey;

    @SettingMeta
    private String iconUrl = "";

    @SettingMeta(val = "https://host.clubby.io")
    private String hostApiUrl;


    public String getFirebaseSettingsJson() {
        return firebaseSettingsJson;
    }

    public ClubbySettings setFirebaseSettingsJson(String firebaseSettingsJson) {
        this.firebaseSettingsJson = firebaseSettingsJson;
        return this;
    }


    public String getFirebaseSubdomain() {
        return firebaseSubdomain;
    }

    public ClubbySettings setFirebaseSubdomain(String firebaseSubdomain) {
        this.firebaseSubdomain = firebaseSubdomain;
        return this;
    }

    public String getFirebaseServerKey() {
        return firebaseServerKey;
    }

    public ClubbySettings setFirebaseServerKey(String firebaseServerKey) {
        this.firebaseServerKey = firebaseServerKey;
        return this;
    }

    @Column
    public String getIconUrl() {
        return iconUrl;
    }

    public ClubbySettings setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    public String getHostApiUrl() {
        return hostApiUrl;
    }

    public ClubbySettings setHostApiUrl(String hostApiUrl) {
        this.hostApiUrl = hostApiUrl;
        return this;
    }

    @Override
    public void assignDefaults() {
    }
}