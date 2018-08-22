package io.clubby.server.emailers;

import io.clubby.server.CentralHostApiConnector;
import io.clubby.server.ClubbyDynamicSettings;
import io.clubby.server.MessageCombo;
import io.clubby.server.UserProfile;
import io.stallion.email.ContactableEmailer;
import io.stallion.users.User;

import java.util.List;


public class MessageEmailer extends ContactableEmailer<User> {
    public MessageEmailer(User user, UserProfile profile, List<MessageCombo> combos) {
        super(user);
        put("profile", profile);
        put("messages", combos);

    }


    @Override
    public boolean sendEmail() {
        if (ClubbyDynamicSettings.isUseClubbyHostForEmail()) {
            return CentralHostApiConnector.sendEmailChatNotification(
                   user,
                    "You have messages"
            );
        } else {
            return super.sendEmail();
        }
    }

    @Override
    public boolean isTransactional() {
        return false;
    }

    @Override
    public String getTemplate() {
        return "clubby:/emails/message-notifications.jinja";
    }

    @Override
    public String getSubject() {
        return "Notifications for messages.";
    }
}
