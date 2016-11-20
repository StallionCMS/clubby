package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.boot.CommandOptionsBase;
import io.stallion.services.Log;
import org.junit.Test;


public class TestGenerateDataAction extends BaseTestCase {

    @Test
    public void testGenerateData() throws Exception {
        new GenerateDataAction().execute(new CommandOptionsBase());
    }
}
