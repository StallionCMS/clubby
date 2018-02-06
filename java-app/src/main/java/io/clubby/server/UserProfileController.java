package io.clubby.server;

import java.util.regex.Pattern;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.PartialStash;
import io.stallion.dataAccess.StandardModelController;


public class UserProfileController extends StandardModelController<UserProfile> {

    public static final Pattern USERNAME_PATTERN = Pattern.compile("[a-z][a-z0-9\\_]+");

    public static UserProfileController instance() {
        return (UserProfileController) DataAccessRegistry.instance().get("sch_user_profiles");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(UserProfile.class, UserProfileController.class, PartialStash.class);
    }

    public UserProfile forStallionUser(Long userId) {
        return forUniqueKey("userId", userId);
    }

    public UserProfile forStallionUserOrNotFound(Long userId) {
        return forUniqueKeyOrNotFound("userId", userId);
    }
}
