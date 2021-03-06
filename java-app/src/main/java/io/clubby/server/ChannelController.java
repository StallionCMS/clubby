package io.clubby.server;

import io.stallion.Context;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.dataAccess.db.DB;

import javax.ws.rs.ClientErrorException;
import java.math.BigInteger;
import java.util.List;

import static io.stallion.utils.Literals.empty;


public class ChannelController extends StandardModelController<Channel> {
    public static ChannelController instance() {
        return (ChannelController) DataAccessRegistry.instance().get("sch_channels");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(Channel.class, ChannelController.class, true);
    }

    @Override
    public void onPreSaveValidate(Channel obj) {
        if (empty(obj.getName())) {
            throw new ClientErrorException("Channel must have a name.", 400);
        }
        Channel existing = ChannelController.instance().filter("name", obj.getName()).first();
        if (existing != null && !existing.getId().equals(obj.getId())) {
            throw new ClientErrorException("A channel with the name " + obj.getName() + " already exists.", 400);
        }
    }



    public Long getFirstUserChannel(Long userId) {
        BigInteger big = ((BigInteger)DB.instance().queryScalar(" " +
                " SELECT MIN(c.id) FROM sch_channels AS c " +
                " INNER JOIN sch_channel_members AS cm ON cm.channelId=c.id" +
                " WHERE cm.userId=? ",
                userId
        ));
        if (big == null) {
            return 0L;
        } else {
            return big.longValue();
        }
    }

    public List<ChannelUserWrapper> listChannelPossibleUsers(Long channelId) {
        List<ChannelUserWrapper> channelUsers = DB.instance().queryBean(
                ChannelUserWrapper.class,
                "" +
                        " SELECT su.id, su.displayName, su.email, su.username, up.aboutMe, up.webSite, up.publicKeyJwkJson, cm.id AS channelMemberId " +
                        " FROM stallion_users AS su" +
                        " INNER JOIN sch_user_profiles as up ON up.userId=su.id " +
                        " LEFT OUTER JOIN sch_channel_members as cm ON cm.userId=su.id AND cm.channelId=? " +
                        "  ",
                channelId
        );
        return channelUsers;
    }

    public List<ChannelUserWrapper> listChannelUsers(Long channelId) {
        List<ChannelUserWrapper> channelUsers = DB.instance().queryBean(
                ChannelUserWrapper.class,
                "" +
                        " SELECT su.id, su.displayName, su.email, su.username, up.aboutMe, up.webSite, up.publicKeyJwkJson " +
                        " FROM stallion_users AS su" +
                        " INNER JOIN sch_user_profiles as up ON up.userId=su.id " +
                        " INNER JOIN sch_channel_members as cm ON cm.userId=su.id " +
                        " WHERE cm.channelId=? AND su.approved=1 ",
                channelId
        );
        return channelUsers;



    }

    public ChannelCombo getChannelCombo(Long channelId) {
        List<ChannelCombo> channels = DB.instance().queryBean(
                ChannelCombo.class,
                " SELECT c.id, c.name, c.allowReactions, c.displayEmbeds, c.channelType, c.directMessageUserIds, " +
                        " cm.owner, cm.canPost, cm.userId as channelMemberId, c.encrypted, cm.favorite, c.inviteOnly, c.wikiStyle " +
                        " FROM sch_channels AS c " +
                        " LEFT OUTER JOIN sch_channel_members AS cm ON c.id=cm.channelId AND cm.userId=?" +
                        " WHERE (cm.userId=? OR c.inviteOnly=0) AND c.deleted=0 AND c.id=? ",
                Context.getUser().getId(),
                Context.getUser().getId(),
                channelId
        );
        if (channels.size() < 1) {
            throw new ClientErrorException("Channel not found or you do not have access.", 400);
        }
        return channels.get(0);
    }

    public Channel getIfViewable(Long channelId) {
        Channel channel = forIdOrNotFound(channelId);
        ChannelMember cm = ChannelMemberController.instance()
                .filter("channelId", channelId)
                .filter("userId", Context.getUser().getId())
                .first();
        if (cm == null) {
            throw new ClientErrorException("You do not have access to this channel.", 400);
        }
        return channel;
    }



}
