package io.stallion.clubhouse;

import io.stallion.plugins.BasePluginSettings;
import io.stallion.reflection.PropertyUtils;
import io.stallion.settings.SettingMeta;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClubhouseSettings extends BasePluginSettings {

    public static ClubhouseSettings getInstance() {
        return getInstance(ClubhouseSettings.class, "clubhouse");
    }

    @SettingMeta
    private String firebaseSettingsJson;
    @SettingMeta
    private String firebaseSubdomain;
    @SettingMeta
    private String firebaseServerKey;

    @SettingMeta
    private String iconUrl = "";


    public String getFirebaseSettingsJson() {
        return firebaseSettingsJson;
    }

    public ClubhouseSettings setFirebaseSettingsJson(String firebaseSettingsJson) {
        this.firebaseSettingsJson = firebaseSettingsJson;
        return this;
    }


    public String getFirebaseSubdomain() {
        return firebaseSubdomain;
    }

    public ClubhouseSettings setFirebaseSubdomain(String firebaseSubdomain) {
        this.firebaseSubdomain = firebaseSubdomain;
        return this;
    }

    public String getFirebaseServerKey() {
        return firebaseServerKey;
    }

    public ClubhouseSettings setFirebaseServerKey(String firebaseServerKey) {
        this.firebaseServerKey = firebaseServerKey;
        return this;
    }

    @Column
    public String getIconUrl() {
        return iconUrl;
    }

    public ClubhouseSettings setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    @Override
    public void assignDefaults() {
    }
}