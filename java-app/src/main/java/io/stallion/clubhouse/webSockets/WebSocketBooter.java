package io.stallion.clubhouse.webSockets;

import java.util.List;
import java.util.Map;

import javax.websocket.server.ServerContainer;

import io.stallion.boot.ServeCommandOptions;
import org.eclipse.jetty.plus.annotation.RunAs;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;




import static io.stallion.utils.Literals.*;

import io.stallion.services.Log;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;


public class WebSocketBooter {
    public Server boot(Server server, HandlerCollection handlerCollection, ServeCommandOptions options) {
        if (server == null) {
            server = new Server();
        }
        //ServerConnector connector = new ServerConnector(server);
        //connector.setPort(8091);
        //server.addConnector(connector);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/st-wsroot");

        //server.setHandler(context);
        handlerCollection.addHandler(context);
        context.setServer(server);
        try
        {
            // Initialize javax.websocket layer
            ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);

            // Add WebSocket endpoint to javax.websocket layer
            wscontainer.addEndpoint(WebSocketEventHandler.class);

            //server.start();
            server.dump(System.err);
            return server;
        }
        catch (Throwable t)
        {
            throw new RuntimeException(t);
        }
    }
}
