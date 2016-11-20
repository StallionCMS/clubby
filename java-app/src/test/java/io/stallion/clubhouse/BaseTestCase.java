package io.stallion.clubhouse;

import java.io.File;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.email.EmailSender;
import io.stallion.plugins.PluginRegistry;
import io.stallion.services.Log;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.testing.Stubbing;
import org.apache.commons.io.FilenameUtils;
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
        ClubhousePlugin booter = new ClubhousePlugin();
        PluginRegistry.instance().loadPluginFromBooter(booter);
        booter.boot();
    }
}

