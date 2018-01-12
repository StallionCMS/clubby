package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.boot.CommandOptionsBase;
import io.stallion.services.Log;
import org.junit.Test;


public class TestEncryption extends BaseTestCase {

    @Test
    public void testEncryption() throws Exception {
        String message = "The time is: " + mils();
        String result = new EncryptionHelper().encryptForUser(message, 10000L);
        Log.info("Encryption result: {0}", result);
    }
}
