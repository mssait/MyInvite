package com.hionstudios.mypersonalinvite.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.hionstudios.iam.IsAdmin;
import com.hionstudios.iam.IsAdminAndUser;
import com.hionstudios.iam.IsUser;
import com.hionstudios.mypersonalinvite.Flow.EventFlow;

@RestController
@RequestMapping("api/event")
public class EventController {

    private final EventFlow eventFlow;

    @Autowired
    public EventController(EventFlow eventFlow) {
        this.eventFlow = eventFlow;
    }
    
    @GetMapping("all")
    @IsAdmin
    public ResponseEntity<MapResponse> getAllEvents() {
        return ((DbTransaction) () -> eventFlow.getAllEvents()).read();
    }

    @GetMapping("upcoming")
    @IsAdmin
    public ResponseEntity<MapResponse> getUpcomingEvents() {
        return ((DbTransaction) () -> eventFlow.getUpcomingEvents()).read();
    }

    @GetMapping("completed")
    @IsAdmin
    public ResponseEntity<MapResponse> getCompletedEvents() {
        return ((DbTransaction) () -> eventFlow.getCompletedEvents()).read();
    }

    @GetMapping("{id}/details")
    // @IsAdminAndUser
    @PermitAll
    public ResponseEntity<MapResponse> getEventDetails(@PathVariable Long id) {
        return ((DbTransaction) () -> eventFlow.getEventDetails(id)).read();
    }

    @GetMapping("{id}/guest-event-list")
    @IsAdminAndUser
    public ResponseEntity<MapResponse> getEventGuestList(@PathVariable Long id) {
        return ((DbTransaction) () -> eventFlow.getEventGuestList(id)).read();
    }

    @GetMapping("{id}/todo-list")
    @IsAdminAndUser
    public ResponseEntity<MapResponse> getEventTodoList(@PathVariable Long id) {
        return ((DbTransaction) () -> eventFlow.getEventTodoList(id)).read();
    }

    @GetMapping("{id}/carpool")
    @IsAdminAndUser
    public ResponseEntity<MapResponse> getEventCarpool(@PathVariable Long id) {
        return ((DbTransaction) () -> eventFlow.getEventCarpool(id)).read();
    }

    @GetMapping("{id}/group-chat")
    @IsAdminAndUser
    public ResponseEntity<MapResponse> getEventGroupChat(@PathVariable Long id) {
        return ((DbTransaction) () -> eventFlow.getEventGroupChat(id)).read();
    }

    @PostMapping("add")
    @IsUser
    public ResponseEntity<MapResponse> events(
            @RequestParam int event_type_id, @RequestParam String title,
            @RequestParam String description, @RequestParam int no_of_guest, @RequestParam String date,
            @RequestParam String start_time, @RequestParam String end_time, @RequestParam String address,
            @RequestParam String gift_suggestion, @RequestParam double latitude, @RequestParam double longitude,
            MultipartFile[] thumbnail) {
        return ((DbTransaction) () -> eventFlow.addEvent(event_type_id, title, description, no_of_guest, date,
                start_time, end_time, address, gift_suggestion, latitude, longitude, thumbnail)).write();
    }

    @PutMapping("{id}/edit")
    @IsUser
    public ResponseEntity<MapResponse> editEvent(
            @PathVariable Long id,
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

        return ((DbTransaction) () -> eventFlow.editEvent(
                id, event_type_id, title, description, no_of_guest, date, start_time, end_time,
                address, gift_suggestion, latitude, longitude, thumbnail)).write();
    }

    @GetMapping("owner/upcoming")
    @IsUser
    public ResponseEntity<MapResponse> getEventsForOwners() {
        return ((DbTransaction) () -> eventFlow.getUpcomingEventsForOwners()).read();
    }

    @GetMapping("owner/completed")
    @IsUser
    public ResponseEntity<MapResponse> getCompletedEventsForOwners() {
        return ((DbTransaction) () -> eventFlow.getCompletedEventsForOwners()).read();
    }

    @GetMapping("owner/all")
    @IsUser
    public ResponseEntity<MapResponse> getAllEventsForOwners() {
        return ((DbTransaction) () -> eventFlow.getAllEventsForOwners()).read();
    }

    @DeleteMapping("{id}/delete")
    @IsUser
    public ResponseEntity<MapResponse> deleteEvent(@PathVariable Long id) {
        return ((DbTransaction) () -> eventFlow.deleteEvent(id)).write();
    }

    @GetMapping("invited-to/upcoming")
    public ResponseEntity<MapResponse> getInvitedToUpcomingEvents() {
        return ((DbTransaction) () -> eventFlow.guestUpcomingEvents()).read();
    }

