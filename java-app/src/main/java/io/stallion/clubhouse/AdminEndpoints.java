package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.*;

import io.stallion.Context;
import io.stallion.dataAccess.filtering.SortDirection;
import io.stallion.email.ContactableEmailer;
import io.stallion.exceptions.ClientException;
import io.stallion.requests.validators.SafeMerger;
import io.stallion.restfulEndpoints.BodyParam;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.MinRole;
import io.stallion.restfulEndpoints.ObjectParam;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.users.IUser;
import io.stallion.users.Role;
import io.stallion.users.User;
import io.stallion.users.UserController;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


@Produces("application/json")
@Path("/clubhouse-api/admin")
@MinRole(Role.ADMIN)
public class AdminEndpoints implements EndpointResource {

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
                            .setUser((User)user)
                            .setUserProfile(profile)
            );
        }

        Map<String, Object> ctx = map();
        ctx.put("userAndProfiles", combos);
        ctx.put("settings",
                map(
                        val("siteName", AdminSettings.getSiteName()),
                        val("iconUrl", AdminSettings.getIconUrl()),
                        val("iconImageId", AdminSettings.getIconImageId())
                )
                );
        return ctx;
    }

    @POST
    @Path("/save-settings")
    @MinRole(Role.ADMIN)
    public Object saveSettings(
            @BodyParam("siteName") String siteName,
            @BodyParam(value = "iconImageId", required = false, allowEmpty = true) Long iconImageId
    ) {
        if (!empty(siteName)) {
            AdminSettings.setSiteName(siteName);
        }
        if (!empty(iconImageId)) {
            AdminSettings.setIconImageId(iconImageId);
        }
        return true;
    }




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
        if (!UserProfileController.USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
            throw new ClientException("Username is not valid. Can only contain lower-case characters and underscores. Must be at least two characters.");
        }
        user.setResetToken(GeneralUtils.randomTokenBase32(24));
        user.setDisplayName(user.getGivenName() + " " + user.getFamilyName());
        UserController.instance().save(user);

        List<Channel> channels = ChannelController.instance().filter("defaultForNewUsers", true).all();
        for (Channel channel: channels) {
            ChannelMember cm = ChannelMemberController.instance().forUserChannel(user.getId(), channel.getId());
            if (cm == null) {
                cm = new ChannelMember()
                        .setUserId(user.getId())
                        .setChannelId(channel.getId())
                        .setCanPost(true)
                        .setJoinedAt(DateUtils.utcNow())
                        .setHidden(false)
                        .setOwner(false);
                ChannelMemberController.instance().save(cm);
            }
        }

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


}
