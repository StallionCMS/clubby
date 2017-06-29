package io.stallion.clubhouse;

import static io.stallion.utils.Literals.*;

import io.stallion.Context;
import io.stallion.requests.Site;
import io.stallion.restfulEndpoints.BodyParam;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.XSRF;
import io.stallion.settings.Settings;
import io.stallion.users.IUser;
import io.stallion.users.UserController;
import io.stallion.utils.GeneralUtils;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

        IUser user = UserController.instance().checkUserLoginValid(username, password);

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
                val("authcookie", cookie),
                val("passphraseEncryptionSecret", mb.getPassphraseEncryptionSecret()),
                val(
                        "site", new Site().setName(Settings.instance().getSiteName())
                        ),
                val("defaultChannelId", ChannelController.instance().getFirstUserChannel(user.getId())),
                val("userProfile", up)
        );
    }
}
