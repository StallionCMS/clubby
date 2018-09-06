package io.clubby.server;

import io.clubby.server.webSockets.WebSocketBooter;
import io.stallion.StallionApplication;
import io.stallion.contentPublishing.UploadedFileController;
import io.stallion.contentPublishing.UploadedFileEndpoints;
import io.stallion.http.ServeCommandOptions;
import io.stallion.jobs.JobCoordinator;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.services.SecureTempTokens;
import io.stallion.services.ShortCodeTokenController;
import io.stallion.settings.Settings;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.List;

import static io.stallion.utils.Literals.list;

public class ClubbyStallionApplication extends StallionApplication {
    private Server server;

    @Override
    public String getName() {
        return "clubby";
    }

    @Override
    protected void onBuildJerseyResourceConfig(ResourceConfig rc) {
        List<Class> resources = list(
                UploadedFileEndpoints.class,
                AdminEndpoints.class,
                AuthEndpoints.class,
                ChannelEndpoints.class,
                Endpoints.class,
                MessagingEndpoints.class,
                UserEndpoints.class
        );
        for(Class cls: resources) {
            rc.register(cls);
        }
    }

    @Override
    public void onRegisterAll() {
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



        JobCoordinator.instance().registerJob(new SendNotificationsJob());

    }

    @Override
    public void preStartJetty(Server server, HandlerCollection handlerCollection, ServeCommandOptions options) {
        server = new WebSocketBooter().boot(server, handlerCollection, options);
    }



}

