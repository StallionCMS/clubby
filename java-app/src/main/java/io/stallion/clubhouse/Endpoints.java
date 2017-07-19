package io.stallion.clubhouse;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.amazonaws.util.Md5Utils;
import io.stallion.dataAccess.db.DB;
import io.stallion.exceptions.ClientException;
import io.stallion.requests.ResponseComplete;
import io.stallion.requests.ServletFileSender;
import io.stallion.restfulEndpoints.EndpointsRegistry;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import io.stallion.Context;
import io.stallion.users.IUser;
import io.stallion.users.UserController;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.Sanitize;
import io.stallion.utils.json.JSON;
import org.apache.commons.lang3.StringUtils;

import static io.stallion.utils.Literals.*;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;


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
        Context.getResponse().getMeta().setTitle("Clubhouse");

        if (Context.getUser().isAnon()) {
            if (ChannelController.instance().getStash().getItems().size() == 0) {
                isFirstUser = true;
            }
        }

        Map ctx = map(
            val("pluginName", "clubhouse"),
            val("theApplicationContextJson", Sanitize.htmlSafeJson(
                    map(
                            val("isFirstUser", isFirstUser),
                            val("site", map(
                                    val("siteUrl", Settings.instance().getSiteUrl()),
                                    val("cdnUrl", Settings.instance().getCdnUrl()),
                                    val("name", AdminSettings.getSiteName()),
                                    val("logo", AdminSettings.getIconUrl())
                            )),
                            val("user", Context.getUser()),
                            val("profile", profile),
                            val("defaultChannelId", defaultChannelId)
                    )
            ))
        );
        return TemplateRenderer.instance().renderTemplate("clubhouse:app.jinja", ctx);
    }



    @GET
    @Path("/clubhouse-api/general/poll-for-mentions")
    public Object pollForMentions() {
        Map ctx = map();
        Long mentions = DB.instance().queryScalar("SELECT COUNT(*) as mentions FROM sch_user_messages " +
                " WHERE `mentioned`=1 AND `read`=0 AND userId=? ", Context.getUser().getId());
        List<Long> unread = DB.instance().queryColumn(
                "SELECT id FROM sch_users_messages WHERE `read`=0 AND userId=? LIMIT 1 ",
                Context.getUser().getId());
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
                " SELECT c.id, c.name, c.allowReactions, c.displayEmbeds, c.channelType, c.directMessageUserIds, c.encrypted, cm.owner, cm.canPost, cm.favorite " +
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

        // Get mention count by channel
        List<Map<String, Object>> records = DB.instance().findRecords("" +
                "SELECT channelId, COUNT(*) as mentions FROM sch_user_messages " +
                " WHERE `mentioned`=1 AND `read`=0 AND userId=? " +
                " GROUP BY channelId;",
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
                        " WHERE " +
                        "     `read`=0 AND userId=? AND sch_messages.deleted=0 " +
                        "  AND sch_user_messages.deleted=0 AND sch_messages.fromUserId!=? " +
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
                        " SELECT su.id, su.displayName, su.email, su.username, up.aboutMe, up.webSite, " +
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
        return TemplateRenderer.instance().renderTemplate("clubhouse:oembed.jinja", ctx);
    }



    @GET
    @Path("/emoji-test")
    @Produces("text/html")
    public Object emojiTest() {
        Map ctx = map();
        return TemplateRenderer.instance().renderTemplate("clubhouse:emoji.jinja", ctx);
    }

    @GET
    @Path("/hello-world")
    @Produces("text/html")
    public Object hello() {
        return "Hello, world.";
    }

}

