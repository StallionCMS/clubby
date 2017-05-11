package io.stallion.clubhouse;


import java.util.List;
import java.util.Map;
import io.stallion.restfulEndpoints.EndpointsRegistry;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.templating.TemplateRenderer;
import io.stallion.Context;
import io.stallion.utils.Sanitize;
import io.stallion.utils.json.JSON;

import static io.stallion.utils.Literals.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;



public class Endpoints implements EndpointResource {


    @GET
    @Path("/")
    @Produces("text/html")
    public Object app() {
        UserProfile profile = new UserProfile();
        if (!Context.getUser().isAnon()) {
            profile = UserProfileController.instance().forUniqueKey("userId", Context.getUser().getId());
        }
        Context.getResponse().getMeta().setTitle("Clubhouse");
        Map ctx = map(
            val("pluginName", "clubhouse"),
            val("theApplicationContextJson", Sanitize.htmlSafeJson(
                 map(
                     val("user", Context.getUser()),
                     val("profile", profile)
                 )
            ))
        );
        return TemplateRenderer.instance().renderTemplate("clubhouse:app.jinja", ctx);
    }

    @GET
    @Path("/oembed-iframe")
    @Produces("text/html")
    public Object oembedIframe() {
        Map ctx = map(
                val("embedUrl", JSON.stringify(Context.getRequest().getQueryParams().getOrDefault("embedUrl", ""))),
                val("iframeId", JSON.stringify(Context.getRequest().getQueryParams().getOrDefault("iframeId", "")))
        );
        return TemplateRenderer.instance().renderTemplate("clubhouse:oembed.jinja", ctx);
    }



    @GET
    @Path("/emoji-test")
    @Produces("text/html")
    public Object emojiTest() {
        Map ctx = map();
        return TemplateRenderer.instance().renderTemplate("clubhouse:emoji.jinja", ctx);
    }

    @GET
    @Path("/hello-world")
    @Produces("text/html")
    public Object hello() {
        return "Hello, world.";
    }

}

