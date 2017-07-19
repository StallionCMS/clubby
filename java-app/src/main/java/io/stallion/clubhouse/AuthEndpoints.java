package io.stallion.clubhouse;

import static io.stallion.utils.Literals.*;

import io.stallion.Context;
import io.stallion.contentPublishing.UploadedFile;
import io.stallion.contentPublishing.UploadedFileController;
import io.stallion.requests.Site;
import io.stallion.restfulEndpoints.BodyParam;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.MinRole;
import io.stallion.restfulEndpoints.XSRF;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.users.IUser;
import io.stallion.users.Role;
import io.stallion.users.UserController;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Produces("application/json")
@Path("/clubhouse-api/auth")
public class AuthEndpoints implements EndpointResource {
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
            @BodyParam("registrationToken") String registrationToken,
            @BodyParam("deviceName") String deviceName,
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

        MobileSession mb = new MobileSession()
                .setDeviceName(deviceName)
                .setDeviceOperatingSystem(deviceOS)
                .setIpAddress(Context.getRequest().getActualIp())
                .setLastSignInAt(utcNow())
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
}
