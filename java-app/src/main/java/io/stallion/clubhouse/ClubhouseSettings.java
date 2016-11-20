package io.stallion.clubhouse;

import io.stallion.plugins.BasePluginSettings;
import io.stallion.reflection.PropertyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClubhouseSettings extends BasePluginSettings {

    public static ClubhouseSettings getInstance() {
        return getInstance(ClubhouseSettings.class, "clubhouse");
    }



    @Override
    public void assignDefaults() {
    }
}