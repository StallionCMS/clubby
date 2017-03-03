package io.stallion.clubhouse;

import java.math.BigInteger;
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
import io.stallion.utils.Sanitize;
import io.stallion.utils.json.JSON;

import javax.ws.rs.*;

@Path("/clubhouse-api/messaging")
@Produces("application/json")
@MinRole(Role.MEMBER)
public class MessagingEndpoints implements EndpointResource {

    @GET
    @Path("/get-channel-members/:channelId")
    public Object getChannelMembers(@PathParam("channelId") Long channelId) {
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
        ctx.put("members", ChannelController.instance().listChannelPossibleUsers(channelId));
        return ctx;
    }


    @POST
    @Path("/delete-message")
    public Object deleteMessage(@BodyParam("messageId") Long messageId) {

        Message message = MessageController.instance().forIdOrNotFound(messageId);
        if (!MessageController.instance().currentUserCanDelete(message)) {
            throw new ClientException("You are not authorized to delete this message.", 403);
        }
        MessageController.instance().hardDelete(message);
        return true;
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


    @POST
    @Path("/create-channel")
    public Object createChannel( @ObjectParam Channel updatedChannel) {
        Channel channel = new Channel();

        new SafeMerger()
                .optional("name", "inviteOnly", "encrypted", "purgeAfterDays")
                .merge(updatedChannel, channel);
        channel.setName(Sanitize.stripAll(channel.getName()));
        channel.setChannelType(ChannelType.CHANNEL);
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
                .optional("name", "inviteOnly", "encrypted", "purgeAfterDays")
                .merge(updatedChannel, channel);
        ChannelController.instance().save(channel);
        ctx.put("channel", channel);
        return ctx;
    }


    @GET
    @Path("/general-context")
    public Object getGeneralContext() {
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
                " SELECT c.id, c.name, c.allowReactions, c.displayEmbeds, c.channelType, c.directMessageUserIds, cm.owner, cm.canPost " +
                        " FROM sch_channels AS c " +
                        " INNER JOIN sch_channel_members AS cm ON c.id=cm.channelId" +
                        " WHERE cm.userId=? AND c.deleted=0 ",
                Context.getUser().getId()
        );
        Map<Long, ChannelCombo> channelById = map();

        for(ChannelCombo cc: channels) {
            channelById.put(cc.getId(), cc);
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

        // Get mention count by channel
        List<Map<String, Object>> records = DB.instance().findRecords("SELECT channelId, COUNT(*) as mentions FROM sch_user_messages WHERE `mentioned`=1 AND `read`=0 AND userId=? GROUP BY channelId;",
                Context.getUser().getId()
        );
        for (Map<String, Object> record: records) {
            Long channelId = (Long)record.get("channelId");
            Long mentionCount = (Long)record.get("mentions");
            ChannelCombo cc = channelById.getOrDefault(channelId, null);
            if (cc != null) {
                cc.setMentionsCount(mentionCount.intValue());
            }
        }


        // Get unread channels
        List<Long> channelIds = DB.instance().queryColumn("" +
                " SELECT DISTINCT(sch_user_messages.channelId) FROM sch_user_messages " +
                " INNER JOIN sch_messages ON sch_user_messages.messageId=sch_messages.id " +
                " WHERE `read`=0 AND userId=? AND sch_messages.deleted=0 AND sch_user_messages.deleted=0 AND sch_messages.fromUserId!=? " +
                " GROUP BY channelId;",
                Context.getUser().getId(),
                Context.getUser().getId()
        );
        for(Long channelId: channelIds) {
            ChannelCombo cc = channelById.getOrDefault(channelId, null);
            if (cc != null) {
                cc.setHasNew(true);
            }
        }


        // Load all users
        List<ChannelUserWrapper> users = DB.instance().queryBean(
                ChannelUserWrapper.class,
                "" +
                        " SELECT su.id, su.displayName, su.email, su.username, up.aboutMe, up.webSite, " +
                        "        up.publicKeyHex, us.state, up.avatarUrl " +
                        " FROM stallion_users AS su" +
                        " INNER JOIN sch_user_profiles as up ON up.userId=su.id " +
                        " LEFT OUTER JOIN sch_user_states AS us ON us.userId=su.id WHERE su.deleted=0 " +
                        " "
        );
        for(ChannelUserWrapper user: users) {
            if (empty(user.getAvatarUrl())) {
                user.setAvatarUrl("https://www.gravatar.com/avatar/" + GeneralUtils.md5Hash(user.getEmail()) + "?d=retro");
            }
        }
        ctx.put("users", users);

        return ctx;
    }

    @POST
    @Path("/add-reaction")
    public Object addReaction(@BodyParam("messageId") Long messageId, @BodyParam("emoji") String emoji) {
        // TODO: verify that can access this channel
        Message message = MessageController.instance().forIdOrNotFound(messageId);
        MessageReaction reaction = new MessageReaction()
                .setCreatedAt(DateUtils.utcNow())
                .setDisplayName(Context.getUser().getUsername())
                .setEmoji(emoji)
                .setMessageId(messageId)
                .setUserId(Context.getUser().getId())
                .setRecipientUserId(message.getFromUserId())
                ;
        MessageReactionController.instance().save(reaction);
        return true;
    }


    @POST
    @Path("/remove-reaction")
    public Object removeReaction(@BodyParam("messageId") Long messageId, @BodyParam("emoji") String emoji) {
        MessageReaction reaction = MessageReactionController.instance()
                .filter("emoji", emoji)
                .filter("messageId", messageId)
                .filter("userId", Context.getUser().getId())
                .first()
                ;
        if (reaction != null) {
            MessageReactionController.instance().hardDelete(reaction);
        }
        return true;
    }


    @GET
    @Path("/my-channel-context/:channelId")
    public Object getChannelContext(@PathParam("channelId") Long channelId, @QueryParam("page") Integer page) {
        Map ctx = map();

        ctx.put("channel", ChannelController.instance().getIfViewable(channelId));
        ctx.put("messages", MessageController.instance().loadMessagesForChannel(Context.getUser().getId(), channelId, page, true));
        ctx.put("members", ChannelController.instance().listChannelUsers(channelId));
        return ctx;
    }

    @POST
    @Path("/mark-read")
    public Object markRead(@BodyParam("messageId") Long messageId) {
        MessageController.instance().markRead(Context.getUser().getId(), messageId);
        return true;
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
                        .setOwner(false)
                        .setJoinedAt(DateUtils.utcNow())
                        ;
                ChannelMemberController.instance().save(cm);
            }
        }
        ctx.put("channel", channel);
        return ctx;
    }

    @POST
    @Path("/update-message")
    public Object updateMessage(@ObjectParam Message updated) {

        Message message = MessageController
                .instance()
                .forIdOrNotFound(updated.getId());
        if (!MessageController.instance().currentUserCanEdit(message)) {
            throw new ClientException("You do not have permission to edit this message.", 403);
        }
        message.setMessageJson(updated.getMessageJson());
        message.setEdited(true);
        message.setEditedAt(DateUtils.utcNow());
        MessageController.instance().save(message);
        notifyMessageUpdated(message);
        return true;
    }


    @POST
    @Path("/update-encrypted-message")
    public Object updateEncryptedMessage(@ObjectParam Message updated) {

        Message message = MessageController
                .instance()
                .forIdOrNotFound(updated.getId());
        if (!MessageController.instance().currentUserCanEdit(message)) {
            throw new ClientException("You do not have permission to edit this message.", 403);
        }
        message.setMessageEncryptedJson(updated.getMessageEncryptedJson());
        message.setMessageEncryptedJsonVector(updated.getMessageEncryptedJsonVector());
        message.setEdited(true);
        message.setEditedAt(DateUtils.utcNow());
        MessageController.instance().save(message);
        notifyMessageUpdated(message);
        return true;
    }

    public void notifyMessageUpdated(Message message) {
        for(UserMessage um: UserMessageController.instance().filter("messageId", message.getId()).all()) {
            MessageCombo combo = messageUserMessageToCombo(message, um);
            WebSocketEventHandler.sendMessageToUser(um.getUserId(), JSON.stringify(map(val("message", combo), val("type", "message-edited"))));
        }
    }


    @POST
    @Path("/post-message")
    public Object postMessage(@ObjectParam Message rawMessage) {
        Channel channel = ChannelController.instance().forIdOrNotFound(rawMessage.getChannelId());
        Message message = new SafeMerger()
                .optional("usersMentioned")
                .nonEmpty("channelId", "messageJson", "hereMentioned", "channelMentioned")
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
            if (um.getUserId().equals(message.getFromUserId())) {
                um.setRead(true);
            }
            if (channel.getChannelType().equals(ChannelType.DIRECT_MESSAGE)) {
                um.setMentioned(true);
            }
            if (rawMessage.isHereMentioned()) {
                um.setHereMentioned(true);
            }
            if (rawMessage.isChannelMentioned()) {
                um.setMentioned(true);
            }
            IUser user = UserController.instance().forId(cm.getUserId());
            if (rawMessage.getUsersMentioned().contains(user.getUsername())) {
                um.setMentioned(true);
            }
            UserMessageController.instance().save(um);

            notifyOfNewMessage(message, um);
        }

        return message;
    }


    @POST
    @Path("/post-encrypted-message")
    public Object postEncryptedMessage(@ObjectParam EncryptedMessageContainer container) {
        Channel channel = ChannelController.instance().forIdOrNotFound(container.getChannelId());
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
            if (channel.getChannelType().equals(ChannelType.DIRECT_MESSAGE)) {
                um.setMentioned(true);
            }
            if (container.isHereMentioned()) {
                um.setHereMentioned(true);
            }
            if (container.isChannelMentioned()) {
                um.setMentioned(true);
            }
            IUser user = UserController.instance().forId(um.getUserId());
            if (container.getUsersMentioned().contains(user.getUsername())) {
                um.setMentioned(true);
            }
            UserMessageController.instance().save(um);
            notifyOfNewMessage(message, um);
        }


        return true;
    }

    public MessageCombo messageUserMessageToCombo(Message message, UserMessage um) {
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
                .setUserMessageId(um.getId())
                .setChannelId(message.getChannelId())
                ;

        if (um.isHereMentioned() || um.isMentioned()) {
            combo.setMentioned(true);
        }
        return combo;
    }

    public void notifyOfNewMessage(Message message, UserMessage um) {
        MessageCombo combo = messageUserMessageToCombo(message, um);

        WebSocketEventHandler.sendMessageToUser(um.getUserId(), JSON.stringify(map(val("message", combo), val("type", "new-message"))));
    }

    public static class EncryptedMessageContainer {
        private String messageEncryptedJson = "";
        private String messageVectorHex = "";
        private Long channelId;
        List<EncryptedUserPasswordsContainer> encryptedPasswords = list();
        private boolean hereMentioned = false;
        private boolean channelMentioned = false;
        private List<String> usersMentioned = list();

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

        public boolean isHereMentioned() {
            return hereMentioned;
        }

        public EncryptedMessageContainer setHereMentioned(boolean hereMentioned) {
            this.hereMentioned = hereMentioned;
            return this;
        }

        public boolean isChannelMentioned() {
            return channelMentioned;
        }

        public EncryptedMessageContainer setChannelMentioned(boolean channelMentioned) {
            this.channelMentioned = channelMentioned;
            return this;
        }

        public List<String> getUsersMentioned() {
            return usersMentioned;
        }

        public EncryptedMessageContainer setUsersMentioned(List<String> usersMentioned) {
            this.usersMentioned = usersMentioned;
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
