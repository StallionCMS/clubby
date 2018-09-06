package io.clubby.server;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;
import static org.junit.Assert.assertEquals;

import io.stallion.services.Log;
import io.stallion.testing.JerseyIntegrationBaseCase;
import io.stallion.utils.json.JSON;
import org.junit.Test;

import javax.ws.rs.core.Response;


public class TestEndpoints extends BaseTestCase {
    @Test
    public void testRoot() {
        Response response = target("/").request().get();
        assertEquals(200, response.getStatus());
        String output = response.readEntity(String.class);

        assertContains(output, "iconBase64");
    }
}
