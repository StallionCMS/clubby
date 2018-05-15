package io.clubby.server;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.clubby.server.emailers.MessageEmailer;
import io.stallion.dataAccess.db.DB;
import io.stallion.jobs.Job;
import io.stallion.jobs.JobComplete;
import io.stallion.jobs.Schedule;
import io.stallion.services.Log;
import io.stallion.users.User;
import io.stallion.users.UserController;
import io.stallion.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;


public class SendNotificationsJob extends JobComplete {

    @Override
    public String getName() {
        return "send-notifications";
    }

    @Override
    public int getAlertThresholdMinutes() {
        return 30;
    }

    @Override
    public Schedule getSchedule() {
        return new Schedule()
                .everyMonth()
                .everyDay()
                .everyHour()
                .everyMinute();
    }

    @Override
    public void execute() {
        Log.info("Run SendNotificationJob");
        findAndSendMobileNotifications();
        findAndSendEmails();

        /*
        .setJobClass(EmailMessagesJob.class)
                        .setName("email-messages")
                        .setAlertThresholdMinutes(90)
                        .setSchedule(

                        )

         */
    }

    public void findAndSendMobileNotifications() {
        ZonedDateTime since = DateUtils.utcNow().minusMinutes(1);
        Map<Long, List<MessageCombo>> userMessageByUserId = map();
        List<MessageCombo> combos = MessageController.instance().loadUnseenMessagesForMobileNotify(since);
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

            if (!UserNotifyPreference.NONE.equals(profile.getMobileNotifyPreference())) {
                //MessageController.instance().notifyOfNewMessage();
                //String title = "Message(s) from ";
                //for (MessageCombo mc: entry.getValue()) {
                //    title += mc.getFromUsername() + ", ";
                //}
                //title = StringUtils.strip(StringUtils.strip(title, " "), ",");
                MessageController.instance().sendMobileNotificationOfMessage(entry.getValue().get(0));

            }


            // Make the messages as notified
            List umIds = apply(entry.getValue(), combo-> combo.getUserMessageId());
            DB.SqlAndParams sqlAndParams = DB.instance().toInQueryParams(umIds);
            DB.instance().execute(
                    "UPDATE sch_user_messages SET mobileNotifyPending=0 WHERE id IN " + sqlAndParams.getSql(),
                    sqlAndParams.getParams()
            );

        }
    }

    public void findAndSendEmails() {

        ZonedDateTime since = DateUtils.utcNow().minusMinutes(10);
        Map<Long, List<MessageCombo>> userMessageByUserId = map();
        List<MessageCombo> combos = MessageController.instance().loadUnseenMessagesForEmailNotify(since);
        Log.info("Found messageCombos {0}", combos);
        for(MessageCombo combo: combos) {
            if (!userMessageByUserId.containsKey(combo.getToUserId())) {
                userMessageByUserId.put(combo.getToUserId(), list());
            }
            userMessageByUserId.get(combo.getToUserId()).add(combo);
        }

        for(Map.Entry<Long, List<MessageCombo>> entry: userMessageByUserId.entrySet()) {
            User user = (User)UserController.instance().forId(entry.getKey());
            Log.info("Have message combos for user {0} userId: {1}: message count: {2}", user.getUsername(), user.getId(), entry.getValue().size());
            if (user == null) {
                continue;
            }
            if (entry.getValue().size() == 0){
                continue;
            }
            UserProfile profile = UserProfileController.instance().forStallionUserOrNotFound(user.getId());

            if (profile.isEmailMeWhenMentioned()) {
                // Actually send the email
                Log.info("Send notify email to {0} email {1} userId: ", user.getUsername(), user.getEmail(), user.getId());
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

}
