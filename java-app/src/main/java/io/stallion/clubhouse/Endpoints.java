package io.stallion.clubhouse;


import java.util.List;
import java.util.Map;
import io.stallion.restfulEndpoints.EndpointsRegistry;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.templating.TemplateRenderer;
import io.stallion.Context;
import io.stallion.utils.Sanitize;
import static io.stallion.utils.Literals.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;



public class Endpoints implements EndpointResource {


    @GET
    @Path("/")
    @Produces("text/html")
    public Object app() {
        Map ctx = map(
            val("pluginName", "clubhouse"),
            val("theApplicationContextJson", Sanitize.htmlSafeJson(
                 map(
                     val("user", Context.getUser())
                 )
            ))
        );
        return TemplateRenderer.instance().renderTemplate("clubhouse:app.jinja", ctx);
    }

    @GET
    @Path("/hello-world")
    @Produces("text/html")
    public Object hello() {
        return "Hello, world.";
    }

}

