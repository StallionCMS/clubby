package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.ModelBase;
import io.stallion.services.Log;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_user_profiles")
public class UserProfile extends ModelBase {
    private String publicKey = "";
    private String encryptedPrivateKey = "";
    private Long userId = 0L;

    @Column
    public String getPublicKey() {
        return publicKey;
    }

    public UserProfile setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    @Column
    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public UserProfile setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
        return this;
    }

    @Column
    public Long getUserId() {
        return userId;
    }

    public UserProfile setUserId(Long userId) {
        this.userId = userId;
        return this;
    }
}
