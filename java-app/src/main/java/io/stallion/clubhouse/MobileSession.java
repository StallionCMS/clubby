package io.stallion.clubhouse;

import java.time.ZonedDateTime;

import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.UniqueKey;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_mobile_sessions")
public class MobileSession extends ModelBase {
    private String deviceName = "";
    private String ipAddress = "";
    private String location = "";
    private ZonedDateTime lastSignInAt;
    private OperatingSystems deviceOperatingSystem;
    private String registrationToken = "";
    private String sessionKey = "";
    private String passphraseEncryptionSecret = "";
    private Long userId = 0L;


    @Column(nullable = false)
    public String getDeviceName() {
        return deviceName;
    }

    public MobileSession setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    @Column(nullable = false)
    public String getIpAddress() {
        return ipAddress;
    }

    public MobileSession setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    @Column(nullable = false)
    public String getLocation() {
        return location;
    }

    public MobileSession setLocation(String location) {
        this.location = location;
        return this;
    }

    @Column
    public ZonedDateTime getLastSignInAt() {
        return lastSignInAt;
    }

    public MobileSession setLastSignInAt(ZonedDateTime lastSignInAt) {
        this.lastSignInAt = lastSignInAt;
        return this;
    }

    @Column(nullable = false)
    public OperatingSystems getDeviceOperatingSystem() {
        return deviceOperatingSystem;
    }

    public MobileSession setDeviceOperatingSystem(OperatingSystems deviceOperatingSystem) {
        this.deviceOperatingSystem = deviceOperatingSystem;
        return this;
    }

    @Column(nullable = false)
    public String getRegistrationToken() {
        return registrationToken;
    }

    public MobileSession setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
        return this;
    }

    @Column(nullable = false)
    @UniqueKey
    public String getSessionKey() {
        return sessionKey;
    }

    public MobileSession setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
        return this;
    }

    @Column(nullable = false)
    public String getPassphraseEncryptionSecret() {
        return passphraseEncryptionSecret;
    }

    public MobileSession setPassphraseEncryptionSecret(String passphraseEncryptionSecret) {
        this.passphraseEncryptionSecret = passphraseEncryptionSecret;
        return this;
    }

    @Column
    public Long getUserId() {
        return userId;
    }

    public MobileSession setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public static enum OperatingSystems {
        ANDROID, IOS, OTHER
    }

}
