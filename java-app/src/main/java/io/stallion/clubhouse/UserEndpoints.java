package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.Context;
import io.stallion.dataAccess.db.DB;
import io.stallion.exceptions.ClientException;
import io.stallion.requests.validators.SafeMerger;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.MinRole;
import io.stallion.restfulEndpoints.ObjectParam;
import io.stallion.services.Log;
import io.stallion.users.IUser;
import io.stallion.users.Role;
import io.stallion.users.User;
import io.stallion.users.UserController;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
        private User user;
        private UserProfile userProfile;

        @Column
        public User getUser() {
            return user;
        }

        public UserAndProfile setUser(User user) {
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
