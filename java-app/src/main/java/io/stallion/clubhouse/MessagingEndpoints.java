package io.stallion.clubhouse;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.Context;
import io.stallion.clubhouse.webSockets.WebSocketEventHandler;
import io.stallion.dataAccess.db.DB;
import io.stallion.exceptions.ClientException;
import io.stallion.requests.validators.SafeMerger;
import io.stallion.restfulEndpoints.BodyParam;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.MinRole;
import io.stallion.restfulEndpoints.ObjectParam;
import io.stallion.services.Log;
import io.stallion.users.IUser;
import io.stallion.users.Role;
import io.stallion.users.User;
import io.stallion.users.UserController;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.JSON;

import javax.ws.rs.*;

@Path("/clubhouse-api/messaging")
@Produces("application/json")
@MinRole(Role.MEMBER)
public class MessagingEndpoints implements EndpointResource {

    @GET
    @Path("/my-channels")
    public Object getUserChannels() {
        /*
            private Long id;
    private String name;
    private boolean allowReactions = true;
    private boolean displayEmbeds = true;
    private ChannelType channelType;
    private boolean hasNew = false;
    private int mentionsCount = 0;
         */
        Map ctx = map();
        List<ChannelCombo> standardChannels = list();
        List<ChannelCombo> directMessageChannels = list();
        List<ChannelCombo> forumChannels = list();

        List<ChannelCombo> channels = DB.instance().queryBean(
                ChannelCombo.class,
                " SELECT c.id, c.name, c.allowReactions, c.displayEmbeds, c.channelType " +
                        " FROM sch_channels AS c " +
                        " INNER JOIN sch_channel_members AS cm ON c.id=cm.channelId" +
                        " WHERE cm.userId=?  ",
                Context.getUser().getId()
        );
        for(ChannelCombo cc: channels) {
            if (cc.getChannelType().equals(ChannelType.CHANNEL)) {
                standardChannels.add(cc);
            } else if (cc.getChannelType().equals(ChannelType.FORUM)) {
                forumChannels.add(cc);
            } else {
                if (!empty(cc.getDirectMessageUserIds())) {
                    String name = "";
                    for (Long userId: cc.getDirectMessageUserIdsList()) {
                        if (userId.equals(Context.getUser().getId())) {
                            continue;
                        }
                        IUser user = UserController.instance().forId(userId);
                        String personName = or(user.getDisplayName(), user.getUsername());
                        if (!empty(name)) {
                            name += ", ";
                        }
                        name += personName;
                    }
                    cc.setName(name);
                }
                directMessageChannels.add(cc);

            }
        }
        ctx.put("standardChannels", standardChannels);
        ctx.put("directMessageChannels", directMessageChannels);
        ctx.put("forumChannels", forumChannels);

        return ctx;
    }


    @GET
    @Path("/my-channel-context/:channelId")
    public Object getChannelContext(@PathParam("channelId") Long channelId, @QueryParam("page") Integer page) {
        Map ctx = map();

        ctx.put("channel", ChannelController.instance().getIfViewable(channelId));
        ctx.put("messages", MessageController.instance().loadMessagesForChannel(Context.getUser().getId(), channelId, page));
        ctx.put("members", ChannelController.instance().listChannelUsers(channelId));
        return ctx;
    }

    @POST
    @Path("/open-direct-message")
    public Object openDirectMessage(@BodyParam("userIds") List<Long> userIdsInts) {
        List<Long> userIds = list();
        for(Object userIdInt: userIdsInts) {
            if (userIdInt instanceof Integer) {
                userIds.add(new Long((Integer)userIdInt));
            } else {
                userIds.add((Long)userIdInt);
            }
        }
        userIds.add(Context.getUser().getId());

        Collections.sort(userIds);
        Map<String, Object> ctx = map();
        String key = GeneralUtils.md5Hash(JSON.stringify(userIds));
        Channel channel = ChannelController.instance().forUniqueKey("uniqueHash", key);
        if (channel == null) {
            channel = new Channel()
                    .setUniqueHash(key)
                    .setChannelType(ChannelType.DIRECT_MESSAGE)
                    .setDefaultForNewUsers(false)
                    .setEncrypted(true)
                    .setHidden(true)
                    .setInviteOnly(true)
                    .setDirectMessageUserIds(userIds)
                    .setName(key);
            ChannelController.instance().save(channel);
            for(Long userId: userIds) {
                ChannelMember cm = new ChannelMember()
                        .setUserId(userId)
                        .setCanPost(true)
                        .setChannelId(channel.getId())
                        .setOwner(true)
                        .setJoinedAt(DateUtils.utcNow())
                        ;
                ChannelMemberController.instance().save(cm);
            }
        }
        ctx.put("channel", channel);
        return ctx;
    }

