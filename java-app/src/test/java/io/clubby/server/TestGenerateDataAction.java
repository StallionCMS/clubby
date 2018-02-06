package io.clubby.server;

import io.stallion.boot.CommandOptionsBase;
import org.junit.Test;


public class TestGenerateDataAction extends BaseTestCase {

    @Test
    public void testGenerateData() throws Exception {
        new GenerateDataAction().execute(new CommandOptionsBase());
    }
}
