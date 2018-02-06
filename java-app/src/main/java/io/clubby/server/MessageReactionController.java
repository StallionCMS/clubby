package io.clubby.server;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;


public class MessageReactionController extends StandardModelController<MessageReaction> {
    public static MessageReactionController instance() {
        return (MessageReactionController) DataAccessRegistry.instance().get("sch_message_reactions");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(MessageReaction.class, MessageReactionController.class, true);
    }
}
