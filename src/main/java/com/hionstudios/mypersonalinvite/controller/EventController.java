package com.hionstudios.mypersonalinvite.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hionstudios.MapResponse;
import com.hionstudios.MixMultipartFileAndString;
import com.hionstudios.db.DbTransaction;
import com.hionstudios.iam.IsUser;
import com.hionstudios.mypersonalinvite.Flow.EventFlow;

@RestController
@RequestMapping("api/event")
public class EventController {

    @PostMapping("add")
    @IsUser
    public ResponseEntity<MapResponse> events(
            @RequestParam int event_type_id, @RequestParam String title,
            @RequestParam String description, @RequestParam int no_of_guest, @RequestParam String date,
            @RequestParam String start_time, @RequestParam String end_time, @RequestParam String address,
            @RequestParam String gift_suggestion, @RequestParam double latitude, @RequestParam double longitude,
            MultipartFile[] thumbnail) {
        return ((DbTransaction) () -> new EventFlow().addEvent(event_type_id, title, description, no_of_guest, date,
                start_time, end_time, address, gift_suggestion, latitude, longitude, thumbnail)).write();
    }

    @PutMapping("{id}/edit")
    @IsUser
    public ResponseEntity<MapResponse> editEvent(
            @PathVariable int id,
            @RequestParam int event_type_id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam int no_of_guest,
            @RequestParam String date,
            @RequestParam String start_time,
            @RequestParam String end_time,
            @RequestParam String address,
            @RequestParam String gift_suggestion,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @MixMultipartFileAndString @RequestParam(required = false) List<Object> thumbnail) {

        return ((DbTransaction) () -> new EventFlow().editEvent(
                id, event_type_id, title, description, no_of_guest, date, start_time, end_time,
                address, gift_suggestion, latitude, longitude, thumbnail)).write();
    }

    @GetMapping("owner")
    @IsUser
    public ResponseEntity<MapResponse> getEventsForOwners() {
        return ((DbTransaction) () -> new EventFlow().getEventsForOwners()).write();
    }

    @PostMapping("{id}/add-guest-list")
    @IsUser
    public ResponseEntity<MapResponse> addEventGuest(
            @PathVariable int id,
            @RequestParam(required = false) List<String> emailList,
            @RequestBody(required = false) List<Map<String, String>> guestList) {
        return ((DbTransaction) () -> new EventFlow().postEventGuest(id, emailList,  guestList)).write();
    }

    @PutMapping("{id}/rsvp")
    public ResponseEntity<MapResponse> rsvpEvent(
            @PathVariable int id,
            @RequestParam String rsvp,
            @RequestParam int no_of_attendees,
            @RequestParam(required = false) String comment,
            @RequestParam boolean carpool_expecting,
            @RequestParam(required = false) Long carpool_guest_status_id) {
        return ((DbTransaction) () -> new EventFlow().rsvpEvent(id, rsvp, no_of_attendees, comment, carpool_expecting,
                carpool_guest_status_id)).write();
    }

    @PostMapping("{id}/add-budget")
    @IsUser
    public ResponseEntity<MapResponse> addBudget(
            @PathVariable long id,
            @RequestParam long budget_type_id,
            @RequestParam String amount,
            @RequestParam(required = false) String description) {
        return ((DbTransaction) () -> new EventFlow().addBudget(id, budget_type_id, amount, description)).write();
    }

    @PutMapping("edit-budget/{id}")
    @IsUser
    public ResponseEntity<MapResponse> updateBudget(
            @PathVariable long id,
            @RequestParam(required = false) String amount,
            @RequestParam(required = false) Long budget_type_id,
            @RequestParam(required = false) String description ) {
        return ((DbTransaction) () -> new EventFlow().updateBudget(id, budget_type_id, amount, description)).write();
    }

    @DeleteMapping("delete-budget/{id}")
    @IsUser
    public ResponseEntity<MapResponse> deleteBudget(@PathVariable long id) {
        return ((DbTransaction) () -> new EventFlow().deleteBudget(id)).write();
    }

    @GetMapping("upcoming")
    @IsUser
    public ResponseEntity<MapResponse> getUpcomingEvents(){
        return ((DbTransaction) () -> new EventFlow().getUpcomingEvents()).read();
    }

    @GetMapping("completed")
    @IsUser
    public ResponseEntity<MapResponse> getCompletedEvents(){
        return ((DbTransaction) () -> new EventFlow().getCompletedEvents()).read();
    }

    @GetMapping("{id}/guest-list")
    @IsUser
    public ResponseEntity<MapResponse> getGuestList(@PathVariable long id){
        return ((DbTransaction) () -> new EventFlow().getGuestList(id)).read();
    }

}
