package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.Context;
import io.stallion.exceptions.ClientException;
import io.stallion.requests.validators.SafeMerger;
import io.stallion.restfulEndpoints.BodyParam;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.MinRole;
import io.stallion.restfulEndpoints.ObjectParam;
import io.stallion.services.Log;
import io.stallion.users.Role;

import javax.ws.rs.*;

@Path("/clubhouse-api/messaging")
@Produces("application/json")
@MinRole(Role.MEMBER)
public class MessagingEndpoints implements EndpointResource {

    @GET
    @Path("/my-channels/:channelId")
    public Object getUserChannels() {
        Map ctx = map();


        return ctx;
    }


    @GET
    @Path("/my-channel-context/:channelId")
    public Object getChannelContext(@PathParam("channelId") Long channelId, @QueryParam("page") Integer page) {
        Map ctx = map();

        ctx.put("channel", ChannelController.instance().getIfViewable(channelId));
        ctx.put("messages", MessageController.instance().loadMessagesForChannel(Context.getUser().getId(), channelId, page));

        return ctx;
    }

    @POST
    @Path("/post-encrypted-message")
    public Object postEncryptedMessage(@ObjectParam Message rawMessage) {
        Message message = new SafeMerger()
                .nonEmpty("channelId", "messageEncryptedJson")
                .merge(rawMessage);

        message.setFromUserId(Context.getUser().getId());
        message.setFromUsername(Context.getUser().getUsername());

        // Verify user can post to channel
        ChannelMember member = ChannelMemberController.instance()
                .filter("userId", Context.getUser().getId())
                .filter("channelId", message.getChannelId())
                .first();
        if (member == null || !member.isCanPost()) {
            throw new ClientException("You do not have permission to post to this channel.");
        }
        MessageController.instance().save(message);
        return true;
    }


}
