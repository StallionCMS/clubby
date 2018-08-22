package io.clubby.server;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import io.clubby.server.webSockets.WebSocketEventHandler;
import io.stallion.Context;
import io.stallion.contentPublishing.UploadedFile;
import io.stallion.contentPublishing.UploadedFileController;
import io.stallion.dataAccess.SafeMerger;
import io.stallion.dataAccess.db.DB;
import io.stallion.jerseyProviders.BodyParam;
import io.stallion.jerseyProviders.MinRole;
import io.stallion.jerseyProviders.XSRF;
import io.stallion.settings.Settings;
import io.stallion.users.IUser;
import io.stallion.users.Role;
import io.stallion.users.User;
import io.stallion.users.UserController;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

@Path("/clubhouse-api/users")
@Produces("application/json")
@MinRole(Role.MEMBER)
@Provider
@Consumes("application/json")
public class UserEndpoints  {


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
            throw new ClientErrorException("Check google auth", 400);
        }
    }

    @GET
    @Path("/all-users")
    public Object allUsers() {
        // ChannelUserWrapper
        List<ChannelUserWrapper> users = DB.instance().queryBean(
                ChannelUserWrapper.class,
                "" +
                        " SELECT su.id, su.displayName, su.email, su.username, up.`avatarurl`, up.aboutMe, up.webSite, up.publicKeyJwkJson  " +
                        " FROM stallion_users AS su" +
                        " INNER JOIN sch_user_profiles as up ON up.userId=su.id "
        );
        Map<String, Object> ctx = map();
        ctx.put("users", users);
        return ctx;
    }

    @GET
    @Path("/public-profile/{userId}")
    public Object publicProfile(@PathParam("userId") Long userId) {

        List<ChannelUserWrapper> users = DB.instance().queryBean(
                ChannelUserWrapper.class,
                "" +
                        " SELECT su.id, su.displayName, su.email, su.username, up.`avatarUrl`, up.aboutMe, up.webSite, up.publicKeyJwkJson, " +
                        "   up.contactInfo " +
                        " FROM stallion_users AS su " +
                        " INNER JOIN sch_user_profiles as up ON up.userId=su.id " +
                        " WHERE su.id=?",
                userId
        );
        ChannelUserWrapper user;
        if (users.size() == 0) {
            throw new NotFoundException("User not found.");
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
        String expectedToken = UserController.instance().readEncryptedToken(user, "invite", token, 60 * 24 *2);
        if (!user.getResetToken().equals(expectedToken)) {
            throw new ClientErrorException("Invalid invite token.", 403);
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
            @BodyParam("password4chars") String password4chars,
            @BodyParam("passwordSalt") String passwordSalt,
            @BodyParam("givenName") String givenName,
            @BodyParam("familyName") String familyName,
            @BodyParam("publicKeyJwkJson") String publicKeyJwkJson,
            @BodyParam("privateKeyJwkEncryptedHex") String privateKeyJwkEncryptedHex,
            @BodyParam("privateKeyVectorHex") String privateKeyVectorHex,
            @BodyParam("rememberThisDevice") Boolean rememberThisDevice,
            @BodyParam(value = "webSite", allowEmpty = true) String webSite,
            @BodyParam(value = "aboutMe", allowEmpty = true) String aboutMe,
            @BodyParam("userId") Long userId
            ) {
        IUser user = UserController.instance().forIdOrNotFound(userId);
        String expectedToken = UserController.instance().readEncryptedToken(user, "invite", token, 60);
        if (!user.getResetToken().equals(expectedToken)) {
            throw new ClientErrorException("Invalid invite token.", 403);
        }
        if (!UserProfileController.USERNAME_PATTERN.matcher(username).matches()) {
            throw new ClientErrorException("Username is not valid. Can only contain lower-case characters and underscores. Must be at least two characters.", 400);
        }

        user.setApproved(true);
        user.setEmailVerified(true);
        user.setResetToken(GeneralUtils.randomTokenBase32(24));
        user.setUsername(username);
        user.setGivenName(givenName);
        user.setRole(Role.MEMBER);
        user.setBcryptedPassword(GeneralUtils.randomToken(32));
        user.setFamilyName(familyName);
        user.setDisplayName(givenName + " " + familyName);


        UserController.instance().save(user);
        UserProfile profile = UserProfileController.instance().filter("userId", user.getId()).first();
        if (profile == null) {
            profile = new UserProfile()
                    .setUserId(user.getId())
                    ;
        }
        profile.setPasswordSalt(passwordSalt);
        profile.setPasswordFourCharactersHashed(password4chars);
        profile.setPrivateKeyJwkEncryptedHex(privateKeyJwkEncryptedHex);
        profile.setPublicKeyJwkJson(publicKeyJwkJson);
        profile.setPrivateKeyVectorHex(privateKeyVectorHex);

        UserProfileController.instance().save(profile);

        UserController.instance().addSessionCookieForUser(user, true);

        String rememberToken = null;
        if (rememberThisDevice) {
            rememberToken = new AuthEndpoints().makeRememberDeviceCookie(user);
        }


        WebSocketEventHandler.notifyNewMember(
                new ChannelUserWrapper()
                        .fromUserAndProfile(user, profile, true)
        );

        return map(
                val("rememberDeviceToken", rememberToken),
                val("user", user),
                val("userProfile", profile),
                val("defaultChannelId", ChannelController.instance().getFirstUserChannel(user.getId()))
        );

    }


    @POST
    @Path("/create-first-user")
    @MinRole(Role.ANON)
    public Object createFirstUser(
            @BodyParam("secretKey") String secretKey,
            @BodyParam("username") String username,
            @BodyParam("email") String email,
            @BodyParam("givenName") String givenName,
            @BodyParam("familyName") String familyName,
            @BodyParam("password4chars") String password4chars,
            @BodyParam("passwordSalt") String passwordSalt,
            @BodyParam("rememberThisDevice") Boolean rememberThisDevice,
            @BodyParam("publicKeyJwkJson") String publicKeyJwkJson,
            @BodyParam("privateKeyVectorHex") String privateKeyVectorHex,
            @BodyParam("privateKeyJwkEncryptedHex") String privateKeyJwkEncryptedHex,
            @BodyParam(value = "webSite", allowEmpty = true) String webSite,
            @BodyParam(value = "contactInfo", allowEmpty = true) String contactInfo,
            @BodyParam(value = "aboutMe", allowEmpty = true) String aboutMe
    ) {
        int userCount = UserController.instance().filterChain().count();
        if (userCount > 0) {
            throw new ClientErrorException("This site has already been initialized. You must log in with the admin account.", 409);
        }
        if (!Settings.instance().getHealthCheckSecret().equals(secretKey)) {
            throw new ClientErrorException("The secret key you entered is not correct.", 403);
        }

        if (!UserProfileController.USERNAME_PATTERN.matcher(username).matches()) {
            throw new ClientErrorException("Username is not valid. Can only contain lower-case characters and underscores. Must be at least two characters.", 400);
        }


        User user = new User();
        user.setEmail(email);
        user.setApproved(true);
        user.setEmailVerified(true);
        user.setResetToken(GeneralUtils.randomTokenBase32(24));
        user.setUsername(username);
        user.setGivenName(givenName);
        user.setRole(Role.ADMIN);
        user.setFamilyName(familyName);
        user.setDisplayName(givenName + " " + familyName);
        user.setBcryptedPassword(GeneralUtils.randomTokenBase32(24));


        UserController.instance().save(user);

        UserProfile profile = new UserProfile()
                    .setUserId(user.getId())
            ;
        profile.setContactInfo(contactInfo);
        profile.setAboutMe(aboutMe);
        profile.setPrivateKeyJwkEncryptedHex(privateKeyJwkEncryptedHex);
        profile.setPublicKeyJwkJson(publicKeyJwkJson);
        profile.setPrivateKeyVectorHex(privateKeyVectorHex);
        profile.setPasswordSalt(passwordSalt);
        profile.setPasswordFourCharactersHashed(password4chars);
        UserProfileController.instance().save(profile);

        UserController.instance().addSessionCookieForUser(user, true);



        Channel channel1 = new Channel()
                .setName("General")
                .setChannelType(ChannelType.CHANNEL)
                .setDefaultForNewUsers(true)
                .setEncrypted(false)
                .setHidden(false)
                .setInviteOnly(false)
                ;
        ChannelController.instance().save(channel1);


        Channel channel2 = new Channel()
                .setName("Random")
                .setChannelType(ChannelType.CHANNEL)
                .setDefaultForNewUsers(true)
                .setEncrypted(false)
                .setHidden(false)
                .setInviteOnly(false)
                ;
        ChannelController.instance().save(channel2);



        Channel channel3 = new Channel()
                .setName("Discussions")
                .setChannelType(ChannelType.FORUM)
                .setDefaultForNewUsers(true)
                .setEncrypted(false)
                .setHidden(false)
                .setInviteOnly(false)
                ;
        ChannelController.instance().save(channel3);

        for(Channel c: list(channel1, channel2, channel3)) {
            ChannelMember cm = new ChannelMember()
                    .setCanPost(true)
                    .setChannelId(c.getId())
                    .setFavorite(false)
                    .setHidden(false)
                    .setJoinedAt(utcNow())
                    .setOwner(true)
                    .setUserId(user.getId())
                    ;
            ChannelMemberController.instance().save(cm);
        }

        String rememberToken = null;
        if (rememberThisDevice) {
            rememberToken = new AuthEndpoints().makeRememberDeviceCookie(user);
        }

        return map(
                val("rememberDeviceToken", rememberToken),
                val("user", user),
                val("userProfile", profile),
                val("defaultChannelId", ChannelController.instance().getFirstUserChannel(user.getId()))
        );

    }

    @POST
    @Path("/update-current-user")
    public Object updateCurrentUser(UserAndProfile userAndProfile) {

        if (!userAndProfile.getUser().getId().equals(Context.getUser().getId())) {
             throw new ClientErrorException("You do not have authorization to update this user.", 403);
        }
        User user = (User)UserController.instance().forIdOrNotFound(Context.getUser().getId());
        UserProfile profile = UserProfileController.instance().forStallionUserOrNotFound(user.getId());
        new SafeMerger()
                .optional("givenName", "familyName", "username")
                .merge(userAndProfile.getUser(), user);
        user.setDisplayName(StringUtils.strip(user.getGivenName() + " " + user.getFamilyName(), " "));
        new SafeMerger()
                .optional("aboutMe", "contactInfo", "avatarUrl", "avatarFileId", "webSite", "emailMeWhenMentioned", "notifyWhenMentioned",
                        "mobileNotifyPreference", "desktopNotifyPreference")
                .merge(userAndProfile.getUserProfile(), profile);

        UserController.instance().save(user);
        UserProfileController.instance().save(profile);

        if (!empty(profile.getAvatarFileId())) {
            UploadedFile file = (UploadedFile)UploadedFileController.instance().forId(profile.getAvatarFileId());
            if (file != null) {
                if (!file.isPubliclyViewable()) {
                    file.setPubliclyViewable(true);
                    UploadedFileController.instance().save(file);
                }
            }
        }

        return new UserAndProfile().setUser(user).setUserProfile(profile);
    }

}