    @POST
    @Path("/post-message")
    public Object postMessage(@ObjectParam Message rawMessage) {

        Message message = new SafeMerger()
                .nonEmpty("channelId", "messageJson")
                .merge(rawMessage);
        message.setFromUserId(Context.getUser().getId());
        message.setFromUsername(Context.getUser().getUsername());
        message.setCreatedAt(DateUtils.utcNow());
        // Verify user can post to channel
        ChannelMember member = ChannelMemberController.instance()
                .filter("userId", Context.getUser().getId())
                .filter("channelId", message.getChannelId())
                .first();
        if (member == null || !member.isCanPost()) {
            throw new ClientException("You do not have permission to post to this channel.");
        }
        MessageController.instance().save(message);
        for(ChannelMember cm: ChannelMemberController.instance().filter("channelId", message.getChannelId()).all()) {
            UserMessage um = new UserMessage()
                    .setChannelId(message.getChannelId())
                    .setMessageId(message.getId())
                    .setDate(message.getCreatedAt())
                    .setRead(false)
                    .setUserId(cm.getUserId())
                    ;
            UserMessageController.instance().save(um);
            WebSocketEventHandler.sendMessageToUser(um.getUserId(), JSON.stringify(map(val("message", message), val("type", "new-message"))));
        }

        return message;
    }


    @POST
    @Path("/post-encrypted-message")
    public Object postEncryptedMessage(@ObjectParam EncryptedMessageContainer container) {

        Message message = new Message();

        message.setFromUserId(Context.getUser().getId());
        message.setFromUsername(Context.getUser().getUsername());
        message.setChannelId(container.getChannelId());
        message.setMessageEncryptedJson(container.getMessageEncryptedJson());
        message.setMessageEncryptedJsonVector(container.getMessageVectorHex());
        message.setCreatedAt(utcNow());
        // Verify user can post to channel
        ChannelMember member = ChannelMemberController.instance()
                .filter("userId", Context.getUser().getId())
                .filter("channelId", message.getChannelId())
                .first();
        if (member == null || !member.isCanPost()) {
            throw new ClientException("You do not have permission to post to this channel.");
        }
        MessageController.instance().save(message);

        for(EncryptedUserPasswordsContainer userContainer: container.getEncryptedPasswords()) {
            UserMessage um = new UserMessage()
                    .setChannelId(message.getChannelId())
                    .setMessageId(message.getId())
                    .setDate(message.getCreatedAt())
                    .setRead(false)
                    .setUserId(userContainer.getUserId())
                    .setEncryptedPasswordHex(userContainer.getEncryptedPasswordHex())
                    .setPasswordVectorHex(userContainer.getPasswordVectorHex())
                    ;
            if (um.getUserId().equals(message.getFromUserId())) {
                um.setRead(true);
            }
            UserMessageController.instance().save(um);

            MessageCombo combo = new MessageCombo()
                    .setId(message.getId())
                    .setMessageJson(message.getMessageJson())
                    .setMessageEncryptedJson(message.getMessageEncryptedJson())
                    .setMessageEncryptedJsonVector(message.getMessageEncryptedJsonVector())
                    .setCreatedAt(message.getCreatedAt())
                    .setEdited(message.isEdited())
                    .setEncryptedPasswordHex(um.getEncryptedPasswordHex())
                    .setPasswordVectorHex(um.getPasswordVectorHex())
                    .setFromUserId(message.getFromUserId())
                    .setFromUsername(message.getFromUsername())
                    .setRead(um.isRead())
                    ;




            WebSocketEventHandler.sendMessageToUser(um.getUserId(), JSON.stringify(map(val("message", combo), val("type", "new-message"))));
        }


        return true;
    }

    public static class EncryptedMessageContainer {
        private String messageEncryptedJson = "";
        private String messageVectorHex = "";
        private Long channelId;
        List<EncryptedUserPasswordsContainer> encryptedPasswords = list();

        public String getMessageEncryptedJson() {
            return messageEncryptedJson;
        }

        public EncryptedMessageContainer setMessageEncryptedJson(String messageEncryptedJson) {
            this.messageEncryptedJson = messageEncryptedJson;
            return this;
        }

        public String getMessageVectorHex() {
            return messageVectorHex;
        }

        public EncryptedMessageContainer setMessageVectorHex(String messageVectorHex) {
            this.messageVectorHex = messageVectorHex;
            return this;
        }

        public Long getChannelId() {
            return channelId;
        }

        public EncryptedMessageContainer setChannelId(Long channelId) {
            this.channelId = channelId;
            return this;
        }

        public List<EncryptedUserPasswordsContainer> getEncryptedPasswords() {
            return encryptedPasswords;
        }

        public EncryptedMessageContainer setEncryptedPasswords(List<EncryptedUserPasswordsContainer> encryptedPasswords) {
            this.encryptedPasswords = encryptedPasswords;
            return this;
        }
    }

    public static class EncryptedUserPasswordsContainer {
        private String encryptedPasswordHex;
        private String passwordVectorHex;
        private Long userId;
        private String username;

        public String getEncryptedPasswordHex() {
            return encryptedPasswordHex;
        }

        public EncryptedUserPasswordsContainer setEncryptedPasswordHex(String encryptedPasswordHex) {
            this.encryptedPasswordHex = encryptedPasswordHex;
            return this;
        }

        public String getPasswordVectorHex() {
            return passwordVectorHex;
        }

        public EncryptedUserPasswordsContainer setPasswordVectorHex(String passwordVectorHex) {
            this.passwordVectorHex = passwordVectorHex;
            return this;
        }

        public Long getUserId() {
            return userId;
        }

        public EncryptedUserPasswordsContainer setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public String getUsername() {
            return username;
        }

        public EncryptedUserPasswordsContainer setUsername(String username) {
            this.username = username;
            return this;
        }
    }


}
