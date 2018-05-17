package io.clubby.server;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.Context;
import io.clubby.server.webSockets.WebSocketEventHandler;
import io.stallion.dataAccess.db.DB;
import io.stallion.exceptions.ClientException;
import io.stallion.requests.validators.SafeMerger;
import io.stallion.restfulEndpoints.BodyParam;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.MinRole;
import io.stallion.restfulEndpoints.ObjectParam;
import io.stallion.users.Role;
import io.stallion.utils.DateUtils;
import io.stallion.utils.Sanitize;

import javax.ws.rs.*;

@Path("/clubhouse-api/channels")
@Produces("application/json")
@MinRole(Role.MEMBER)
public class ChannelEndpoints implements EndpointResource {

    @GET
    @Path("/my-channels")
    public Object myChannels() {
        List<ChannelCombo> channels = DB.instance().queryBean(
                ChannelCombo.class,
                " SELECT c.id, c.name, c.allowReactions, c.displayEmbeds, c.channelType, c.directMessageUserIds, " +
                        " cm.owner, cm.canPost, cm.userId as channelMemberId, c.encrypted, cm.favorite, c.wikiStyle " +
                        " FROM sch_channels AS c " +
                        " LEFT OUTER JOIN sch_channel_members AS cm ON c.id=cm.channelId AND cm.userId=?" +
                        " WHERE (cm.userId=? OR c.inviteOnly=0) AND c.deleted=0 AND c.channelType='CHANNEL' or c.channelType='FORUM' " +
                        " ORDER BY c.name ASC ",
                Context.getUser().getId(),
                Context.getUser().getId()
        );
        channels.sort((a, b)->a.getName().compareTo(b.getName()));
        Map ctx = map();
        ctx.put("channels", channels);
        return ctx;
    }

    @GET
    @Path("/get-channel-members/:channelId")
    public Object getChannelMembers(@PathParam("channelId") Long channelId) {
        ChannelCombo channel = ChannelController.instance().getChannelCombo(channelId);
        ChannelMember channelMember = ChannelMemberController.instance()
                .filter("channelId", channelId)
                .filter("userId", Context.getUser().getId())
                .first();
        if (channel == null || channelMember == null) {
            throw new ClientException("You cannot view this channel's members.");
        }
        Map ctx = map();


        List<ChannelUserWrapper> cus = list();
        for(ChannelUserWrapper cu: ChannelController.instance().listChannelPossibleUsers(channelId)) {
            if (channelMember.isOwner()) {
                cus.add(cu);
            } else if (!empty(cu.getChannelMemberId())){
                cus.add(
                        new ChannelUserWrapper()
                                .setDisplayName(cu.getDisplayName())
                                .setState(cu.getState())
                                .setUsername(cu.getUsername())
                                .setId(cu.getId())
                );
            }
        }


        ctx.put("channel", channel);
        ctx.put("members", cus);
        return ctx;
    }


    @GET
    @Path("/channel-details/:channelId")
    public Object channelDetails(@PathParam("channelId") Long channelId) {
        Channel channel = ChannelController.instance().forIdWithDeleted(channelId);
        ChannelMember channelMember = ChannelMemberController.instance()
                .filter("channelId", channelId)
                .filter("userId", Context.getUser().getId())
                .first();
        if (channel == null || channelMember == null || !channelMember.isOwner()) {
            throw new ClientException("You cannot edit this channel.");
        }
        Map ctx = map();
        ctx.put("channel", channel);
        return ctx;
    }

    @POST
    @Path("/leave-channel/:channelId")
    public Object leaveChannel(@PathParam("channelId") Long channelId) {
        Channel channel = ChannelController.instance().forIdWithDeleted(channelId);
        ChannelMember channelMember = ChannelMemberController.instance()
                .filter("channelId", channelId)
                .filter("userId", Context.getUser().getId())
                .first();
        if (channel == null || channelMember == null) {
            return map();
        }
        ChannelMemberController.instance().hardDelete(channelMember);
        return map(val("succeeded", true));
    }


    @POST
    @Path("/create-channel")
    public Object createChannel( @ObjectParam Channel updatedChannel) {
        Channel channel = new Channel();

        new SafeMerger()
                .nonEmpty("name", "channelType")
                .optional("inviteOnly", "encrypted", "purgeAfterDays", "wikiStyle")
                .merge(updatedChannel, channel);
        channel.setName(Sanitize.stripAll(channel.getName()));
        //channel.setChannelType(ChannelType.CHANNEL);
        ChannelController.instance().save(channel);
        ChannelMember cm = new ChannelMember()
                .setJoinedAt(DateUtils.utcNow())
                .setCanPost(true)
                .setOwner(true)
                .setChannelId(channel.getId())
                .setHidden(false)
                .setUserId(Context.getUser().getId());
        ChannelMemberController.instance().save(cm);
        Map ctx = map();
        ctx.put("channel", channel);
        return ctx;
    }


