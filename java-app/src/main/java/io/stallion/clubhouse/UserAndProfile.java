package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.services.Log;
import io.stallion.users.IUser;


public class UserAndProfile {
    private IUser user;
    private UserProfile userProfile;


    public IUser getUser() {
        return user;
    }

    public UserAndProfile setUser(IUser user) {
        this.user = user;
        return this;
    }


    public UserProfile getUserProfile() {
        return userProfile;
    }

    public UserAndProfile setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        return this;
    }
}
