package io.clubby.server.emailers;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.clubby.server.CentralHostApiConnector;
import io.clubby.server.ClubbyDynamicSettings;
import io.stallion.email.ContactableEmailer;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.users.IUser;
import io.stallion.utils.GeneralUtils;


public class UserInviteEmailer extends ContactableEmailer<IUser> {
    private IUser inviter;
    private String inviteCode;

    public UserInviteEmailer(IUser user, IUser inviter, String token) {
        super(user);
        this.inviter = inviter;
        this.inviteCode = token;
        put("inviteLink", Settings.instance().getSiteUrl() + "/#accept-invite?userId=" + user.getId() + "&token=" + token);
    }


    @Override
    public boolean sendEmail() {
        if (ClubbyDynamicSettings.isUseClubbyHostForEmail()) {
            return CentralHostApiConnector.sendInviteEmail(
                    user.getDisplayName(),
                    user.getEmail(),
                    user.getId(),
                    inviter.getDisplayName(),
                    inviteCode
            );
        } else {
            return super.sendEmail();
        }
    }

    private static final DateTimeFormatter MINUTE_FORMAT  =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    @Override
    public String getUniqueKey() {
        return "invite-email--" + GeneralUtils.slugify(this.user.getEmail()) + "--" +MINUTE_FORMAT.format(utcNow());
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public String getTemplate() {
        return "clubby:emails/invite-user.jinja";
    }

    @Override
    public String getSubject() {
        return inviter.getDisplayName() + " invites you to chat!";
    }
}
