package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.services.Log;


public class UserMessageController extends StandardModelController<UserMessage> {
    public static UserMessageController instance() {
        return (UserMessageController) DataAccessRegistry.instance().get("sch_user_messages");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(UserMessage.class, UserMessageController.class, true);
    }
}
