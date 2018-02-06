package io.clubby.server;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;


public class MobileSessionController extends StandardModelController<MobileSession> {
    public static MobileSessionController instance() {
        return (MobileSessionController) DataAccessRegistry.instance().get("sch_mobile_sessions");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(MobileSession.class, MobileSessionController.class, false);
    }
}
