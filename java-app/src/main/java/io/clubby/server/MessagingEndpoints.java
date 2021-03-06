package io.clubby.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.clubby.server.webSockets.WebSocketEventHandler;
import io.stallion.Context;
import io.stallion.dataAccess.SafeMerger;
import io.stallion.dataAccess.db.DB;
import io.stallion.http.BodyParam;
import io.stallion.http.MinRole;
import io.stallion.users.IUser;
import io.stallion.users.Role;
import io.stallion.users.UserController;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.JSON;

import javax.ws.rs.*;
import javax.ws.rs.ext.Provider;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

@Path("/clubhouse-api/messaging")
@Consumes("application/json")
@Produces("application/json")
@MinRole(Role.MEMBER)
@Provider
public class MessagingEndpoints  {



    @GET
    @Path("/channel-messages-context/{channelId}")
    public Object getChannelContext(
            @PathParam("channelId") Long channelId,
            @QueryParam("page") Integer page,
            @QueryParam("threadId") Long threadId
    ) {
        Map ctx = map();


        ChannelMember channelMember = ChannelMemberController.instance()
                .filter("userId", Context.getUser().getId())
                .filter("channelId", channelId)
                .first();
        if (channelMember == null) {
            throw new ClientErrorException("You do not have access to this channel.", 403);
        }
        ChannelCombo channel = ChannelController.instance().getChannelCombo(channelId);
        ctx.put("channel", channel);
        ctx.put("channelMembership", channelMember);
        ctx.put("members", ChannelController.instance().listChannelUsers(channelId));
        ctx.put("retrievedAt", mils());
        if (!empty(threadId)) {
            MessageController.ThreadContext tc = MessageController.instance().loadMessagesForForumThread(
                    Context.getUser().getId(), channelId, threadId, page, true);
            ctx.put("messages", tc.getMessages());
            ctx.put("totalPageCount", tc.getPageCount());
            ctx.put("allIdsDates", tc.getAllIdsDates());
            ctx.put("topic", tc.getTopic());
            ctx.put("pageCount", tc.getPageCount());
            ctx.put("unreadCount", tc.getUnreadCount());
            ctx.put("unreadMentionsCount", tc.getUnreadMentionsCount());
        } else {
            MessageController.MessageContext messageContext = MessageController.instance().loadMessagesForChannel(
                    Context.getUser().getId(), channelId, page, true);
            ctx.put("messages", messageContext.getMessages());
            ctx.put("unreadCount", messageContext.getUnreadCount());
            ctx.put("unreadMentionsCount", messageContext.getUnreadMentionsCount());
        }



        return JSON.stringify(ctx);
    }

    @GET
    @Path("/forum-post-editor-context")
    public Object forumNewThreadContext(@QueryParam("channelId") Long channelId, @QueryParam("messageId") Long messageId) {
        Map ctx = map();
        Channel channel;
        if (!empty(messageId)) {
            MessageCombo message = MessageController.instance().getMessageCombo(
                    Context.getUser().getId(), messageId);
            if (!message.getFromUserId().equals(Context.getUser().getId())) {
                throw new ClientErrorException("You do not have rights to edit this message.", 403);
            }
            ctx.put("message", message);
            channel = ChannelController.instance().getIfViewable(message.getChannelId());
        } else {
            channel = ChannelController.instance().getIfViewable(channelId);
        }


        ctx.put("channel", channel);
        ctx.put("channelMembership", ChannelMemberController.instance()
                .filter("userId", Context.getUser().getId())
                .filter("channelId", channelId).first());
        ctx.put("members", ChannelController.instance().listChannelUsers(channelId));

        return JSON.stringify(ctx);
    }

    @GET
    @Path("/forum-top-level/{channelId}")
    public Object forumTopLevel(@PathParam("channelId") Long channelId, @QueryParam("page") Integer page) {
        Map ctx = map();

        ctx.put("channel", ChannelController.instance().getChannelCombo(channelId));
        ctx.put("channelMembership", ChannelMemberController.instance().filter("userId", Context.getUser().getId()).filter("channelId", channelId).first());
        ctx.put("topicContext", MessageController.instance().loadMessagesForForumTopLevel(Context.getUser().getId(), channelId, page));
        return JSON.stringify(ctx);
    }

