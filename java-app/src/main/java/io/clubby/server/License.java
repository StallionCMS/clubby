package io.clubby.server;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.mils;


public class License {
    private String key = "";
    private Long expiresAt = 0L;
    private LicenseType type = LicenseType.STANDARD;

    public boolean isValidForNotifications() {
        if (!empty(key) && expiresAt > mils()) {
            if (type.equals(LicenseType.STANDARD)) {
                return true;
            }
        }
        return false;
    }

    public String getKey() {
        return key;
    }

    public License setKey(String key) {
        this.key = key;
        return this;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public License setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public License.LicenseType getType() {
        return type;
    }

    public License setType(LicenseType type) {
        this.type = type;
        return this;
    }

    public enum LicenseType {
        STANDARD
    }
}
