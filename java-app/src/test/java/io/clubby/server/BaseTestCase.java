package io.clubby.server;

import java.io.File;

import io.stallion.testing.JerseyIntegrationBaseCase;
import org.junit.BeforeClass;

import javax.ws.rs.core.Feature;


public class BaseTestCase  extends JerseyIntegrationBaseCase {

    static {
        System.setProperty("java.awt.headless", "true");
    }


    @BeforeClass
    public static void setUpClass() throws Exception {
        //String path = FilenameUtils.concat(System.getProperty("user.dir"), "../site");
        String path = new File(BaseTestCase.class.getResource("/test_site/conf/stallion.toml").getFile()).getParentFile().getParent();

        startApp(path, new ClubbyStallionApplication(), new Feature[0]);


    }
}

