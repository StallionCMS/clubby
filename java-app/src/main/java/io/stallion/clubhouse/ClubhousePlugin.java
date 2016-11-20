package io.stallion.clubhouse;

import io.stallion.Context;
import io.stallion.hooks.HookRegistry;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.services.Log;
import io.stallion.restfulEndpoints.EndpointsRegistry;

public class ClubhousePlugin extends StallionJavaPlugin {

    @Override
    public String getPluginName() {
        return "clubhouse";
    }

    @Override
    public void boot() throws Exception {
        EndpointsRegistry.instance().addResource("", new Endpoints());

        ChannelController.register();
        ChannelMemberController.register();
        MessageController.register();
        UserProfileController.register();

    }
}

