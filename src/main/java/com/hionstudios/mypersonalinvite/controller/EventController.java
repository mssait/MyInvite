package com.hionstudios.mypersonalinvite.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
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

    @PostMapping("{id}/add-event-guest")
    @IsUser
    public ResponseEntity<MapResponse> addEventGuest(
            @PathVariable int id,
            @RequestBody List<Map<String, String>> guestList) {
        return ((DbTransaction) () -> new EventFlow().postEventGuest(id, guestList)).write();
    }

    @PutMapping("{id}/rsvp")
    public ResponseEntity <MapResponse> rsvpEvent(
        @PathVariable int id,
        @RequestParam String rsvp,
        @RequestParam(required = false) int no_of_attendees,
        @RequestParam(required = false) String comment,
        @RequestParam(required = false) boolean carpool_expecting,
        @RequestParam(required = false) Long carpool_guest_status_id){
            return((DbTransaction) () -> new EventFlow().rsvpEvent(id, rsvp, no_of_attendees, comment, carpool_expecting, carpool_guest_status_id)).write();
        }

}
