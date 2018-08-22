package io.clubby.server;

import io.clubby.server.webSockets.WebSocketBooter;
import io.stallion.boot.ServeCommandOptions;
import io.stallion.contentPublishing.UploadedFileController;
import io.stallion.contentPublishing.UploadedFileEndpoints;
import io.stallion.jobs.JobCoordinator;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.services.SecureTempTokens;
import io.stallion.services.ShortCodeTokenController;
import io.stallion.settings.Settings;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.glassfish.jersey.server.ResourceConfig;

public class ClubbyPlugin extends StallionJavaPlugin {
    private Server server;
    @Override
    public String getPluginName() {
        return "clubby";
    }

    @Override
    public void buildResourceConfig(ResourceConfig rc) {
        rc.packages("io.clubby.server");
        rc.register(UploadedFileEndpoints.class);
    }

    @Override
    public void boot() throws Exception {

        AdminSettings.init();

        if ("/st-users/login".equals(Settings.instance().getUsers().getLoginPage())) {
            Settings.instance().getUsers().setLoginPage("/#/login");
        }

        ClubbyDynamicSettings.syncToStallionEmailSettings();


        UploadedFileController.register();
        ChannelController.register();
        ChannelMemberController.register();
        MessageController.register();
        MessageReactionController.register();
        MobileSessionController.register();
        UserMessageController.register();
        UserProfileController.register();
        UserStateController.register();
        SecureTempTokens.register();
        ShortCodeTokenController.register();
        MessageVersionController.register();

        EncryptionHelper.instance();





        Notifier.init();


        JobCoordinator.instance().registerJob(new SendNotificationsJob());


    }

    @Override
    public void preStartJetty(Server server, HandlerCollection handlerCollection, ServeCommandOptions options) {
        server = new WebSocketBooter().boot(server, handlerCollection, options);
    }


}

