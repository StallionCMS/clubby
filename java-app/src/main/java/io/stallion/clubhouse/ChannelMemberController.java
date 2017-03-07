package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.services.Log;


public class ChannelMemberController extends StandardModelController<ChannelMember> {
    public static ChannelMemberController instance() {
        return (ChannelMemberController) DataAccessRegistry.instance().get("sch_channel_members");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(ChannelMember.class, ChannelMemberController.class, true);
    }

    public ChannelMember forUserChannel(Long userId, Long channelId) {
        return filter("userId", userId).filter("channelId", channelId).first();
    }
}
