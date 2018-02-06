package io.clubby.server.emailers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.stallion.utils.Literals.*;

import io.stallion.email.ContactableEmailer;
import io.stallion.services.Log;
import io.stallion.services.ShortCodeToken;
import io.stallion.users.IUser;
import io.stallion.utils.GeneralUtils;


public class LoginVerifyEmailer extends ContactableEmailer<IUser> {

    public LoginVerifyEmailer(IUser user, ShortCodeToken tokenInfo) {
        super(user);
        put("tokenInfo", tokenInfo);
        Log.fine("Email {0} with token {1} ", tokenInfo.getCode());
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
        return "clubhouse:/emails/login-verify.jinja";
    }

    @Override
    public String getSubject() {
        return "New Device Confirmation";
    }
}
