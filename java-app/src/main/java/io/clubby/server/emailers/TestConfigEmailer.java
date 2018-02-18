package io.clubby.server.emailers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.stallion.utils.Literals.*;

import io.clubby.server.ClubbyDynamicSettings;
import io.stallion.email.ContactableEmailer;
import io.stallion.services.Log;
import io.stallion.services.ShortCodeToken;
import io.stallion.users.IUser;


public class TestConfigEmailer extends ContactableEmailer<IUser> {

    public TestConfigEmailer(IUser user) {
        super(user);
    }



    @Override
    public boolean isTransactional() {
        return true;
    }


    @Override
    public String getUniqueKey() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String getTemplate() {
        return "clubby:/emails/test-email-configuration.jinja";
    }

    @Override
    public String getSubject() {
        return "Confirming Clubby Email Configuration";
    }
}

