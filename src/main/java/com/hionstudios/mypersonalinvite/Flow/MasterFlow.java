package com.hionstudios.mypersonalinvite.Flow;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.mypersonalinvite.model.BudgetType;
import com.hionstudios.mypersonalinvite.model.EventType;

public class MasterFlow {

    public MapResponse getEventTypes() {

        String sql = "Select * From Event_Types";
        return Handler.toDataGrid(sql);
    }

    public MapResponse postEventTypes(String event_type) {

        EventType type = new EventType();
        type.set("event_type", event_type);
        return type.insert() ? MapResponse.success() : MapResponse.failure();
    }

    public MapResponse putEventTypes(int id, String event_type) {

        EventType type = EventType.findById(id);
        type.set("event_type", event_type);
        return type.save() ? MapResponse.success() : MapResponse.failure();
    }

    public MapResponse getBudgetTypes() {

        String sql = "Select * From Budget_Types";
        return Handler.toDataGrid(sql);
    }

    public MapResponse postBudgetTypes(String budget_type) {

        BudgetType type = new BudgetType();
        type.set("budget_type", budget_type);
        return type.insert() ? MapResponse.success() : MapResponse.failure();
    }

    public MapResponse putBudgetTypes(int id, String budget_type) {

        BudgetType type = BudgetType.findById(id);
        type.set("budget_type", budget_type);
        return type.save() ? MapResponse.success() : MapResponse.failure();
    }

}
