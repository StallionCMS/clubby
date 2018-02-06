package io.clubby.server;

import io.stallion.users.User;


public class UserAndProfile {
    private User user;
    private UserProfile userProfile;


    public User getUser() {
        return user;
    }

    public UserAndProfile setUser(User user) {
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
