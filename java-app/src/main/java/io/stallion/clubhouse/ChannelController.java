package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.services.Log;


public class ChannelController extends StandardModelController<Channel> {
    public static ChannelController instance() {
        return (ChannelController) DataAccessRegistry.instance().get("sch_channels");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(Channel.class, ChannelController.class, true);
    }
}