   /*
    @GET
    @Path("/forum-thread/:channelId/:threadId")
    public Object forumThread(@PathParam("channelId") Long channelId, @PathParam("threadId") Long threadId, @QueryParam("page") Integer page) {
        Map ctx = map();

        ctx.put("channel", ChannelController.instance().getChannelCombo(channelId));
        ctx.put("channelMembership", ChannelMemberController.instance().filter("userId", Context.getUser().getId()).filter("channelId", channelId).first());
        ctx.put("threadContext", MessageController.instance().loadMessagesForForumThread(Context.getUser().getId(), channelId, threadId, page, true));
        return ctx;
    }
    */


    @POST
    @Path("/mark-read")
    public Object markRead(@BodyParam(value = "messageId", required = false) Long messageId, @BodyParam(value = "messageIds", required = false) List<Long> messageIds) {
        if (messageIds == null) {
            messageIds = list();
        }
        if (messageId != null)  {
            messageIds.add(messageId);
        }

        for(Object mid: messageIds) {
            Long midL = null;
            if (mid instanceof Integer) {
                midL = new Long((Integer)mid);
            } else {
                midL = (Long)mid;
            }
            MessageController.instance().markRead(Context.getUser().getId(), midL);
        }

        return true;
    }

    @POST
    @Path("/count-channel-messages-since")
    public Object markRead(@BodyParam("channelId") Long channelId, @BodyParam("mostRecentMessageAt") Long mostRecentMessageAt) {
        return map(
                val("retrievedAt", mils()),
                val("count", MessageController.instance().countMessagesUpdatedSinceTime(Context.getUser().getId(), channelId, mostRecentMessageAt))
        );
    }


    @POST
    @Path("/open-direct-message")
    public Object openDirectMessage(@BodyParam("userIds") List userIdsInts) {
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
        return JSON.stringify(ctx);
    }

    @POST
    @Path("/update-message")
    public Object updateMessage(Message updated) {

        Message message = MessageController
                .instance()
                .forIdOrNotFound(updated.getId());
        if (!MessageController.instance().currentUserCanEdit(message)) {
            throw new ClientErrorException("You do not have permission to edit this message.", 403);
        }
        message.setTitle(updated.getTitle());
        message.setMessageJson(updated.getMessageJson());
        message.setEdited(true);
        message.setEditedAt(DateUtils.utcNow());
        MessageController.instance().save(message);
        Channel channel = ChannelController.instance().forIdOrNotFound(message.getChannelId());
        if (channel.isWikiStyle()) {
            MessageVersionController.instance().saveVersion(message, Context.getUser().getId(), utcNow());
        }
        notifyMessageUpdated(message);
        return true;
    }


    @POST
    @Path("/update-encrypted-message")
    public Object updateEncryptedMessage(Message updated) {

        Message message = MessageController
                .instance()
                .forIdOrNotFound(updated.getId());
        if (!MessageController.instance().currentUserCanEdit(message)) {
            throw new ClientErrorException("You do not have permission to edit this message.", 403);
        }
        message.setMessageEncryptedJson(updated.getMessageEncryptedJson());
        message.setMessageEncryptedJsonVector(updated.getMessageEncryptedJsonVector());
        message.setEdited(true);
        message.setEditedAt(DateUtils.utcNow());
        message.setTitle(updated.getTitle());
        MessageController.instance().save(message);
        Channel channel = ChannelController.instance().forIdOrNotFound(message.getChannelId());
        if (channel.isWikiStyle()) {
            MessageVersionController.instance().saveVersion(message, Context.getUser().getId(), utcNow());
        }
        notifyMessageUpdated(message);
        return true;
    }

    public void notifyMessageUpdated(Message message) {
        for(UserMessage um: UserMessageController.instance().filter("messageId", message.getId()).all()) {
            MessageCombo combo = MessageController.instance().messageUserMessageToCombo(message, um);
            WebSocketEventHandler.sendMessageToUser(um.getUserId(), JSON.stringify(map(val("message", combo), val("type", "message-edited"))));
        }
    }


