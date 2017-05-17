package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import io.stallion.Context;
import io.stallion.dataAccess.db.DB;
import io.stallion.exceptions.*;
import io.stallion.requests.validators.SafeMerger;
import io.stallion.restfulEndpoints.*;
import io.stallion.settings.Settings;
import io.stallion.users.IUser;
import io.stallion.users.Role;
import io.stallion.users.User;
import io.stallion.users.UserController;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;

@Path("/clubhouse-api/users")
@Produces("application/json")
@MinRole(Role.MEMBER)
public class UserEndpoints implements EndpointResource {


    @POST
    @Path("/generate-credentials")
    public Object generateNewCredentials() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        final GoogleAuthenticatorKey key = gAuth.createCredentials();

        return map(
                val("key", key.getKey())
        );
    }

    @POST
    @Path("/check-google-auth")
    public Object checkGoogleAuth(@PathParam("secretKey") String secretKey, @PathParam("password") Integer password) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean isCodeValid = gAuth.authorize(secretKey, password);
        if (isCodeValid) {
            return true;
        } else {
            throw new ClientException("Check google auth", 400);
        }
    }

    @GET
    @Path("/all-users")
    public Object allUsers() {
        // ChannelUserWrapper
        List<ChannelUserWrapper> users = DB.instance().queryBean(
                ChannelUserWrapper.class,
                "" +
                        " SELECT su.id, su.displayName, su.email, su.username, up.`avatarurl`, up.aboutMe, up.webSite, up.publicKeyHex  " +
                        " FROM stallion_users AS su" +
                        " INNER JOIN sch_user_profiles as up ON up.userId=su.id "
        );
        Map<String, Object> ctx = map();
        ctx.put("users", users);
        return ctx;
    }

    @GET
    @Path("/public-profile/:userId")
    public Object publicProfile(@PathParam("userId") Long userId) {

        List<ChannelUserWrapper> users = DB.instance().queryBean(
                ChannelUserWrapper.class,
                "" +
                        " SELECT su.id, su.displayName, su.email, su.username, up.`avatarUrl`, up.aboutMe, up.webSite, up.publicKeyHex, " +
                        "   up.contactInfo " +
                        " FROM stallion_users AS su " +
                        " INNER JOIN sch_user_profiles as up ON up.userId=su.id " +
                        " WHERE su.id=?",
                userId
        );
        ChannelUserWrapper user;
        if (users.size() == 0) {
            throw new io.stallion.exceptions.NotFoundException("User not found.");
        } else {
            user = users.get(0);
        }
        Map<String, Object> ctx = map();
        ctx.put("user", user);
        return ctx;
    }

    @POST
    @Path("/external-login")
    @MinRole(Role.ANON)
    @XSRF(false)
    public Object externalLogin(@BodyParam("username") String username, @BodyParam("password") String password) {
        IUser user = UserController.instance().checkUserLoginValid(username, password);
        return map(
                val("userId", user.getId()),
                val("name", Settings.instance().getSiteName()),
                val("icon", "")
        );
    }

    @GET
    @Path("/accept-invite-context")
    @MinRole(Role.ANON)
    public Object acceptInvitationContext(@QueryParam("token") String token, @QueryParam("userId") Long userId) {
        IUser user = UserController.instance().forIdOrNotFound(userId);
        String expectedToken = UserController.instance().readEncryptedToken(user, "invite", token, 60);
        if (!user.getResetToken().equals(expectedToken)) {
            throw new ClientException("Invalid invite token.");
        }

        Map ctx = map();
        ctx.put("givenName", user.getGivenName());
        ctx.put("familyName", user.getFamilyName());
        ctx.put("email", user.getEmail());
        ctx.put("username", user.getUsername());

        return ctx;
    }

    @POST
    @Path("/accept-invite")
    @MinRole(Role.ANON)
    public Object acceptInvitation(
            @BodyParam("token") String token,
            @BodyParam("username") String username,
            @BodyParam("password") String password,
            @BodyParam("givenName") String givenName,
            @BodyParam("familyName") String familyName,
            @BodyParam("publicKeyHex") String publicKeyHex,
            @BodyParam("encryptedPrivateKeyHex") String encryptedPrivateKeyHex,
            @BodyParam("encryptedPrivateKeyInitializationVectorHex") String encryptedPrivateKeyInitializationVectorHex,
            @BodyParam(value = "webSite", allowEmpty = true) String webSite,
            @BodyParam(value = "aboutMe", allowEmpty = true) String aboutMe,
            @BodyParam("userId") Long userId
            ) {
        IUser user = UserController.instance().forIdOrNotFound(userId);
        String expectedToken = UserController.instance().readEncryptedToken(user, "invite", token, 60);
        if (!user.getResetToken().equals(expectedToken)) {
            throw new ClientException("Invalid invite token.");
        }
        if (!UserProfileController.USERNAME_PATTERN.matcher(username).matches()) {
            throw new ClientException("Username is not valid. Can only contain lower-case characters and underscores. Must be at least two characters.");
        }

        user.setApproved(true);
        user.setEmailVerified(true);
        user.setResetToken(GeneralUtils.randomTokenBase32(24));
        user.setUsername(username);
        user.setGivenName(givenName);
        user.setRole(Role.MEMBER);
        user.setFamilyName(familyName);
        user.setDisplayName(givenName + " " + familyName);

        UserController.instance().hydratePassword(user, password, password);

        UserController.instance().save(user);
        UserProfile profile = UserProfileController.instance().filter("userId", user.getId()).first();
        if (profile == null) {
            profile = new UserProfile()
                    .setUserId(user.getId())
                    ;
        }
        profile.setEncryptedPrivateKeyHex(encryptedPrivateKeyHex);
        profile.setPublicKeyHex(publicKeyHex);
        profile.setEncryptedPrivateKeyInitializationVectorHex(encryptedPrivateKeyInitializationVectorHex);
        UserProfileController.instance().save(profile);

        UserController.instance().addSessionCookieForUser(user, true);

        return map(
                val("user", user),
                val("userProfile", profile),
                val("defaultChannelId", ChannelController.instance().getFirstUserChannel(user.getId()))
        );

    }

    @POST
    @Path("/update-current-user")
    public Object updateCurrentUser(@ObjectParam UserAndProfile userAndProfile) {

        if (!userAndProfile.getUser().getId().equals(Context.getUser().getId())) {
             throw new ClientException("You do not have authorization to update this user.");
        }
        User user = (User)UserController.instance().forIdOrNotFound(Context.getUser().getId());
        UserProfile profile = UserProfileController.instance().forStallionUserOrNotFound(user.getId());
        new SafeMerger()
                .optional("givenName", "familyName", "username")
                .merge(userAndProfile.getUser(), user);
        user.setDisplayName(StringUtils.strip(user.getGivenName() + " " + user.getFamilyName(), " "));
        new SafeMerger()
                .optional("aboutMe", "contactInfo", "avatarUrl", "webSite", "emailMeWhenMentioned", "notifyWhenMentioned")
                .merge(userAndProfile.getUserProfile(), profile);

        UserController.instance().save(user);
        UserProfileController.instance().save(profile);
        return new UserAndProfile().setUser(user).setUserProfile(profile);
    }

}
