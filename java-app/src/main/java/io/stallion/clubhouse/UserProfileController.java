package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.PartialStash;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.services.Log;


public class UserProfileController extends StandardModelController<UserProfile> {
    public static UserProfileController instance() {
        return (UserProfileController) DataAccessRegistry.instance().get("sch_user_profiles");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(UserProfile.class, UserProfileController.class, PartialStash.class);
    }

    public UserProfile forStallionUserOrNotFound(Long userId) {
        return forUniqueKeyOrNotFound("userId", userId);
    }
}
