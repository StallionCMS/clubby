package io.stallion.clubhouse.webSockets;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.exceptions.ClientException;
import io.stallion.services.Log;
import io.stallion.users.UserController;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ClientEndpoint
@ServerEndpoint(value="/events/")
public class WebSocketEventHandler {
    private static Map<Long, List<Session>> sessionsByUserId = map();
    @OnOpen
    public void onWebSocketConnect(Session sess)
    {
        sess.getOpenSessions();
        //encodeURIComponent(stallion.getCookie("stUserSession"))
        List<String> values = sess.getRequestParameterMap().get("stUserSession");
        if (values.size() == 0) {
            throw new ClientException("stUserSession query param required.");
        }
        String token = values.get(0);
        UserController.UserValetResult result = UserController.instance().cookieStringToUser(token);
        if (result == null || result.getUser() == null) {
            throw new ClientException("You do not have authorization to connect.", 403);
        }
        sess.getUserProperties().put("user", result.getUser());
        if (!sessionsByUserId.containsKey(result.getUser().getId())) {
            sessionsByUserId.put(result.getUser().getId(), list());
        }
        sessionsByUserId.get(result.getUser().getId()).add(sess);
        //boolean UserController.instance().checkCookieAndAuthorizeForCookieValue(token);
        System.out.println("Socket Connected: " + sess);
    }

    public static void sendMessageToUser(Long userId, String message) {
        List<Session> sessions = sessionsByUserId.getOrDefault(userId, Collections.emptyList());
        for(Session sess: sessions) {
            if (sess.isOpen()) {
                sess.getAsyncRemote().sendText(message);

            }
        }
    }

    @OnMessage
    public void onWebSocketText(String message)
    {
        System.out.println("Received TEXT message: " + message);
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason)
    {

        System.out.println("Socket Closed: " + reason);
    }

    @OnError
    public void onWebSocketError(Throwable cause)
    {
        cause.printStackTrace(System.err);
    }
}