    @POST
    @Path("/post-message")
    public Object postMessage(Message rawMessage) {
        Channel channel = ChannelController.instance().forIdOrNotFound(rawMessage.getChannelId());
        Message message = new SafeMerger()
                .optional("usersMentioned", "title", "parentMessageId", "threadId")
                .nonEmpty("channelId", "messageJson", "hereMentioned", "channelMentioned")
                .merge(rawMessage);

        message.setFromUserId(Context.getUser().getId());
        message.setFromUsername(Context.getUser().getUsername());
        message.setCreatedAt(DateUtils.utcNow());
        message.setUpdatedAt(DateUtils.utcNow());
        // Verify user can post to channel
        ChannelMember member = ChannelMemberController.instance()
                .filter("userId", Context.getUser().getId())
                .filter("channelId", message.getChannelId())
                .first();
        if (member == null || !member.isCanPost()) {
            throw new ClientErrorException("You do not have permission to post to this channel.", 403);
        }
        message.setId(DB.instance().getTickets().nextId());
        if (empty(message.getThreadId())) {
            message.setThreadId(message.getId());
        }


        MessageController.instance().save(message);

        if (channel.isWikiStyle()) {
            MessageVersionController.instance().saveVersion(message, Context.getUser().getId(), utcNow());
        }



        if (!empty(message.getThreadId()) && !message.getId().equals(message.getThreadId())) {
            Message thread = MessageController.instance().forId(message.getThreadId());
            if (thread != null) {
                thread.setThreadUpdatedAt(utcNow());
                MessageController.instance().save(thread);
            }
        }

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
            if (user == null) {
                continue;
            }
            if (rawMessage.getUsersMentioned().contains(user.getUsername())) {
                um.setMentioned(true);
            }
            UserMessageController.instance().save(um);

            MessageController.instance().notifyOfNewMessage(message, um, channel);

        }

