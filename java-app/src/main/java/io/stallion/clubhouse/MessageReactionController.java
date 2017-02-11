package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.services.Log;


public class MessageReactionController extends StandardModelController<MessageReaction> {
    public static MessageReactionController instance() {
        return (MessageReactionController) DataAccessRegistry.instance().get("message_reactions");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(MessageReaction.class, MessageReactionController.class, true);
    }
}
