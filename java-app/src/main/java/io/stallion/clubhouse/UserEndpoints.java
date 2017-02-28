package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.db.DB;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.MinRole;
import io.stallion.services.Log;
import io.stallion.users.Role;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/clubhouse-api/users")
@Produces("application/json")
@MinRole(Role.MEMBER)
public class UserEndpoints implements EndpointResource {
    @GET
    @Path("/all-users")
    public Object allUsers() {
        // ChannelUserWrapper
        List<ChannelUserWrapper> users = DB.instance().queryBean(
                ChannelUserWrapper.class,
                "" +
                        " SELECT su.id, su.displayName, su.email, su.username, up.aboutMe, up.webSite, up.publicKeyHex " +
                        " FROM stallion_users AS su" +
                        " INNER JOIN sch_user_profiles as up ON up.userId=su.id "
        );
        Map<String, Object> ctx = map();
        ctx.put("users", users);
        return ctx;
    }
}
