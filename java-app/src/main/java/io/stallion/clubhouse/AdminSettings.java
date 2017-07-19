package io.stallion.clubhouse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;


import io.stallion.contentPublishing.UploadedFile;
import io.stallion.contentPublishing.UploadedFileController;
import io.stallion.dataAccess.db.DB;
import io.stallion.reflection.PropertyUtils;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.DateUtils;
import io.stallion.utils.json.JSON;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;


public class AdminSettings {

    private static String siteName;
    private static Long iconImageId;
    private static UploadedFile iconImage;
    private static Long logoImageId;
    private static UploadedFile logoImage;

    private static Map defaults = map(
            val("iconImageId", 0L)
    );

    public static String getIconBase64() {
        String iconBase64 = "";

        File iconFile = null;
        if (!empty(AdminSettings.getIconImageId())) {
            UploadedFile uf = (UploadedFile)UploadedFileController.instance().forId(AdminSettings.getIconImageId());
            if (uf != null) {
                String folder = Settings.instance().getDataDirectory() + "/uploaded-files/";
                String fullPath = folder + uf.getCloudKey();
                File file = new File(fullPath);
                if (file.exists()) {
                    iconFile = file;
                }
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
        if (iconImage == null) {
            if (!empty(getIconImageId())) {
                iconImage = (UploadedFile)UploadedFileController.instance().forId(getIconImageId());
            }
        }
        if (iconImage == null) {
            if (!empty(ClubhouseSettings.getInstance().getIconUrl())) {
                String url = ClubhouseSettings.getInstance().getIconUrl();
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


    public static void init() {
        siteName = Settings.instance().getSiteName();
        List<NameValue> nvs = DB.instance().queryBean(NameValue.class, "SELECT * FROM sch_admin_settings");

        for (NameValue nv: nvs) {
            try {
                Field field = AdminSettings.class.getDeclaredField(nv.name);
                field.set(null, nv.value);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }

    protected static Object getDefault(String fieldName, Class cls) {
        if (defaults.containsKey(fieldName)) {
            return defaults.get(fieldName);
        }

        if (PropertyUtils.isReadable(ClubhouseSettings.getInstance(), fieldName)) {
            Object val = PropertyUtils.getProperty(ClubhouseSettings.getInstance(), fieldName);
            if (val != null) {
                return val;
            }
        }

        if (cls == String.class) {
            return "";
        }
        if (cls == Boolean.class) {
            return false;
        }
        if (cls == Long.class) {
            return 0L;
        }
        if (cls == Integer.class) {
            return 0;
        }
        if (cls == Float.class) {
            return 0.0f;
        }
        if (cls == Double.class) {
            return 0.0d;
        }
        if (cls == ZonedDateTime.class) {
            return ZonedDateTime.of(1, 1, 1, 0, 0, 0, 0, UTC);
        }

        try {
            return cls.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }


    public static class NameValue {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public NameValue setName(String name) {
            this.name = name;
            return this;
        }
    }

    public static String getSiteName() {
        if (siteName == null) {
            siteName = fetch("siteName", String.class);
        }
        return siteName;
    }

    public static void setSiteName(String siteName) {
        AdminSettings.siteName = siteName;
        persist("siteName", siteName);
    }

    public static Long getIconImageId() {
        if (iconImageId == null) {
            iconImageId = fetch("iconImageId", Long.class);
        }
        return iconImageId;
    }

    public static void setIconImageId(Long iconImageId) {
        AdminSettings.iconImageId = iconImageId;
        iconImage = null;
        persist("iconImageId", iconImageId);
    }



    public static Long getLogoImageId() {
        if (logoImageId == null) {
            logoImageId = fetch("logoImageId", Long.class);
        }
        return logoImageId;
    }

    public static void setLogoImageId(Long logoImageId) {
        AdminSettings.logoImageId = logoImageId;
        persist("logoImageId", logoImageId);
    }



    protected static <T> T fetch(String fieldName, Class<? extends T> cls) {
        String val = (String)DB.instance().queryScalar("" +
                "SELECT value FROM sch_admin_settings WHERE name=?", fieldName
        );
        if (val == null) {

            return (T)getDefault(fieldName, cls);
        }
        if (cls == String.class) {
            return (T)val;
        }
        if (val.equals("")) {
            return (T)getDefault(fieldName, cls);
        }
        if (cls == Boolean.class) {
            return (T)new Boolean("true".equals(val));
        }
        if (cls == Long.class) {
            return (T)new Long(Long.parseLong(val));
        }
        if (cls == Integer.class) {
            return (T)new Integer(Integer.parseInt(val));
        }
        if (cls == ZonedDateTime.class) {
            return (T)ZonedDateTime.parse(val, DateUtils.SQL_FORMAT);
        }
        if (cls == String.class) {
            return (T)val;
        }
        if (val.startsWith("{")) {
            return JSON.parse(val, cls);
        }
        return (T)val.toString();
    }

    protected static void persist(String fieldName, Object value) {
        String val;
        if (value == null) {
            val = "";
        } else if (value instanceof ZonedDateTime) {
            val = DateUtils.SQL_FORMAT.format((ZonedDateTime)value);
        } else if (value instanceof String || value instanceof Long || value instanceof Float || value instanceof Integer) {
            val = value.toString();
        } else if (value.equals(false))  {
            val = "false";
        } else if (value.equals(true)) {
            val = "true";
        } else {
            val = JSON.stringify(value);
        }
        DB.instance().execute("" +
                " INSERT INTO sch_admin_settings (name, value) VALUES(?, ?) ON DUPLICATE KEY UPDATE value=VALUES(value)",
                fieldName,
                val
        );
    }
}
