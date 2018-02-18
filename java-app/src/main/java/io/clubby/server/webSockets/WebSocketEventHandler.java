package io.clubby.server.webSockets;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.*;

import static io.stallion.utils.Literals.*;

import io.clubby.server.*;
import io.stallion.exceptions.ClientException;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.users.IUser;
import io.stallion.users.UserController;
import io.stallion.utils.json.JSON;

import org.eclipse.jetty.websocket.common.WebSocketSession;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ClientEndpoint
@ServerEndpoint(value="/events/")
public class WebSocketEventHandler {
    private static Map<Long, Map<String, Session>> sessionsByUserId = map();



    @OnOpen
    public void onWebSocketConnect(Session sess) throws IOException
    {
        sess.getOpenSessions();
        WebSocketSession wsSess = (WebSocketSession)sess;
        //encodeURIComponent(stallion.getCookie("stUserSession"))
        String token = null;
        List<String> values = sess.getRequestParameterMap().get("stUserSession");
        if (values == null || values.size() == 0) {
            Log.info("stUserSession is null");

            for(HttpCookie cookie:wsSess.getUpgradeRequest().getCookies()) {
                if (cookie.getName().equals("stUserSession")) {
                    token = cookie.getValue();
                }
            }
            //sess
        } else {
            token = values.get(0);
        }
        if (empty(token)) {
            sess.close(new CloseReason(CloseReason.CloseCodes.NOT_CONSISTENT, "stUserSession query param or Cookie required."));
            return;
        }

        if (!Settings.instance().getSiteUrl().equals(wsSess.getUpgradeRequest().getHeader("Origin"))) {
            String msg = "You are accessing site with URL " + Settings.instance().getSiteUrl() + " but the Origin header was " + wsSess.getUpgradeRequest().getHeader("Origin");
            sess.close(new CloseReason(CloseReason.CloseCodes.NOT_CONSISTENT, msg));
            throw new ClientException(msg, 403);
        }


        UserController.UserValetResult result = UserController.instance().cookieStringToUser(token);
        if (result == null || result.getUser() == null || result.getUser().isAnon()) {
            sess.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Not authorized. You must log in again."));
            return;
        }
        sess.getUserProperties().put("user", result.getUser());
        if (!sessionsByUserId.containsKey(result.getUser().getId())) {
            sessionsByUserId.put(result.getUser().getId(), map());
        }
        sessionsByUserId.get(result.getUser().getId()).put(sess.getId(), sess);
        //boolean UserController.instance().checkCookieAndAuthorizeForCookieValue(token);
        System.out.println("Socket Connected: " + sess);
        //notifyUserStateChange(
        UserStateController.instance().updateState(result.getUser().getId(), UserStateType.AWAKE);

        String message = JSON.stringify(map(val("type", "confirmed-ws-open"), val("userId", result.getUser().getId())));
        sess.getAsyncRemote().sendText(message);
    }


    public static void notifyNewMember(ChannelUserWrapper user) {
        String message = JSON.stringify(map(val("type", "new-member"), val("member", user)));
        notifyAll(message);
    }


    public static void notifyMemberUpdated(ChannelUserWrapper user) {
        String message = JSON.stringify(map(val("type", "member-updated"), val("member", user)));
        notifyAll(message);
    }

    public static void notifyChannelChanges(Long userId, Channel channel, String change) {
        String message = JSON.stringify(map(val("type", "channel-changes"), val("channel", channel), val("change", change)));
        for (Session session : sessionsByUserId.getOrDefault(userId, map()).values()) {
            if (session.isOpen()) {
                session.getAsyncRemote().sendText(message);
            }
        }
    }


    public static void notifyNewChannel(ChannelCombo channel) {
        String message = JSON.stringify(map(val("type", "new-channel"), val("channel", channel)));
        notifyAll(message);
    }


    public static void notifyChannelUpdated(ChannelCombo channel) {
        String message = JSON.stringify(map(val("type", "channel-updated"), val("channel", channel)));
        notifyAll(message);
    }

    public static void notifyAll(String message) {
        for(Map<String, Session> sessionMap: sessionsByUserId.values()) {
            for (Session session : sessionMap.values()) {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(message);
                }
            }
        }
    }


    public static void notifyUserStateChange(Long userId, UserStateType state) {
        String message = JSON.stringify(map(val("type", "state-change"), val("userId", userId), val("newState", state.toString())));
        for(Map<String, Session> sessionMap: sessionsByUserId.values()) {
            for (Session session : sessionMap.values()) {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(message);
                }
            }
        }
    }


    public static void sendMessageToUser(Long userId, String message) {
        Collection<Session> sessions = sessionsByUserId.getOrDefault(userId, map()).values();
        for (Iterator<Session> sessionIterator = sessions.iterator(); sessionIterator.hasNext();) {
            Session sess = sessionIterator.next();
            if (sess.isOpen()) {
                sess.getAsyncRemote().sendText(message);

            } else {
                try {
                    sessionIterator.remove();
                    //sessionsByUserId.get(userId).remove(sess.getId());
                } catch (Exception e) {
                    Log.exception(e, "Error removing session");
                }
            }
        }
    }

    @OnMessage
    public void onWebSocketText(String message, Session session)
    {
        System.out.println("Received TEXT message: " + message);
    }

    @OnClose
    public void onWebSocketClose(Session session, CloseReason reason)
    {
        IUser user = (IUser)session.getUserProperties().getOrDefault("user", null);
        // Remove the session from the dictionary
        if (user != null) {
            if (sessionsByUserId.containsKey(user.getId())) {
                if (sessionsByUserId.get(user.getId()).containsKey(session.getId())) {
                    sessionsByUserId.get(user.getId()).remove(session.getId());
                }

            }
            if (sessionsByUserId.getOrDefault(user.getId(), map()).values().size() == 0) {
                UserStateController.instance().updateState(user.getId(), UserStateType.OFFLINE);
            }
        }
        System.out.println("Socket Closed: " + reason);
    }

    @OnError
    public void onWebSocketError(Throwable cause)
    {
        cause.printStackTrace(System.err);
    }
}
