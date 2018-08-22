package io.clubby.server.emailers;

import io.stallion.email.ContactableEmailer;
import io.stallion.users.IUser;

import java.util.UUID;


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

