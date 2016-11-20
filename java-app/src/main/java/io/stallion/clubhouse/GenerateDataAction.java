package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.boot.AppContextLoader;
import io.stallion.boot.CommandOptionsBase;
import io.stallion.boot.StallionRunAction;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.tools.SampleDataGenerator;
import io.stallion.users.Role;
import io.stallion.users.User;
import io.stallion.users.UserController;
import io.stallion.utils.GeneralUtils;

import javax.persistence.Column;


public class GenerateDataAction extends SampleDataGenerator implements StallionRunAction<CommandOptionsBase> {
    @Override
    public String getActionName() {
        return "generate-data";
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public void loadApp(CommandOptionsBase options) {
        AppContextLoader.loadCompletely(options);
    }

    @Override
    public void execute(CommandOptionsBase options) throws Exception {
        generate();
    }


    @Override
    public Long getBaseId() {
        return 10000L;
    }

    @Override
    public void generate() {
        createUsers();
    }

    private long GEORGE_ID = 10000;
    private long PAUL_ID = 10001;
    private long JOHN_ID = 10002;


    public void createUsers() {
        List<Combo> combos = list(
                new Combo()
                        .setProfile(
                                new UserProfile()
                        ).setUser(
                            new User()
                                    .setEmail("georgewashington@stallion.io")
                                    .setGivenName("George")
                                    .setEncryptionSecret("41XMF2YvTWvs")
                                    .setFamilyName("Washington")
                                    .setId(GEORGE_ID)

                        )
                ,
                new Combo()
                        .setProfile(
                                new UserProfile()
                        ).setUser(
                            new User()
                                    .setEmail("paulrevere@stallion.io")
                                    .setEncryptionSecret("iO5D9pXLms0k")
                                    .setGivenName("Paul")
                                    .setFamilyName("Revere")
                                    .setId(PAUL_ID)
                ),
                new Combo()
                        .setProfile(
                                new UserProfile()
                        ).setUser(
                           new User()
                                   .setEncryptionSecret("iO5D9pXLms0k")
                                   .setEmail("johnadams@stallion.io")
                                   .setGivenName("John")
                                   .setFamilyName("Adams")
                                   .setId(JOHN_ID)
                )
        );
        for(Combo c: combos) {
            User user = c.getUser();
            //user.setEmail(user.getEmail());
            user.setApproved(true);
            user.setEmailVerified(true);
            user.setUsername(user.getEmail());
            user.setRole(Role.MEMBER);

            user.setBcryptedPassword("$2a$10$V2B71X0xH/tS0udKoBeIzelx6AjTlVwxn9.0n/bULXSFdhL5ATo7K");
            user.setEncryptionSecret(GeneralUtils.md5Hash(user.getEmail()));

            UserController.instance().save(c.getUser());
            c.getProfile().setId(user.getId() + 200);
            UserProfileController.instance().save(c.getProfile());
        }
    }



    public class Combo {
        private User user;
        private UserProfile profile;


        public User getUser() {
            return user;
        }

        public Combo setUser(User user) {
            this.user = user;
            return this;
        }


        public UserProfile getProfile() {
            return profile;
        }

        public Combo setProfile(UserProfile profile) {
            this.profile = profile;
            return this;
        }
    }


}