    @GetMapping("invited-to/completed")
    public ResponseEntity<MapResponse> getInvitedToCompletedEvents() {
        return ((DbTransaction) () -> eventFlow.guestCompletedEvents()).read();
    }

    @GetMapping("{id}/details/invited-to")
    // @IsUser
    public ResponseEntity<MapResponse> getInvitedToEventDetails(@PathVariable Long id) {
        return ((DbTransaction) () -> eventFlow.getInvitedToEventDetails(id)).read();
    }

    @DeleteMapping("{id}/delete-guest/{guestId}")
    @IsUser
    public ResponseEntity<MapResponse> deleteGuestList(@PathVariable Long id, @PathVariable Long guestId) {
        return ((DbTransaction) () -> eventFlow.deleteGuestList(id, guestId)).write();
    }

    @PutMapping("{id}/update-rsvp")
    @IsUser
    public ResponseEntity<MapResponse> updateRsvp(@PathVariable Long id, @RequestParam Long rsvp) {
        return ((DbTransaction) () -> eventFlow.updateRsvp(id, rsvp)).write();
    }

    @GetMapping("{id}/carpool-list")
    @IsUser
    public ResponseEntity<MapResponse> getCarpoolList(@PathVariable Long id) {
        return ((DbTransaction) () -> eventFlow.getCarpoolList(id)).read();
    }

    @GetMapping("{id}/view-budget")
    // @IsUser
    public ResponseEntity<MapResponse> viewBudget(@PathVariable Long id) {
        return ((DbTransaction) () -> eventFlow.viewBudget(id)).read();
    }

    @GetMapping("{id}/invited-list")
    @IsUser
    public ResponseEntity<MapResponse> getInvitedList(@PathVariable Long id) {
        return ((DbTransaction) () -> eventFlow.getInvitedList(id)).read();
    }

    @PostMapping("{id}/add-guest-list")
    // @IsUser
    public ResponseEntity<MapResponse> addEventGuest(
            @PathVariable Long id,
            @RequestParam(required = false) List<String> emailList,
            @RequestBody(required = false) List<Map<String, String>> guestList) {
            return ((DbTransaction) () -> eventFlow.postEventGuest(id, emailList, guestList)).write();
    }

    @PutMapping("guest/{id}/rsvp")
    public ResponseEntity<MapResponse> rsvpEvent(
            @PathVariable Long id,
            @RequestParam String rsvp,
            @RequestParam int no_of_attendees,
            @RequestParam(required = false) String comment,
            @RequestParam boolean carpool_expecting,
            @RequestParam(required = false) Long carpool_guest_status_id) {
        return ((DbTransaction) () -> eventFlow.rsvpEvent(id, rsvp, no_of_attendees, comment, carpool_expecting,
                carpool_guest_status_id)).write();
    }

    @PostMapping("{id}/add-budget")
    @IsUser
    public ResponseEntity<MapResponse> addBudget(
            @PathVariable Long id,
            @RequestParam Long budget_type_id,
            @RequestParam Long planned_amount,
            @RequestParam Long actual_amount) {
        return ((DbTransaction) () -> eventFlow.addBudget(id, budget_type_id, planned_amount, actual_amount))
                .write();
    }

    @PutMapping("edit-budget/{id}")
    @IsUser
    public ResponseEntity<MapResponse> updateBudget(
            @PathVariable Long id,
            @RequestParam(required = false) Long budget_type_id,
            @RequestParam(required = false) Long planned_amount,
            @RequestParam(required = false) Long actual_amount) {
        return ((DbTransaction) () -> eventFlow.updateBudget(id, budget_type_id, planned_amount, actual_amount))
                .write();
    }

    @DeleteMapping("delete-budget/{id}")
    @IsUser
    public ResponseEntity<MapResponse> deleteBudget(@PathVariable Long id) {
        return ((DbTransaction) () -> eventFlow.deleteBudget(id)).write();
    }

    @GetMapping("{id}/guest-list")
    @IsUser
    public ResponseEntity<MapResponse> getGuestList(@PathVariable Long id) {
        return ((DbTransaction) () -> eventFlow.getGuestList(id)).read();
    }

    @GetMapping("types")
    @IsUser
    public ResponseEntity<MapResponse> getEventTypes() {
        return ((DbTransaction) () -> eventFlow.getEventTypes()).read();
    }

    @GetMapping("rsvp")
    @IsUser
    public ResponseEntity<MapResponse> getRsvpStatuses() {
        return ((DbTransaction) () -> eventFlow.getRsvpStatuses()).read();
    }

    @GetMapping("budget-types")
    @IsUser
    public ResponseEntity<MapResponse> getBudgetTypes() {
        return ((DbTransaction) () -> eventFlow.getBudgetTypes()).read();
    }

}
