package io.stallion.clubhouse;

;
import java.nio.charset.Charset;


import java.security.Security;

import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import io.stallion.services.Log;
import org.apache.commons.codec.binary.Hex;
import javax.crypto.Cipher;

public class EncryptionHelper {

    private static EncryptionHelper _instance;

    public static EncryptionHelper instance() {
        if(_instance == null) {
            synchronized(EncryptionHelper.class) {
                if(_instance == null) {
                    _instance = new EncryptionHelper();
                }
            }
        }
        return _instance;
    }

    private Cipher cipher;


    private EncryptionHelper() {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
                                       //RSA/ECB/OAEPWithSHA-1AndMGF1Padding
            cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Encrypts the message
     *
     * @param message
     * @param userId
     * @return  returns a Hexadecimal encoded encrypted version of message
     * @throws InvalidKeySpecException
     * @throws ParseException
     * @throws JOSEException
     */
    public String encryptForUser(String message, Long userId) {

        try {
            UserProfile profile = UserProfileController.instance().forStallionUserOrNotFound(userId);
            String keyJson = profile.getPublicKeyJwkJson();
            RSAKey jwk = RSAKey.parse(keyJson);

            byte[] input = message.getBytes(Charset.forName("UTF-8"));

            cipher.init(Cipher.ENCRYPT_MODE, jwk.toPublicKey());
            byte[] encrypted = new byte[cipher.getOutputSize(input.length)];
            int enc_len = cipher.update(input, 0, input.length, encrypted, 0);
            enc_len += cipher.doFinal(encrypted, enc_len);

            Log.info("encrypted {0}", Arrays.toString(encrypted));
            return Hex.encodeHexString(encrypted);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
