package io.stallion.clubhouse;

import static io.stallion.utils.Literals.*;

import io.stallion.Context;
import io.stallion.clubhouse.emailers.LoginVerifyEmailer;
import io.stallion.contentPublishing.UploadedFile;
import io.stallion.contentPublishing.UploadedFileController;
import io.stallion.email.ContactableEmailer;
import io.stallion.exceptions.ClientException;
import io.stallion.requests.Site;
import io.stallion.restfulEndpoints.BodyParam;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.MinRole;
import io.stallion.restfulEndpoints.XSRF;
import io.stallion.services.*;
import io.stallion.settings.Settings;
import io.stallion.users.IUser;
import io.stallion.users.Role;
import io.stallion.users.UserController;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.swing.*;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Produces("application/json")
@Path("/clubhouse-api/auth")
public class AuthEndpoints implements EndpointResource {


    @POST
    @Path("/login-new-device-step1-get-salt")
    @XSRF(false)
    public Object loginNewDeviceStep1GetSalt(@BodyParam("username") String username) {
        checkLoginThrottle(username);

        IUser user = UserController.instance().forUsername(username);
        if (emptyInstance(user)) {
            user = UserController.instance().forEmail(username);
        }
        String salt = UUID.randomUUID().toString().replace("-", "");
        if (!emptyInstance(user)) {
            UserProfile profile = UserProfileController.instance().forStallionUser(user.getId());
            salt = profile.getPasswordSalt();
        }
        return map(
                val("salt", salt)
        );

    }




    @POST
    @Path("/login-new-device-step2-check-password-partial")
    @XSRF(false)
    public Object loginNewDeviceStep2CheckPasswordPartial(
            @BodyParam("username") String username,
            @BodyParam("password4chars") String password4,
            @BodyParam(value = "rememberDeviceToken", required = false, allowEmpty = true) String rememberDeviceToken
    )
    {
        checkLoginThrottle(username);

        IUser user = UserController.instance().forUsername(username);
        if (emptyInstance(user)) {
            user = UserController.instance().forEmail(username);
        }
        if (emptyInstance(user)) {
            Log.fine("User not found for " + username);
            recordAndThrowInvalidLogin(username);
            return null;
        }

        UserProfile profile = UserProfileController.instance().forStallionUser(user.getId());

        if (!password4.equals(profile.getPasswordFourCharactersHashed())) {
            Log.fine("Passed in password4chars does not match value in user profile");
            recordAndThrowInvalidLogin(username);
            return null;
        }

        // TODO: make this spelled right!
        if ("true".equals(Context.getRequest().getQueryParams().get("appLoginStep2"))) {
            return map(
                    val("userId", user.getId()),
                    val("iconBase64", AdminSettings.getIconBase64()),
                    val(
                            "site", new Site().setName(AdminSettings.getSiteName())
                    )
            );
        } else if (user.getId().equals(Context.getUser().getId())) {
            Map ctx = makePrivateKeyLoginContext(Context.getUser());
            ctx.put("nextStep", "validatePrivateKey");
            return ctx;
            // TODO remove 'true'
        } else if (!empty(rememberDeviceToken) && checkDeviceRemembered(rememberDeviceToken, user)) {
            Map ctx = makePrivateKeyLoginContext(user);
            ctx.put("nextStep", "validatePrivateKey");
            return ctx;
        } else if (!empty(profile.getGoogleAuthenticatorKey())) {
            return map(val("nextStep", "googleAuth"));
        } else {

            ShortCodeToken scode = ShortCodeTokenController.instance().newToken("login-confirm-" + user.getId());

            new LoginVerifyEmailer(user, scode).sendEmail();

            return map(
                    val("nextStep", "emailVerify"),
                    val("email", user.getEmail()),
                    val("key", scode.getKey())
            );
        }



    }


    @POST
    @Path("/login-with-remember-me-step1")
    public Object loginNewDeviceWithRememberMeStep1(
            @BodyParam("username") String username,
            @BodyParam("rememberDeviceToken") String rememberDeviceToken
    ) {
        checkLoginThrottle(username);



        IUser user = UserController.instance().forUsername(username);
        if (emptyInstance(user)) {
            user = UserController.instance().forEmail(username);
        }
        if (emptyInstance(user)) {
            Log.fine("User not found for " + username);
            recordAndThrowInvalidLogin(username);
            return null;
        }

        if (!checkDeviceRemembered(rememberDeviceToken, user)) {
            throw new ClientException("Invalid token, must log in as new device", 403);
        }

        Map ctx = makePrivateKeyLoginContext(user);
        ctx.put("nextStep", "validatePrivateKey");
        return ctx;

    }