    @POST
    @Path("/update-channel/:channelId")
    public Object updateChannel(@PathParam("channelId") Long channelId, @ObjectParam Channel updatedChannel) {
        Channel channel = ChannelController.instance().forIdOrNotFound(channelId);
        ChannelMember channelMember = ChannelMemberController.instance()
                .filter("channelId", channelId)
                .filter("userId", Context.getUser().getId())
                .first();
        if (channelMember == null || !channelMember.isOwner()) {
            throw new ClientException("You cannot edit this channel.");
        }
        Map ctx = map();

        new SafeMerger()
                .nonEmpty("name")
                .optional("inviteOnly", "encrypted", "purgeAfterDays", "wikiStyle")
                .merge(updatedChannel, channel);

        ChannelController.instance().save(channel);
        ctx.put("channel", channel);
        return ctx;
    }



    @POST
    @Path("/archive-channel/:channelId")
    public Object archiveChannel(@PathParam("channelId") Long channelId) {
        Channel channel = ChannelController.instance().forIdWithDeleted(channelId);
        ChannelMember channelMember = ChannelMemberController.instance()
                .filter("channelId", channelId)
                .filter("userId", Context.getUser().getId())
                .first();

        if (channel == null || channelMember == null || !channelMember.isOwner()) {
            throw new ClientException("You cannot edit this channel.");
        }

        ChannelController.instance().softDelete(channel);
        return true;
    }


    @POST
    @Path("/unarchive-channel/:channelId")
    public Object unarchiveChannel(@PathParam("channelId") Long channelId) {
        Channel channel = ChannelController.instance().forIdWithDeleted(channelId);
        ChannelMember channelMember = ChannelMemberController.instance()
                .filter("channelId", channelId)
                .filter("userId", Context.getUser().getId())
                .first();

        if (channel == null || channelMember == null || !channelMember.isOwner()) {
            throw new ClientException("You cannot edit this channel.");
        }
        channel.setName(Sanitize.stripAll(channel.getName()));
        channel.setDeleted(false);
        ChannelController.instance().save(channel);
        return true;
    }


    @Path("/mark-channel-favorite/:channelId")
    @POST
    public Object markChannelFavorite(@PathParam("channelId") Long channelId, @BodyParam("favorite") boolean favorite) {
        ChannelMember cm = ChannelMemberController.instance()
                .filter("channelId", channelId)
                .filter("userId", Context.getUser().getId())
                .first();
        if (cm == null) {
            return false;
        }
        cm.setFavorite(favorite);
        ChannelMemberController.instance().save(cm);
        return true;

    }


    @POST
    @Path("/add-channel-member")
    public Object addChannelMember(@BodyParam("userId") Long memberId, @BodyParam("channelId") Long channelId) {
        Channel channel = ChannelController.instance().forIdOrNotFound(channelId);
        boolean allowed = false;
        ChannelMember adder = ChannelMemberController.instance()
                .filter("userId", Context.getUser().getId())
                .filter("channelId", channelId)
                .first();
        if (adder != null && adder.isOwner()) {
            allowed = true;
        }
        if (!allowed && !channel.isInviteOnly() && Context.getUser().getId().equals(memberId)) {
            allowed = true;
        }
        if (!allowed) {
            throw new ClientException("You are not allowed to add a member to this channel.");
        }
        ChannelMember cm = new ChannelMember();
        cm.setOwner(false);
        cm.setCanPost(true);
        cm.setHidden(false);
        cm.setJoinedAt(DateUtils.utcNow());
        cm.setUserId(memberId);
        cm.setChannelId(channelId);
        ChannelMemberController.instance().save(cm);
        WebSocketEventHandler.notifyChannelChanges(memberId, channel, "added");
        return map(val("channelMemberId", cm.getId()));
    }


    @POST
    @Path("/remove-channel-member")
    public Object removeChannelMember(@BodyParam("userId") Long memberId, @BodyParam("channelId") Long channelId) {
        Channel channel = ChannelController.instance().forIdOrNotFound(channelId);
        boolean allowed = false;
        if (Context.getUser().getId().equals(memberId)) {
            allowed = true;
        } else {
            ChannelMember adder = ChannelMemberController.instance()
                    .filter("userId", Context.getUser().getId())
                    .filter("channelId", channelId)
                    .first();
            if (adder != null && adder.isOwner()) {
                allowed = true;
            }
        }
        if (!allowed) {
            throw new ClientException("You are not allowed remove this member from this channel.");
        }
        ChannelMember cm = ChannelMemberController.instance()
                .filter("userId", memberId)
                .filter("channelId", channelId)
                .first();
        if (cm != null) {
            ChannelMemberController.instance().hardDelete(cm);
        }
        WebSocketEventHandler.notifyChannelChanges(memberId, channel, "removed");
        return map(val("succeeded", true));

    }

    @POST
    @Path("/toggle-watched")
    public Object toggleWatched(@BodyParam("messageId") Long messageId, @BodyParam("watched") Boolean watched) {
        UserMessage userMessage = UserMessageController.instance()
                .filter("messageId", messageId)
                .filter("userId", Context.getUser().getId())
                .first()
                ;
        userMessage.setWatched(watched);
        UserMessageController.instance().save(userMessage);
        return true;
    }




}
