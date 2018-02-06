package io.clubby.server;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.db.DB;
import io.stallion.email.ContactableEmailer;
import io.stallion.jobs.Job;
import io.stallion.services.Log;
import io.stallion.users.User;
import io.stallion.users.UserController;
import io.stallion.utils.DateUtils;


public class EmailMessagesJob implements Job {
    @Override
    public void execute() {
        Log.info("Run EmailMessagesJob");

        ZonedDateTime since = DateUtils.utcNow();//.minusMinutes(10);
        Map<Long, List<MessageCombo>> userMessageByUserId = map();
        List<MessageCombo> combos = MessageController.instance().loadUnseenMessages(since);
        Log.info("Found messageCombos {0}", combos);
        for(MessageCombo combo: combos) {
            if (!userMessageByUserId.containsKey(combo.getFromUserId())) {
                userMessageByUserId.put(combo.getToUserId(), list());
            }
            userMessageByUserId.get(combo.getToUserId()).add(combo);
        }

        for(Map.Entry<Long, List<MessageCombo>> entry: userMessageByUserId.entrySet()) {
            User user = (User)UserController.instance().forId(entry.getKey());
            Log.info("Have message combos for user {0} {1}", user.getUsername(), entry.getValue().size());
            if (user == null) {
                continue;
            }
            if (entry.getValue().size() == 0){
                continue;
            }
            UserProfile profile = UserProfileController.instance().forStallionUserOrNotFound(user.getId());

            if (profile.isEmailMeWhenMentioned()) {
                // Actually send the email
                Log.info("Send notify email to {0}", user.getUsername());
                new MessageEmailer(user, profile, entry.getValue()).sendEmail();
            }


            // Make the messages as notified
            List umIds = apply(entry.getValue(), combo-> combo.getUserMessageId());
            DB.SqlAndParams sqlAndParams = DB.instance().toInQueryParams(umIds);
            DB.instance().execute(
                    "UPDATE sch_user_messages SET emailNotifySent=1 WHERE id IN " + sqlAndParams.getSql(),
                    sqlAndParams.getParams()
            );

        }


    }

    public static class MessageEmailer extends ContactableEmailer<User> {
        public MessageEmailer(User user, UserProfile profile, List<MessageCombo> combos) {
            super(user);
            put("profile", profile);
            put("messages", combos);
        }

        @Override
        public boolean isTransactional() {
            return false;
        }

        @Override
        public String getTemplate() {
            return "clubhouse:/emails/message-notifications.jinja";
        }

        @Override
        public String getSubject() {
            return "Notifications for messages.";
        }
    }

}
