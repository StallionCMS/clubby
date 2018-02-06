package io.clubby.server;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;


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