    @POST
    @Path("/login-new-device-step3-verify-token")
    public Object loginNewDeviceStep3VerifyShortCode(
            @BodyParam("username") String username, @BodyParam("tokenKey") String tokenKey, @BodyParam("tokenCode") String code) throws Exception {
        checkLoginThrottle(username);

        IUser user = UserController.instance().forUsername(username);
        if (emptyInstance(user)) {
            user = UserController.instance().forEmail(username);
        }
        if (emptyInstance(user)) {
            Log.fine("User not found for " + username);
            return recordAndThrowInvalidLogin(username);
        }

        boolean valid = ShortCodeTokenController.instance().verify("login-confirm-" + user.getId(), tokenKey, code);
        if (!valid) {
            Log.fine("Login token not valid.");
            return recordAndThrowInvalidLogin(username);
        }
        return makePrivateKeyLoginContext(user);
    }



    public Map makePrivateKeyLoginContext(IUser user)  {
        String tokenKey = "login-user-" + user.getId();
        TempToken tempToken = SecureTempTokens.instance().getOrCreate(
                tokenKey,
                utcNow().plusMinutes(30)
        );

        String tokenEncryptedHex = EncryptionHelper.instance().encryptForUser(tempToken.getToken(), user.getId());

        UserProfile profile = UserProfileController.instance().forStallionUserOrNotFound(user.getId());

        return map(
                val("privateKeyJwkEncryptedHex", profile.getPrivateKeyJwkEncryptedHex()),
                val("privateKeyVectorHex", profile.getPrivateKeyVectorHex()),
                val("tokenKey", tokenKey),
                val("tokenEncryptedHex", tokenEncryptedHex)
        );
    }


    @POST
    @Path("/private-key-login-step2")
    public Object privateKeyLoginStep2(
            @BodyParam("username") String username,
            @BodyParam("tokenKey") String tokenKey,
            @BodyParam("token") String token,
            @BodyParam("generateElectronAuthCookie") Boolean generateElectronAuthCookie
    ) throws Exception {
        IUser user = UserController.instance().forUsername(username);
        if (emptyInstance(user)) {
            user = UserController.instance().forEmail(username);
        }
        if (emptyInstance(user)) {
            throw new ClientException("Invalid login information.");
        }
        //String tokenKey = "login-user-" + user.getId();
        TempToken tempToken = SecureTempTokens.instance().getOrCreate(
                tokenKey,
                utcNow().plusMinutes(30)
        );

        if (!tempToken.getToken().equals(token)) {
            throw new ClientException("Invalid login information.");
        }

        UserProfile profile = UserProfileController.instance().forStallionUserOrNotFound(user.getId());
        UserController.instance().addSessionCookieForUser(user, false);


        String electronAuthCookie = null;
        if (generateElectronAuthCookie) {
            electronAuthCookie = UserController.instance().userToCookieString(user, true);
        }


        return map(
                val("rememberDeviceToken", makeRememberDeviceCookie(user)),
                val("user", user),
                val("userProfile", profile),
                val("electronAuthCookie", electronAuthCookie),
                val("defaultChannelId", ChannelController.instance().getFirstUserChannel(user.getId()))
        );


    }


    private static String REMEMBER_DEVICE_COOKIE_NAME = "cbRememberDevice";

    public String makeRememberDeviceCookie(IUser user) {
        String token = UserController.instance().makeEncryptedToken(user, REMEMBER_DEVICE_COOKIE_NAME, user.getId().toString());
        return user.getUsername() + "|" + user.getId() + "|" + token;
    }

    public boolean checkDeviceRemembered(String token, IUser user) {
        IUser actual = checkDeviceRemembered(token);
        if (actual != null && actual.getId().equals(user.getId())) {
            return true;
        }
        return false;
    }


