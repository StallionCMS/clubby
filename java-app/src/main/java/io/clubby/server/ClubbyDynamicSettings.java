package io.clubby.server;

import io.stallion.contentPublishing.UploadedFile;
import io.stallion.contentPublishing.UploadedFileController;
import io.stallion.services.DynamicSettings;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.settings.childSections.EmailSettings;
import io.stallion.utils.DateUtils;
import io.stallion.utils.json.JSON;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;

import static io.stallion.utils.Literals.empty;


public class ClubbyDynamicSettings {
    private static final String GROUP = "clubby";

    private static UploadedFile iconImage = null;

    public static Boolean isUseClubbyHostForPushNotification() {
        if (getLicense() != null && getLicense().isValidForNotifications()) {
            return true;
        }
        return false;
    }

    public static Boolean isUseClubbyHostForEmail() {
        if (getLicense() != null && getLicense().isValidForNotifications()) {
            if ("clubbyhost".equals(getEmailType())) {
                return true;
            }
        }
        return false;
    }

    public static String getEmailType() {
        return getString("emailType");
    }

    public static void updateEmailType(String type) {
        updateString("emailType", type);
    }

    public static EmailSettings getEmailSettings() {
        return getObject("emailSettings", EmailSettings.class);
    }

    public static void updateEmailSettings(EmailSettings updatedEmailSettings) {
        EmailSettings es = getObject("emailSettings", EmailSettings.class);
        if (es == null) {
            es = new EmailSettings();
        } else {
            es = JSON.parse(JSON.stringify(es), EmailSettings.class);
        }
        es.setPort(updatedEmailSettings.getPort());
        es.setDefaultFromAddress(updatedEmailSettings.getDefaultFromAddress());
        es.setHost(updatedEmailSettings.getHost());
        es.setPassword(updatedEmailSettings.getPassword());
        es.setUsername(updatedEmailSettings.getUsername());
        es.setTls(updatedEmailSettings.getTls());
        updateObject("emailSettings", es);
        syncToStallionEmailSettings();
    }

    public static void syncToStallionEmailSettings() {
        EmailSettings es = getEmailSettings();
        if (es == null) {
            return;
        }
        if (Settings.instance().getEmail() == null) {
            Settings.instance().setEmail(getEmailSettings());
        } else {
            Settings.instance().getEmail().setTls(es.getTls());
            Settings.instance().getEmail().setDefaultFromAddress(es.getDefaultFromAddress());
            Settings.instance().getEmail().setHost(es.getHost());
            Settings.instance().getEmail().setPassword(es.getPassword());
            Settings.instance().getEmail().setUsername(es.getUsername());
            Settings.instance().getEmail().setPort(es.getPort());
        }
    }

    public static String getSiteName() {
        return getStringOrDefault("siteName", Settings.instance().getSiteName());
    }

    public static void updateSiteName(String siteName) {
        updateString("siteName", siteName);
    }

    public static Long getIconImageId() {
        return getObject("iconImageId", Long.class);
    }

    public static void updateIconImageId(Long iconImageId) {
        updateObject("iconImageId", iconImageId);
        iconImage = null;
    }

    public static UploadedFile getIconImage() {
        if (iconImage != null && !iconImage.getId().equals(getIconImageId())) {
            iconImage = null;
        }
        if (iconImage == null) {
            if (empty(getIconImageId())) {
                return null;
            } else {
                iconImage = (UploadedFile)UploadedFileController.instance().forId(getIconImageId());
            }
        }
        return iconImage;
    }

    public static void updateIconImage(UploadedFile iconImage) {
        updateObject("iconImage", iconImage);
    }

    public static License getLicense() {
        return getObject("license", License.class);
    }

    public static void updateLicenseKey(License license) {
        updateObject("license", license);
    }


    public static String getIconBase64() {
        String iconBase64 = "";

        File iconFile = null;
        if (getIconImage() != null) {
            String folder = Settings.instance().getDataDirectory() + "/uploaded-files/";
            String fullPath = folder + getIconImage().getCloudKey();
            File file = new File(fullPath);
            if (file.exists()) {
                iconFile = file;
            }
        }

        if (iconFile == null) {
            try {
                iconFile = new IconHelper().getOrCreateAutoIcon();
            } catch (IOException e) {
                Log.exception(e, "Error generating icon");
            }
        }

        if (iconFile != null) {
            try {

                byte[] bytes = FileUtils.readFileToByteArray(iconFile);
                iconBase64 = Base64.encodeBase64String(bytes);
            } catch (IOException e) {
                Log.exception(e, "Error reading and encoding icon file " + iconFile.getAbsolutePath());

            }
        }
        return iconBase64;
    }

    public static String getIconUrl() {
        UploadedFile iconImage = getIconImage();
        if (iconImage == null) {
            if (!empty(ClubbySettings.getInstance().getIconUrl())) {
                String url = ClubbySettings.getInstance().getIconUrl();
                if (!url.contains("//")) {
                    url = Settings.instance().getSiteUrl() + url;
                }
                return url;
            } else {
                return Settings.instance().getSiteUrl() + "/auto-logo-icon.png";
            }
        } else {
            return iconImage.getUrl();
        }

    }






    protected static String getStringOrDefault(String name, String defaultObject) {
        String val = getString(name);
        if (val == null) {
            return defaultObject;
        }
        return val;
    }


    protected static <T> T getObjectOrDefault(String name, Class<T> cls, T defaultObject) {
        T val = getObject(name, cls);
        if (val == null) {
            return defaultObject;
        }
        return val;
    }

    protected static String getString(String name) {
        return DynamicSettings.instance().get(GROUP, name);
    }

    protected static void updateString(String name, String value) {
        DynamicSettings.instance().put(GROUP, name, value);
    }

    protected static <T> T getObject(String name, Class<T> cls) {
        T obj = (T)DynamicSettings.instance().getParsedObject(GROUP, name);
        if (obj != null) {
            return obj;
        }
        String json = DynamicSettings.instance().get(GROUP, name);
        if (empty(json)) {
            return null;
        }
        if (Long.class.equals(cls)) {
            obj = (T)new Long(Long.parseLong(json));
        } else if (Double.class.equals(cls)) {
            obj = (T)new Double(Double.parseDouble(json));
        } else if (Integer.class.equals(cls)) {
            obj = (T)new Integer(Integer.parseInt(json));
        } else if (Float.class.equals(cls)) {
            obj = (T)new Float(Float.parseFloat(json));
        } else if (ZonedDateTime.class.equals(cls)) {
            obj = (T)DateUtils.ISO_FORMAT.parse(json);
        } else {
            obj = JSON.parse(json, cls);
        }
        DynamicSettings.instance().stashParsedObject(GROUP, name, obj);
        return obj;
    }

    protected static void updateObject(String name, Object obj) {
        String stringVal = null;
        if (obj instanceof Long || obj instanceof Integer || obj instanceof Float || obj instanceof Double) {
            stringVal = obj.toString();
        } else if (obj instanceof ZonedDateTime) {
            stringVal = DateUtils.ISO_FORMAT.format((ZonedDateTime)obj);
        } else {
            stringVal = JSON.stringify(obj);
        }

        DynamicSettings.instance().put(GROUP, name, stringVal);
        DynamicSettings.instance().stashParsedObject(GROUP, name, obj);

    }



}
