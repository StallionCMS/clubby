package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.*;

import io.stallion.Context;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.filtering.SortDirection;
import io.stallion.email.ContactableEmailer;
import io.stallion.exceptions.ClientException;
import io.stallion.requests.validators.SafeMerger;
import io.stallion.restfulEndpoints.*;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.users.IUser;
import io.stallion.users.Role;
import io.stallion.users.User;
import io.stallion.users.UserController;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.ws.rs.*;

@Path("/clubhouse-api/users")
@Produces("application/json")
@MinRole(Role.MEMBER)
public class UserEndpoints implements EndpointResource {
    @GET
    @Path("/all-users")
    public Object allUsers() {
        // ChannelUserWrapper
        List<ChannelUserWrapper> users = DB.instance().queryBean(
                ChannelUserWrapper.class,
                "" +
                        " SELECT su.id, su.displayName, su.email, su.username, up.aboutMe, up.webSite, up.publicKeyHex " +
                        " FROM stallion_users AS su" +
                        " INNER JOIN sch_user_profiles as up ON up.userId=su.id "
        );
        Map<String, Object> ctx = map();
        ctx.put("users", users);
        return ctx;
    }

    @GET
    @Path("/clubhouse-settings")
    @MinRole(Role.ADMIN)
    public Object clubhouseSettings() {
        List<IUser> users = UserController.instance().filterChain().includeDeleted().sortBy("username", SortDirection.ASC).all();
        List<UserProfile> profiles = UserProfileController.instance().filterChain().includeDeleted().all();
        Map<Long, UserProfile> profileFromId = map();
        List<UserAndProfile> combos = list();

        for(UserProfile profile: profiles) {
            profileFromId.put(profile.getUserId(), profile);
        }
        for (IUser user: users) {
            UserProfile profile = profileFromId.getOrDefault(user.getId(), null);
            if (profile == null) {
                continue;
            }
            combos.add(
                    new UserAndProfile()
                            .setUser(user)
                            .setUserProfile(profile)
            );
        }

        Map<String, Object> ctx = map();
        ctx.put("userAndProfiles", combos);
        return ctx;
    }

    private static Pattern usernamePattern = Pattern.compile("[a-z][a-z0-9\\_]+");

    @POST
    @Path("/send-invite")
    @MinRole(Role.ADMIN)
    public Object sendInvitation(@ObjectParam User newUser) {
        IUser user = UserController.instance().forEmail(newUser.getEmail());
        if (user != null && !user.getEmailVerified() && user.getApproved()) {
            throw new ClientException("This is already a valid user.");
        } else if (user == null) {
            user = new User();
        }
        new SafeMerger()
                .email("email")
                .nonEmpty("username", "givenName", "familyName")
                .merge(newUser, user);
        if (!usernamePattern.matcher(user.getUsername()).matches()) {
            throw new ClientException("Username is not valid. Can only contain lower-case characters and underscores. Must be at least two characters.");
        }
        user.setResetToken(GeneralUtils.randomTokenBase32(24));
        user.setDisplayName(user.getGivenName() + " " + user.getFamilyName());
        UserController.instance().save(user);

        UserProfile profile = UserProfileController.instance().filter("userId", user.getId()).first();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(user.getId());
        }
        UserProfileController.instance().save(profile);

        String token = UserController.instance().makeEncryptedToken(user, "invite", user.getResetToken());

        new UserInviteEmailer(user, Context.getUser(), token).sendEmail();

        return user;

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
        if (!usernamePattern.matcher(username).matches()) {
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

        return map(val("user", user), val("userProfile", profile));

    }

    public static class UserInviteEmailer extends ContactableEmailer<IUser> {
        private IUser inviter;
        public UserInviteEmailer(IUser user, IUser inviter, String token) {
            super(user);
            this.inviter = inviter;
            put("inviteLink", Settings.instance().getSiteUrl() + "/#accept-invite?userId=" + user.getId() + "&token=" + token);
        }

        @Override
        public boolean isTransactional() {
            return true;
        }

        @Override
        public String getTemplate() {
            return "clubhouse:emails/invite-user.jinja";
        }

        @Override
        public String getSubject() {
            return inviter.getDisplayName() + " invites you to chat!";
        }
    }

    @POST
    @Path("/deactivate")
    @MinRole(Role.ADMIN)
    public Object deactivate(@BodyParam("userId") Long userId) {
        if (Context.getUser().getId().equals(userId)) {
            throw new ClientException("You cannot deactivate yourself.");
        }
        IUser user = UserController.instance().forId(userId);
        user.setApproved(false);
        UserController.instance().save(user);
        return true;
    }

    @POST
    @Path("/activate")
    @MinRole(Role.ADMIN)
    public Object activate(@BodyParam("userId") Long userId) {
        if (Context.getUser().getId().equals(userId)) {
            return false;
        }
        IUser user = UserController.instance().forIdWithDeleted(userId);
        user.setApproved(true);
        user.setDeleted(false);
        UserController.instance().save(user);
        return true;
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
                .optional("aboutMe", "avatarUrl", "webSite", "emailMeWhenMentioned", "notifyWhenMentioned")
                .merge(userAndProfile.getUserProfile(), profile);

        UserController.instance().save(user);
        UserProfileController.instance().save(profile);
        return new UserAndProfile().setUser(user).setUserProfile(profile);
    }

    public static class UserAndProfile {
        private IUser user;
        private UserProfile userProfile;

        @Column
        public IUser getUser() {
            return user;
        }

        public UserAndProfile setUser(IUser user) {
            this.user = user;
            return this;
        }

        @Column
        public UserProfile getUserProfile() {
            return userProfile;
        }

        public UserAndProfile setUserProfile(UserProfile userProfile) {
            this.userProfile = userProfile;
            return this;
        }
    }
}
