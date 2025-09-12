package com.hionstudios.mypersonalinvite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hionstudios.MapResponse;
import com.hionstudios.db.DbTransaction;
import com.hionstudios.iam.IsAdmin;
import com.hionstudios.mypersonalinvite.Flow.MasterFlow;

@RestController
@RequestMapping("api/master")
public class MasterController {

    @GetMapping("/event-types")
    // @IsAdmin
    public ResponseEntity<MapResponse> eventTypes() {
        return ((DbTransaction) () -> new MasterFlow().getEventTypes()).read();
    }

    @PostMapping("add-event-types")
    // @IsAdmin
    public ResponseEntity<MapResponse> addEventTypes(@RequestParam String event_type) {
        return ((DbTransaction) () -> new MasterFlow().postEventTypes(event_type)).read();
    }

    @PutMapping("/edit-event-types/{id}")
    // @IsAdmin
    public ResponseEntity<MapResponse> editEventTypes(@PathVariable int id, @RequestParam String event_type) {
        return ((DbTransaction) () -> new MasterFlow().putEventTypes(id, event_type)).read();
    }

    @GetMapping("/budget-types")
    // @IsAdmin
    public ResponseEntity<MapResponse> budgetTypes() {
        return ((DbTransaction) () -> new MasterFlow().getBudgetTypes()).read();
    }

    @PostMapping("/add-budget-types")
    // @IsAdmin
    public ResponseEntity<MapResponse> addBudgetTypes(@RequestParam String budget_type) {
        return ((DbTransaction) () -> new MasterFlow().postBudgetTypes(budget_type)).read();
    }

    @PutMapping("/edit-budget-types/{id}")
    // @IsAdmin
    public ResponseEntity<MapResponse> editBudgetTypes(@PathVariable int id, @RequestParam String budget_type) {
        return ((DbTransaction) () -> new MasterFlow().putBudgetTypes(id, budget_type)).read();
    }

}
