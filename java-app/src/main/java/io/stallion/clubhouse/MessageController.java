package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.services.Log;


public class MessageController extends StandardModelController<Message> {
    public static MessageController instance() {
        return (MessageController) DataAccessRegistry.instance().get("sch_messages");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(Message.class, MessageController.class, false);
    }
}
