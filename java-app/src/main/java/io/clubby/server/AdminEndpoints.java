package io.clubby.server;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.clubby.server.emailers.TestConfigEmailer;
import io.clubby.server.emailers.UserInviteEmailer;
import io.stallion.Context;
import io.stallion.dataAccess.filtering.SortDirection;
import io.stallion.exceptions.ClientException;
import io.stallion.requests.validators.SafeMerger;
import io.stallion.restfulEndpoints.BodyParam;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.MinRole;
import io.stallion.restfulEndpoints.ObjectParam;
import io.stallion.settings.Settings;
import io.stallion.settings.childSections.EmailSettings;
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
        ctx.put("license", or(ClubbyDynamicSettings.getLicense(), new License()));
        ctx.put("emailSettings", or(Settings.instance().getEmail(), new EmailSettings()));
        ctx.put("emailType", ClubbyDynamicSettings.getEmailType());
        ctx.put("settings",
                map(
                        val("siteName", ClubbyDynamicSettings.getSiteName()),
                        val("iconUrl", ClubbyDynamicSettings.getIconUrl()),
                        val("iconImageId", ClubbyDynamicSettings.getIconImageId())
                )
                );
        return ctx;
    }




    @POST
    @Path("/save-license")
    @MinRole(Role.ADMIN)
    public Object saveLicense(
            @BodyParam("key") String key
    ) {
        License license = ClubbyDynamicSettings.getLicense();
        if (license == null) {
            license = new License().setKey(key);
        }
        try {
            HttpResponse<JsonNode> result = Unirest
                    .post(ClubbySettings.getInstance().getHostApiUrl() + "/hosting-api/v1/connect/verify-license")
                    .header("Auth", key)
                    .asJson();
            if (result.getStatus() == 200) {
                license.setKey(key);
                license.setExpiresAt(result.getBody().getObject().getLong("expiresAt"));
                license.setType(License.LicenseType.valueOf(result.getBody().getObject().getString("type")));
                ClubbyDynamicSettings.updateLicenseKey(license);
                return map(val("license", license));
            } else {
                throw new ClientException("Invalid license.");
            }
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }



    @POST
    @Path("/save-settings")
    @MinRole(Role.ADMIN)
    public Object saveSettings(
            @BodyParam("siteName") String siteName,
            @BodyParam(value = "iconImageId", required = false, allowEmpty = true) Long iconImageId
    ) {
        if (!empty(siteName)) {
            ClubbyDynamicSettings.updateSiteName(siteName);
        }
        if (!empty(iconImageId)) {
            ClubbyDynamicSettings.updateIconImageId(iconImageId);
        }
        return true;
    }




    @POST
    @Path("/use-clubby-hoster-for-email")
    @MinRole(Role.ADMIN)
    public Object useClubbyHosterForEmail(

    ) {

        ClubbyDynamicSettings.updateEmailType("clubbyhost");

        return true;
    }



    @POST
    @Path("/save-email-settings")
    @MinRole(Role.ADMIN)
    public Object saveEmailSettings(
            @ObjectParam EmailSettings updatedEmailSettings
    ) {


        ClubbyDynamicSettings.updateEmailSettings(updatedEmailSettings);
        ClubbyDynamicSettings.updateEmailType("custom");
        return true;
    }


    @POST
    @Path("/send-test-email")
    @MinRole(Role.ADMIN)
    public Object sendTestEmail() {
        new TestConfigEmailer(Context.getUser()).sendEmail();
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




    @POST
    @Path("/resend-invite")
    @MinRole(Role.ADMIN)
    public Object resendInvitation(@BodyParam("userId") Long userId) {
        IUser user = UserController.instance().forIdOrNotFound(userId);

        String token = UserController.instance().makeEncryptedToken(user, "invite", user.getResetToken());

        boolean result =new UserInviteEmailer(user, Context.getUser(), token).sendEmail();

        return result;

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