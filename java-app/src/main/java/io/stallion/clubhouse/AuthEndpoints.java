package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.requests.Site;
import io.stallion.restfulEndpoints.BodyParam;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.XSRF;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.users.IUser;
import io.stallion.users.UserController;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
    public Object mobileLogin(@BodyParam("username") String username, @BodyParam("password") String password) {
        if (username.contains("@")) {
            IUser fromEmail = UserController.instance().forEmail(username);
            if (fromEmail != null) {
                username = fromEmail.getUsername();
            }
        }
        IUser user = UserController.instance().checkUserLoginValid(username, password);
        String cookie =  UserController.instance().userToCookieString(user, true, null);
        UserProfile up = UserProfileController.instance().forStallionUserOrNotFound(user.getId());
        return map(
                val(
                        "user", user
                ),
                val("authcookie", cookie),
                val(
                        "site", new Site().setName(Settings.instance().getSiteName())
                        ),
                val("defaultChannelId", ChannelController.instance().getFirstUserChannel(user.getId())),
                val("userProfile", up)
        );
    }
}
