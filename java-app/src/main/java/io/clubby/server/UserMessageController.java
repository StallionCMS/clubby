package io.clubby.server;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;


public class UserMessageController extends StandardModelController<UserMessage> {
    public static UserMessageController instance() {
        return (UserMessageController) DataAccessRegistry.instance().get("sch_user_messages");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(UserMessage.class, UserMessageController.class, true);
    }
}
