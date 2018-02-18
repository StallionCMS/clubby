package io.clubby.server;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nimbusds.jose.util.IOUtils;
import io.stallion.services.Log;
import io.stallion.utils.json.JSON;


public class CentralHostApiConnector {

    public static String baseUrl() {
        return "https://hosterlocal.clubby.io";
    }

    public static String version() {
        // TODO get from manifest
        return "1.0.0--SNAPSHOT";
    }

    public static boolean sendInviteEmail(String toName, String toEmail, Long userId, String inviterName, String inviteCode) {
        try {
            HttpResponse<String> response = Unirest
                    .post(baseUrl() + "/hosting-api/v1/notify/send-invite")
                    .header("Auth", ClubbyDynamicSettings.getLicense().getKey())
                    .body(
                            JSON.stringify(map(
                                    val("toName", toName),
                                    val("toEmail", toEmail),
                                    val("toUserId", userId),
                                    val("inviterName", inviterName),
                                    val("inviteCode", inviteCode),
                                    val("clubbyVersion", version())
                            ))
                    )
                    .asString();
            Log.fine("Invite email API call response: {0} {1}", response.getStatus(), response.getBody());
            if (response.getStatus() == 200) {
                return true;
            } else {
                return false;
            }
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean sendEmailTwoFactor(String toName, String toEmail, String code, Long toUserId) {
        try {
            HttpResponse<JsonNode> response = Unirest
                    .post(baseUrl() + "/hosting-api/v1/notify/send-email-two-factor")
                    .header("Auth", ClubbyDynamicSettings.getLicense().getKey())
                    .body(
                            JSON.stringify(map(
                                    val("toName", toName),
                                    val("toEmail", toEmail),
                                    val("toUserId", toUserId),
                                    val("code", code),
                                    val("clubbyVersion", version())
                            ))
                    )
                    .asJson();
            Log.fine("Two-factor email API call response: {0} {1}", response.getStatus(), response.getBody());
            if (response.getStatus() == 200) {
                return true;
            } else {
                return false;
            }
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean sendEmailChatNotification(String toName, String toEmail, String body) {
        try {
            HttpResponse<JsonNode> response = Unirest
                    .post(baseUrl() + "/hosting-api/v1/notify/send-email-chat-notification")
                    .header("Auth", ClubbyDynamicSettings.getLicense().getKey())
                    .body(JSON.stringify(map(
                            val("toName", toName),
                            val("toEmail", toEmail),
                            val("body", body),
                            val("clubbyVersion", version())
                    )))
                    .asJson();
            Log.fine("Chat notification API call response: {0} {1}", response.getStatus(), response.getBody());
            if (response.getStatus() == 200) {
                return true;
            } else {
                return false;
            }
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean sendMobilePushNotification(String title, String body, Map messageData, List<String> registrationTokens) {
        try {
            HttpResponse<JsonNode> response = Unirest
                    .post(baseUrl() + "/hosting-api/v1/notify/send-mobile-push-notification")
                    .header("Auth", ClubbyDynamicSettings.getLicense().getKey())
                    .body(JSON.stringify(map(
                            val("title", title),
                            val("body", body),
                            val("data", messageData),
                            val("registrationTokens", registrationTokens),
                            val("clubbyVersion", version())
                    )))
                    .asJson();
            Log.fine("Chat notification API call response: {0} {1}", response.getStatus(), response.getBody());
            if (response.getStatus() == 200) {
                return response.getBody().getObject().getBoolean("succeeded");
            } else {
                return false;
            }
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }
}
