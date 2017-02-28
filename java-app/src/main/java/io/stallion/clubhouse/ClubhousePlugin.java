package io.stallion.clubhouse;

import io.stallion.Context;
import io.stallion.boot.ServeCommandOptions;
import io.stallion.clubhouse.webSockets.WebSocketBooter;
import io.stallion.hooks.HookRegistry;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.services.Log;
import io.stallion.restfulEndpoints.EndpointsRegistry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;

import java.util.List;

import static io.stallion.utils.Literals.list;

public class ClubhousePlugin extends StallionJavaPlugin {
    private Server server;
    @Override
    public String getPluginName() {
        return "clubhouse";
    }

    @Override
    public void boot() throws Exception {
        List<EndpointResource> endpoints = list(
                new AuthEndpoints(),
                new Endpoints(),
                new MessagingEndpoints(),
                new UserEndpoints()
        );
        for (EndpointResource ep: endpoints) {
            EndpointsRegistry.instance().addResource("", ep);
        }

        ChannelController.register();
        ChannelMemberController.register();
        MessageController.register();
        MessageReactionController.register();
        UserMessageController.register();
        UserProfileController.register();


    }

    @Override
    public void preStartJetty(Server server, HandlerCollection handlerCollection, ServeCommandOptions options) {
        server = new WebSocketBooter().boot(server, handlerCollection, options);
    }


}

