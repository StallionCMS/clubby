package io.clubby.server;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.clubby.server.emailers.TestConfigEmailer;
import io.clubby.server.emailers.UserInviteEmailer;
import io.clubby.server.webSockets.WebSocketEventHandler;
import io.stallion.Context;
import io.stallion.dataAccess.SafeMerger;
import io.stallion.dataAccess.filtering.SortDirection;
import io.stallion.http.BodyParam;
import io.stallion.http.MinRole;
import io.stallion.settings.Settings;
import io.stallion.settings.childSections.EmailSettings;
import io.stallion.users.IUser;
import io.stallion.users.Role;
import io.stallion.users.User;
import io.stallion.users.UserController;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import org.eclipse.jetty.websocket.common.WebSocketSession;

import javax.websocket.Session;
import javax.ws.rs.*;
import javax.ws.rs.ext.Provider;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;


@Path("/clubhouse-api/admin")
@Consumes("application/json")
@Produces("application/json")
@MinRole(Role.ADMIN)
@Provider
public class AdminEndpoints {

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
                throw new ClientErrorException("Invalid license.", 403);
            }
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }


    @GET
    @Path("/session-explorer")
    @MinRole(Role.ADMIN)
    public Object sessionExplorer(@QueryParam("userId") Long userId) {
        Map ctx = map();

        List<Map> sessions = list();
        ctx.put("sessions", sessions);

        Map<String, Session> sessionMap = WebSocketEventHandler.getSessionsByUserId().getOrDefault(userId, Collections.EMPTY_MAP);
        for(Map.Entry<String, Session> entry: sessionMap.entrySet()) {
            WebSocketSession sess = (WebSocketSession)entry.getValue();
            sessions.add(
                    map(
                            val("URI", entry.getValue().getRequestURI().toString()),
                            val("real-ip", ((WebSocketSession) entry.getValue()).getUpgradeRequest().getHeader("x-Real-Ip")),
                            val("User-agent", ((WebSocketSession) entry.getValue()).getUpgradeRequest().getHeader("User-agent")),
                            val("userState", entry.getValue().getUserProperties().get("userState"))
                    )
            );
        }


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
            EmailSettings updatedEmailSettings
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
    public Object sendInvitation(User newUser) {
        IUser user = UserController.instance().forEmail(newUser.getEmail());
        if (user != null && !user.getEmailVerified() && user.getApproved()) {
            throw new ClientErrorException("This is already a valid user.", 410);
        } else if (user == null) {
            user = new User();
        }
        new SafeMerger()
                .email("email")
                .nonEmpty("username", "givenName", "familyName")
                .merge(newUser, user);
        if (!UserProfileController.USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
            throw new ClientErrorException("Username is not valid. Can only contain lower-case characters and underscores. Must be at least two characters.", 400);
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
            throw new ClientErrorException("You cannot deactivate yourself.", 410);
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