        return message;
    }

    @POST
    @Path("/post-encrypted-message")
    public Object postEncryptedMessage(EncryptedMessageContainer container) {
        Channel channel = ChannelController.instance().forIdOrNotFound(container.getChannelId());
        Message message = new Message();


        message.setFromUserId(Context.getUser().getId());
        message.setFromUsername(Context.getUser().getUsername());
        message.setChannelId(container.getChannelId());
        message.setMessageEncryptedJson(container.getMessageEncryptedJson());
        message.setMessageEncryptedJsonVector(container.getMessageVectorHex());
        message.setCreatedAt(utcNow());
        message.setUpdatedAt(utcNow());
        message.setTitle(container.getTitle());
        message.setThreadId(container.getThreadId());
        message.setParentMessageId(container.getParentMessageId());
        message.setId(DB.instance().getTickets().nextId());
        if (empty(message.getThreadId())) {
            message.setThreadId(message.getId());
        }


        //.optional("usersMentioned", "title", "parentMessageId", "threadId")

        // Verify user can post to channel
        ChannelMember member = ChannelMemberController.instance()
                .filter("userId", Context.getUser().getId())
                .filter("channelId", message.getChannelId())
                .first();
        if (member == null || !member.isCanPost()) {
            throw new ClientErrorException("You do not have permission to post to this channel.", 403);
        }
        MessageController.instance().save(message);
        if (channel.isWikiStyle()) {
            MessageVersionController.instance().saveVersion(message, Context.getUser().getId(), utcNow());
        }

        if (!empty(message.getThreadId()) && !message.getId().equals(message.getThreadId())) {
            Message thread = MessageController.instance().forId(message.getThreadId());
            if (thread != null) {
                thread.setThreadUpdatedAt(utcNow());
                MessageController.instance().save(thread);
            }
        }

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
            MessageController.instance().notifyOfNewMessage(message, um, channel);

        }


        return map(
                val("id", message.getId()),
                val("channelId", message.getChannelId()),
                val("threadId", message.getThreadId())
        );
    }


    @POST
    @Path("/delete-message")
    public Object deleteMessage(@BodyParam("messageId") Long messageId) {

        Message message = MessageController.instance().forIdOrNotFound(messageId);
        if (!MessageController.instance().currentUserCanDelete(message)) {
            throw new ClientErrorException("You are not authorized to delete this message.", 403);
        }
        if (message.getId().equals(message.getThreadId())) {
            Long childCount = DB.instance().queryScalar("SELECT COUNT(*) FROM sch_messages WHERE threadId=? AND id!=? AND deleted=0",
                    message.getThreadId(), messageId
                    );
            if (childCount > 0) {
                throw new ClientErrorException("You cannot delete a thread while it still has children. Delete all the children first.", 409);
            }
        }
        MessageController.instance().hardDelete(message);
        return true;
    }

    @POST
    @Path("/add-reaction")
    public Object addReaction(@BodyParam("messageId") Long messageId, @BodyParam("emoji") String emoji) {
        // TODO: verify that can access this channel
        Message message = MessageController.instance().forIdOrNotFound(messageId);

        MessageReaction existing = MessageReactionController.instance()
                .filter("messageId", messageId)
                .filter("userId", Context.getUser().getId())
                .filter("emoji", emoji)
                .first();
        if (existing != null) {
            return map(val("added", false));
        }


        MessageReaction reaction = new MessageReaction()
                .setCreatedAt(DateUtils.utcNow())
                .setDisplayName(Context.getUser().getUsername())
                .setEmoji(emoji)
                .setMessageId(messageId)
                .setUserId(Context.getUser().getId())
                .setRecipientUserId(message.getFromUserId())
                ;
        MessageReactionController.instance().save(reaction);
        reaction.setUsername(Context.getUser().getUsername());

        for(ChannelMember cm: ChannelMemberController.instance().filter("channelId", message.getChannelId()).all()) {
            if (cm.getUserId().equals(Context.getUser().getId())) {
                continue;
            }

            WebSocketEventHandler.sendMessageToUser(
                    cm.getUserId(),
                    JSON.stringify(
                            map(
                                    val("channelId", message.getChannelId()),
                                    val("reaction", reaction),
                                    val("type", "new-reaction")
                            )
                    )
            );
        }
        return map(val("added", true));
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
        if (reaction == null) {
            return map(val("removed", false));
        }
        if (reaction != null) {
            MessageReactionController.instance().hardDelete(reaction);
        }
        reaction.setUsername(Context.getUser().getUsername());
        Message msg = MessageController.instance().forId(reaction.getMessageId());
        if (msg != null) {
            for (ChannelMember cm : ChannelMemberController.instance().filter("channelId", msg.getChannelId()).all()) {
                if (cm.getUserId().equals(Context.getUser().getId())) {
                    continue;
                }
                WebSocketEventHandler.sendMessageToUser(
                        cm.getUserId(),
                        JSON.stringify(
                                map(
                                        val("channelId", msg.getChannelId()),
                                        val("reaction", reaction),
                                        val("type", "removed-reaction")
                                )
                        )
                );
            }
        }

        return map(val("removed", true));

    }



    public static class EncryptedMessageContainer {
        private String messageEncryptedJson = "";
        private String messageVectorHex = "";
        private Long channelId;
        List<EncryptedUserPasswordsContainer> encryptedPasswords = list();
        private boolean hereMentioned = false;
        private boolean channelMentioned = false;
        private List<String> usersMentioned = list();
        private String title = "";
        private Long threadId = 0L;
        private Long parentMessageId = 0L;

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


        public String getTitle() {
            return title;
        }

        public EncryptedMessageContainer setTitle(String title) {
            this.title = title;
            return this;
        }


        public Long getThreadId() {
            return threadId;
        }

        public EncryptedMessageContainer setThreadId(Long threadId) {
            this.threadId = threadId;
            return this;
        }


        public Long getParentMessageId() {
            return parentMessageId;
        }

        public EncryptedMessageContainer setParentMessageId(Long parentMessageId) {
            this.parentMessageId = parentMessageId;
            return this;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
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
