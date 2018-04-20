package io.clubby.server;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.stallion.dataAccess.db.DB;
import io.stallion.requests.ResponseComplete;
import io.stallion.requests.ServletFileSender;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.MinRole;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import io.stallion.Context;
import io.stallion.users.IUser;
import io.stallion.users.Role;
import io.stallion.users.UserController;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.Sanitize;
import io.stallion.utils.json.JSON;

import static io.stallion.utils.Literals.*;

import javax.ws.rs.*;


public class Endpoints implements EndpointResource {


    @GET
    @Path("/")
    @Produces("text/html")
    public Object app() {
        UserProfile profile = new UserProfile();
        Long defaultChannelId = 0L;
        boolean isFirstUser = false;


        Context.getResponse().getMeta().setBodyCssId("clubhouse-body");

        // Check for auth passed in via query string, used by mobile and desktop apps
        String newAuthToken = Context.getRequest().getQueryParams().getOrDefault("mobileAuthCookie", "");
        if (!empty(newAuthToken)) {
            UserController.UserValetResult result = UserController.instance().cookieStringToUser(newAuthToken);
            if (result != null && result.getUser() != null && !empty(result.getSessionKey())) {
                MobileSession mb = MobileSessionController
                        .instance()
                        .forUniqueKey("sessionKey", result.getSessionKey());
                if (mb != null && mb.getUserId().equals(result.getUser().getId())) {
                    Context.getResponse().addCookie("stUserSession", newAuthToken);
                    Context.setUser(result.getUser());
                }
            }

        }




        if (!Context.getUser().isAnon()) {
            profile = UserProfileController.instance().forUniqueKey("userId", Context.getUser().getId());
            defaultChannelId = ChannelController.instance().getFirstUserChannel(Context.getUser().getId());
        }
        Context.getResponse().getMeta().setTitle("Clubby");

        if (Context.getUser().isAnon()) {
            if (ChannelController.instance().getStash().getItems().size() == 0) {
                isFirstUser = true;
            }
        }

        Map ctx = map(
            val("pluginName", "clubby"),
            val("theApplicationContextJson", Sanitize.htmlSafeJson(
                    map(
                            val("isFirstUser", isFirstUser),
                            val("site", map(
                                    val("siteUrl", Settings.instance().getSiteUrl()),
                                    val("cdnUrl", Settings.instance().getCdnUrl()),
                                    val("name", ClubbyDynamicSettings.getSiteName()),
                                    val("logo", ClubbyDynamicSettings.getIconUrl()),
                                    val("iconBase64", ClubbyDynamicSettings.getIconBase64())
                            )),
                            val("user", Context.getUser()),
                            val("profile", profile),
                            val("defaultChannelId", defaultChannelId)
                    )
            ))
        );
        return TemplateRenderer.instance().renderTemplate("clubby:app.jinja", ctx);
    }



    @POST
    @Path("/clubhouse-api/general/poll-for-mentions")
    @MinRole(Role.MEMBER)
    public Object pollForMentions() {
        Map ctx = map();
        Long mentions = DB.instance().queryScalar("" +
                " SELECT COUNT(*) as mentions FROM sch_user_messages as um " +
                "   INNER JOIN sch_messages AS m ON m.id=um.messageId " +
                "   INNER JOIN sch_channels AS c ON c.id=m.channelId" +
                "   INNER JOIN sch_channel_members AS cm ON cm.channelId=c.id AND cm.userId=um.userId " +
                "   LEFT OUTER JOIN sch_messages AS tm ON m.threadId=tm.id " +
                " WHERE `mentioned`=1 AND `read`=0 AND um.userId=? AND cm.userId=? AND m.deleted=0 AND um.deleted=0 AND c.deleted=0 AND cm.deleted=0 AND (m.threadId IS NULL OR m.threadId=0 OR tm.deleted=0) ",
                Context.getUser().getId(), Context.getUser().getId());
        List<Long> unread = DB.instance().queryColumn("" +
                        " SELECT m.id FROM sch_user_messages as um " +
                        "   INNER JOIN sch_messages AS m ON m.id=um.messageId " +
                        "   INNER JOIN sch_channels AS c ON c.id=m.channelId" +
                        "   INNER JOIN sch_channel_members AS cm ON cm.channelId=c.id AND cm.userId=um.userId " +
                        "   LEFT OUTER JOIN sch_messages AS tm ON m.threadId=tm.id " +
                        " WHERE `read`=0 AND um.userId=? AND cm.userId=? AND m.deleted=0 AND um.deleted=0 AND c.deleted=0 AND cm.deleted=0 AND (m.threadId IS NULL OR m.threadId=0 OR tm.deleted=0)" +
                "  LIMIT 1 ",
                Context.getUser().getId(), Context.getUser().getId());

        ctx.put("mentionCount", mentions);
        ctx.put("hasNew", unread.size() > 0);

        return ctx;

    }