    public IUser checkDeviceRemembered(String token) {
        if (empty(token) || !token.contains("|")) {
            return null;
        }
        String[] parts = StringUtils.split(token, "|", 3);
        if (!StringUtils.isNumeric(parts[1])) {
            return null;
        }
        Long userId = Long.parseLong(parts[1]);
        IUser user = UserController.instance().forId(userId);
        if (user == null) {
            return null;
        }

        String value = UserController.instance().readEncryptedToken(user, REMEMBER_DEVICE_COOKIE_NAME, parts[2], 24 * 60 * 365);
        if (empty(value) || !user.getId().toString().equals(value)) {
            return null;
        }
        return user;

    }



    @POST
    @Path("/login")
    public Object login(@BodyParam("username") String username, @BodyParam("password") String password) {
        if (username.contains("@")) {
            IUser fromEmail = UserController.instance().forEmail(username);
            if (fromEmail != null) {
                username = fromEmail.getUsername();
            }
        }
        IUser user = UserController.instance().loginUser(username, password, false);
        UserProfile up = UserProfileController.instance().forStallionUserOrNotFound(user.getId());
        return map(
                val(
                        "user", user
                ),

                val("defaultChannelId", ChannelController.instance().getFirstUserChannel(user.getId())),
                val("userProfile", up)
        );
    }

    @POST
    @Path("/mobile-login")
    @XSRF(false)
    public Object mobileLogin(
            @BodyParam("username") String username,
            @BodyParam("password") String password,
            @BodyParam(value = "registrationToken", allowEmpty = true) String registrationToken,
            @BodyParam("deviceName") String deviceName,
            @BodyParam("deviceId") String deviceId,
            @BodyParam("deviceOperatingSystem") MobileSession.OperatingSystems deviceOS
                              ) {
        if (username.contains("@")) {
            IUser fromEmail = UserController.instance().forEmail(username);
            if (fromEmail != null) {
                username = fromEmail.getUsername();
            }
        }

        IUser user = null;
        if (password.length() > 50) {
            String[] parts = password.split("&");
            if (parts.length == 2 && StringUtils.isNumeric(parts[0])) {
                if (UserController.instance().checkCookieAndAuthorizeForCookieValue(password)) {
                    user = Context.getUser();
                }
            }
        }

        if (user == null) {
            user = UserController.instance().checkUserLoginValid(username, password);
        }

        // Delete previous sessions from this device
        if (!empty(deviceId)) {
            List<MobileSession> existings = MobileSessionController.instance()
                    .filter("deviceId", deviceId)
                    .filter("userId", user.getId())
                    .all();
            for(MobileSession ms: existings) {
                MobileSessionController.instance().hardDelete(ms);
            }
        }

        MobileSession mb = new MobileSession()
                .setDeviceName(deviceName)
                .setDeviceOperatingSystem(deviceOS)
                .setIpAddress(Context.getRequest().getActualIp())
                .setLastSignInAt(utcNow())
                .setDeviceId(deviceId)
                .setPassphraseEncryptionSecret(GeneralUtils.randomTokenBase32(40))
                .setRegistrationToken(registrationToken)
                .setSessionKey(UUID.randomUUID() + "-" + GeneralUtils.randomTokenBase32(24))
                .setUserId(user.getId())
                ;

        MobileSessionController.instance().save(mb);



        String cookie =  UserController.instance().userToCookieString(user, true, null, mb.getSessionKey());
        UserProfile up = UserProfileController.instance().forStallionUserOrNotFound(user.getId());




        return map(
                val(
                        "user", user
                ),
                val("iconBase64", AdminSettings.getIconBase64()),
                val("authcookie", cookie),
                val("passphraseEncryptionSecret", mb.getPassphraseEncryptionSecret()),
                val(
                        "site", new Site().setName(AdminSettings.getSiteName())
                        ),
                val("defaultChannelId", ChannelController.instance().getFirstUserChannel(user.getId())),
                val("userProfile", up)
        );
    }


    @POST
    @Path("/new-mobile-qr-cookie")
    @MinRole(Role.MEMBER)
    public Object mobileLogin() {
        String cookie =  UserController.instance().userToCookieString(Context.getUser(), false, null);
        return map(val("qrCookie", cookie));

    }


    public void checkLoginThrottle(String username) {
        // TODO: check to see if we are throttling based on IP, username, or global

    }

    public Object recordAndThrowInvalidLogin(String username) throws ClientException {
        // TODO:
        throw new ClientException("Invalid username or password.");
    }
}
