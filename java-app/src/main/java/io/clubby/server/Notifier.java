package io.clubby.server;

import java.io.IOException;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.stallion.dataAccess.filtering.SortDirection;
import io.stallion.services.Log;
import io.stallion.utils.json.JSON;


public class Notifier {



    public static void init() throws IOException {



    }

    public static void sendNotification(Long userId, String title, String body, Map messageData) {
        MobileSession session = MobileSessionController
                .instance()
                .filter("userId", userId)
                .exclude("registrationToken", "")
                .sortBy("id", SortDirection.DESC)
                .first();
        if (session == null) {
            return;
        }


        if (ClubbyDynamicSettings.isUseClubbyHostForPushNotification()) {
            CentralHostApiConnector.sendMobilePushNotification(title, body, messageData, list(session.getRegistrationToken()));
            return;
        }
        if (empty(ClubbySettings.getInstance().getFirebaseServerKey())) {
            Log.warn("Tried to send a mobile push notification but no firebaseServerKey setting in conf/clubby.toml was configured.");
            return;
        }


        RequestBody b = new RequestBody()
                .setNotification(
                        new Notification()
                        .setBody(or(body, title))
                        .setTitle(title)
                )
                .setTo(session.getRegistrationToken())
                ;
        String json = JSON.stringify(b);
        try {
            HttpResponse<String> response = Unirest
                    .post("https://fcm.googleapis.com/fcm/send")
                    .header("Authorization", "key=" + ClubbySettings.getInstance().getFirebaseServerKey())
                    .header("Content-type", "application/json")
                    .body(json)
                    .asString();
            if (response.getStatus() == 200) {
                Log.info("Response from FCM was: {0}", response.getBody());
            } else {
                Log.warn("Error Response from FCM was: {0} {1}", response.getStatus(), response.getBody());
            }
        } catch (UnirestException e) {
            Log.exception(e, "Error sending notification.");
        }
        /*

        { "notification": {
    "title": "Portugal vs. Denmark",
    "body": "5 to 1"
  },
  "to" : "bk3RNwTe3H0:CI2k_HHwgIpoDKCIZvvDMExUdFQ3P1..."
}
         */


    }

    public static class RequestBody {
        private String to;
        private Notification notification;
        private String priority = "high";
        private boolean content_available = true;

        public Notification getNotification() {
            return notification;
        }

        public RequestBody setNotification(Notification notification) {
            this.notification = notification;
            return this;
        }

        public String getTo() {
            return to;
        }

        public RequestBody setTo(String to) {
            this.to = to;
            return this;
        }


        public String getPriority() {
            return priority;
        }

        public RequestBody setPriority(String priority) {
            this.priority = priority;
            return this;
        }

        public boolean isContent_available() {
            return content_available;
        }

        public RequestBody setContent_available(boolean content_available) {
            this.content_available = content_available;
            return this;
        }
    }

    public static class Notification {
        private String title = "";
        private String body = "";


        public String getTitle() {
            return title;
        }

        public Notification setTitle(String title) {
            this.title = title;
            return this;
        }


        public String getBody() {
            return body;
        }

        public Notification setBody(String body) {
            this.body = body;
            return this;
        }
    }
}
