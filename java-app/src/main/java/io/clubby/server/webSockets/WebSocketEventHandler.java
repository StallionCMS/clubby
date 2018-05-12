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

import javax.jws.soap.SOAPBinding;
import javax.persistence.Column;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ClientEndpoint
@ServerEndpoint(value="/events/")
public class WebSocketEventHandler {
    private static Map<Long, Map<String, Session>> sessionsByUserId = map();

    public static Map<Long, Map<String, Session>> getSessionsByUserId() {
        return sessionsByUserId;
    }


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

        if (sess.getRequestParameterMap().containsKey("userState") && sess.getRequestParameterMap().get("userState").size() > 0) {
            String state = sess.getRequestParameterMap().get("userState").get(0);
            sess.getUserProperties().put("userState", UserStateType.valueOf(state));
            updateStateMaybe(result.getUser().getId());
        }




        String message = JSON.stringify(map(val("type", "confirmed-ws-open"), val("userId", result.getUser().getId())));
        sess.getAsyncRemote().sendText(message);
    }

    public static void updateStateMaybe(Long userId) {
        UserStateType newState = null;
        if (sessionsByUserId.getOrDefault(userId, map()).values().size() == 0) {
            newState = UserStateType.OFFLINE;
        } else {
            boolean isAwake = false;
            for (Map.Entry<String, Session> entry: sessionsByUserId.getOrDefault(userId, map()).entrySet()) {
                UserStateType userState = (UserStateType)entry.getValue().getUserProperties().getOrDefault("userState", null);
                if (UserStateType.AWAKE.equals(userState)) {
                    isAwake = true;
                }
            }
            if (isAwake) {
                newState = UserStateType.AWAKE;
            } else {
                newState = UserStateType.IDLE;
            }
        }
        UserState existing = UserStateController.instance().forUniqueKey("userId", userId);
        if (existing == null || !newState.equals(existing.getState())) {
            UserStateController.instance().updateState(userId, newState);
        }
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
        Log.fine("Received TEXT message: " + message);
        int i = message.indexOf("\n\n");
        if (i > -1) {
            String action = message.substring(0, i);
            String json = message.substring(i);
            if (action.equals("updateUserState")) {
                receiveUpdateUserState(json, session);
            }
        }

    }

    public void receiveUpdateUserState(String json, Session session) {
        UserStateInfo info = JSON.parse(json, UserStateInfo.class);
        session.getUserProperties().put("userState", info.getState());
        IUser user = (IUser)session.getUserProperties().getOrDefault("user", null);
        if (user == null) {
            return;
        }
        Log.fine("New session user state for user {0} to {1}", user.getEmail(), info.getState());
        updateStateMaybe(user.getId());

    }


    public static class UserStateInfo {
        private UserStateType state;

        public UserStateType getState() {
            return state;
        }

        public UserStateInfo setState(UserStateType state) {
            this.state = state;
            return this;
        }
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
            updateStateMaybe(user.getId());
        }
        System.out.println("Socket Closed: " + reason);
    }

    @OnError
    public void onWebSocketError(Throwable cause)
    {
        cause.printStackTrace(System.err);
    }
}
