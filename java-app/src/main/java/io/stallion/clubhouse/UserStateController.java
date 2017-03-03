package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.clubhouse.webSockets.WebSocketEventHandler;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.dataAccess.db.DB;
import io.stallion.services.Log;


public class UserStateController extends StandardModelController<UserState> {
    public static UserStateController instance() {
        return (UserStateController) DataAccessRegistry.instance().get("sch_user_states");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(UserState.class, UserStateController.class, true);
    }

    public void updateState(Long userId, UserStateType stateType) {
        DB.instance().execute("INSERT INTO sch_user_states (id, userId, `state`, deleted) VALUES(?, ?, ?, 0) ON DUPLICATE KEY UPDATE `state`=VALUES(`state`) ",
                DB.instance().getTickets().nextId(), userId, stateType.toString()
        );
        WebSocketEventHandler.notifyUserStateChange(userId, stateType);
    }
}
