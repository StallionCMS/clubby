package io.clubby.server.emailers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.stallion.utils.Literals.*;

import io.clubby.server.CentralHostApiConnector;
import io.clubby.server.ClubbyDynamicSettings;
import io.stallion.email.ContactableEmailer;
import io.stallion.services.Log;
import io.stallion.services.ShortCodeToken;
import io.stallion.users.IUser;
import io.stallion.utils.GeneralUtils;


public class LoginVerifyEmailer extends ContactableEmailer<IUser> {
        private ShortCodeToken tokenInfo;

        public LoginVerifyEmailer(IUser user, ShortCodeToken tokenInfo) {
                super(user);
                this.tokenInfo = tokenInfo;
                put("tokenInfo", tokenInfo);
                Log.fine("Email {0} with token {1} ", user.getEmail(), tokenInfo.getCode());
        }

        @Override
        public boolean sendEmail() {
                if (ClubbyDynamicSettings.isUseClubbyHostForEmail()) {
                        return CentralHostApiConnector.sendEmailTwoFactor(
                                user.getDisplayName(),
                                user.getEmail(),
                                tokenInfo.getCode(),
                                user.getId()
                        );
                } else {
                        return super.sendEmail();
                }
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
                return "clubby:/emails/login-verify.jinja";
        }

        @Override
        public String getSubject() {
                return "New Device Confirmation";
        }
}
