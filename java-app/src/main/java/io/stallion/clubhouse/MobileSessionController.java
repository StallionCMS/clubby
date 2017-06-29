package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.services.Log;


public class MobileSessionController extends StandardModelController<MobileSession> {
    public static MobileSessionController instance() {
        return (MobileSessionController) DataAccessRegistry.instance().get("sch_mobile_sessions");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(MobileSession.class, MobileSessionController.class, false);
    }
}