    @GET
    @Path("/clubhouse-api/general-context")
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
                " SELECT c.id, c.name, c.allowReactions, c.displayEmbeds, c.channelType, c.directMessageUserIds, c.encrypted, cm.owner, cm.canPost, cm.favorite, c.inviteOnly " +
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
                        if (user == null) {
                            name = "";
                            break;
                        } else {
                            String personName = or(user.getDisplayName(), user.getUsername());
                            if (!empty(name)) {
                                name += ", ";
                            }
                            name += personName;
                        }
                    }
                    cc.setName(name);
                }
                if (empty(cc.getName())) {
                    continue;
                }
                directMessageChannels.add(cc);

            }
        }
        ctx.put("standardChannels", standardChannels);
        ctx.put("directMessageChannels", directMessageChannels);
        ctx.put("forumChannels", forumChannels);


        /*
                Long mentions = DB.instance().queryScalar("" +
                " SELECT COUNT(*) as mentions FROM sch_user_messages as um " +
                "   INNER JOIN sch_messages AS m ON m.id=um.messageId " +
                "   INNER JOIN sch_channels AS c ON c.id=m.channelId" +
                "   INNER JOIN sch_channel_members AS cm ON cm.channelId=c.id AND cm.userId=um.userId " +
                "   LEFT OUTER JOIN sch_messages AS tm ON m.threadId=tm.id " +
                " WHERE `mentioned`=1 AND `read`=0 AND um.userId=? AND cm.userId=? AND m.deleted=0
                        AND um.deleted=0 AND c.deleted=0 AND cm.deleted=0
                        AND (m.threadId IS NULL OR m.threadId=0 OR tm.deleted=0) ",
                Context.getUser().getId(), Context.getUser().getId());
         */

        // Get mention count by channel
        List<Map<String, Object>> records = DB.instance().findRecords("" +
                "SELECT m.channelId, COUNT(*) as mentions FROM sch_user_messages AS um " +
                " INNER JOIN sch_messages AS m ON m.id=um.messageId  " +
                " LEFT OUTER JOIN sch_messages AS tm ON m.threadId=tm.id " +
                "WHERE um.`mentioned`=1 AND um.`read`=0 AND um.userId=? " +
                "  AND m.deleted=0 AND um.deleted=0 " +
                "  AND m.fromUserId!=? " +
                "  AND (m.threadId IS NULL OR m.threadId=0 OR tm.deleted=0) " +
                " GROUP BY channelId;",
                Context.getUser().getId(),
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
                        " SELECT DISTINCT(m.channelId) FROM sch_user_messages AS um " +
                        " INNER JOIN sch_messages AS m ON um.messageId=m.id " +
                         "   LEFT OUTER JOIN sch_messages AS tm ON m.threadId=tm.id " +
                        " WHERE " +
                        "     `read`=0 AND um.userId=? AND m.deleted=0 " +
                        "  AND m.fromUserId!=? " +
                        " AND (m.threadId IS NULL OR m.threadId=0 OR tm.deleted=0) " +
                        " GROUP BY channelId; ",
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
                        " SELECT su.id, su.displayName, su.email, su.username, up.aboutMe, up.webSite, su.approved, " +
                        "        up.publicKeyJwkJson, us.state, up.avatarUrl " +
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


    @GET
    @Path("/auto-logo-icon.png")
    @Produces("image/png")
    public void viewIcon() throws IOException {

        File file = new IconHelper().getOrCreateAutoIcon();
        sendFile(file);

    }

    private void sendFile(File file) {
        FileInputStream stream;
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        new ServletFileSender(
                Context.getRequest(),
                Context.getResponse()
        ).sendAssetResponse(stream, file.lastModified(), file.length(), file.getAbsolutePath());
        throw new ResponseComplete();

    }


    @GET
    @Path("/oembed-iframe")
    @Produces("text/html")
    public Object oembedIframe() {
        Map ctx = map(
                val("embedUrl", JSON.stringify(Context.getRequest().getQueryParams().getOrDefault("embedUrl", ""))),
                val("iframeId", JSON.stringify(Context.getRequest().getQueryParams().getOrDefault("iframeId", "")))
        );
        return TemplateRenderer.instance().renderTemplate("clubby:oembed.jinja", ctx);
    }



    @GET
    @Path("/emoji-test")
    @Produces("text/html")
    public Object emojiTest() {
        Map ctx = map();
        return TemplateRenderer.instance().renderTemplate("clubby:emoji.jinja", ctx);
    }

    @GET
    @Path("/hello-world")
    @Produces("text/html")
    public Object hello() {
        return "Hello, world.";
    }

}

