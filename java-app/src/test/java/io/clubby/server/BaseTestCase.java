package io.clubby.server;

import java.io.File;

import io.stallion.plugins.PluginRegistry;
import io.stallion.testing.AppIntegrationCaseBase;
import org.junit.BeforeClass;


public class BaseTestCase  extends AppIntegrationCaseBase {

    static {
        System.setProperty("java.awt.headless", "true");
    }


    @BeforeClass
    public static void setUpClass() throws Exception {
        //String path = FilenameUtils.concat(System.getProperty("user.dir"), "../site");
        String path = new File(BaseTestCase.class.getResource("/test_site/conf/stallion.toml").getFile()).getParentFile().getParent();
        startApp(path);
        ClubbyPlugin booter = new ClubbyPlugin();
        PluginRegistry.instance().loadPluginFromBooter(booter);
        booter.boot();
    }
}

