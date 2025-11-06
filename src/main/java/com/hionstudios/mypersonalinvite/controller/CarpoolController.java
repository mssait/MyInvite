package com.hionstudios.mypersonalinvite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hionstudios.MapResponse;
import com.hionstudios.db.DbTransaction;
import com.hionstudios.iam.IsUser;
import com.hionstudios.mypersonalinvite.Flow.CarpoolFlow;

@RestController
@RequestMapping("api/carpool")
public class CarpoolController {

     private final CarpoolFlow carpoolFlow;

    @Autowired
    public CarpoolController(CarpoolFlow carpoolFlow) {
        this.carpoolFlow = carpoolFlow;
    }

    @PostMapping("event/{id}/add")
    @IsUser
    public ResponseEntity<MapResponse> addCarpool(
            @PathVariable Long id,
            @RequestParam String car_model,
            @RequestParam String car_number,
            @RequestParam String car_color,
            @RequestParam int available_seats,
            @RequestParam boolean ladies_accompanied,
            @RequestParam double start_location_latitude,
            @RequestParam double start_location_longitude,
            @RequestParam String address,
            @RequestParam String start_date_time,
            @RequestParam String end_date_time,
            @RequestParam(required = false) String notes) {
        return ((DbTransaction) () -> carpoolFlow.postCarpool(id, car_model, car_number, car_color,
                available_seats, ladies_accompanied, start_location_latitude, start_location_longitude, address, start_date_time, end_date_time, notes)).write();

    }

    @GetMapping("{id}/details")
    @IsUser
    public ResponseEntity<MapResponse> getCarpoolDetails(@PathVariable Long id) {
        return ((DbTransaction) () -> carpoolFlow.getCarpoolDetails(id)).read();
    }

    @DeleteMapping("{id}/delete")
    @IsUser
    public ResponseEntity<MapResponse> deleteCarpool(@PathVariable Long id) {
        return ((DbTransaction) () -> carpoolFlow.deleteCarpool(id)).write();
    }

    @GetMapping("request/{id}/details")
    @IsUser
    public ResponseEntity<MapResponse> getCarpoolRequestDetails(@PathVariable Long id) {
        return ((DbTransaction) () -> carpoolFlow.viewCarpoolRequest(id)).read();
    }

    @GetMapping("{id}/view-request")
    @IsUser
    public ResponseEntity<MapResponse> viewCarpoolRequest(@PathVariable Long id) {
        return ((DbTransaction) () -> carpoolFlow.viewCarpoolRequestDetails(id)).read();
    }

    @PutMapping("{id}/edit")
    @IsUser
    public ResponseEntity<MapResponse> editCarpool(
            @PathVariable Long id,
            @RequestParam(required = false) String car_model,
            @RequestParam(required = false) String car_number,
            @RequestParam(required = false) String car_color,
            @RequestParam(required = false) int available_seats,
            @RequestParam(required = false) boolean ladies_accompanied,
            @RequestParam(required = false) String start_latitude_location,
            @RequestParam(required = false) String start_longitude_location,
            @RequestParam(required = false) String start_date_time,
            @RequestParam(required = false) String end_date_time,
            @RequestParam(required = false) String notes) {
        return ((DbTransaction) () -> carpoolFlow.putCarpool(id, car_model, car_number, car_color,
                available_seats, ladies_accompanied, start_latitude_location, start_longitude_location, start_date_time, end_date_time, notes)).write();

    }

    @PostMapping("{id}/request")
    @IsUser
    public ResponseEntity<MapResponse> addCarpoolRequest(
            @PathVariable Long id,
            @RequestParam int no_of_people,
            @RequestParam boolean ladies_accompanied,
            @RequestParam(required = false) String notes) {
        return ((DbTransaction) () -> carpoolFlow.postCarpoolRequest(id, no_of_people, ladies_accompanied, notes))
                .write();

    }

    @DeleteMapping("request/{id}/delete")
    @IsUser
    public ResponseEntity<MapResponse> deleteCarpoolRequest(
            @PathVariable Long id) {
        return ((DbTransaction) () -> carpoolFlow.deleteCarpoolRequest(id)).write();
    }

    @PutMapping("request/{id}/edit")
    @IsUser
    public ResponseEntity<MapResponse> editCarpoolRequest(
            @PathVariable Long id,
            @RequestParam int no_of_people,
            @RequestParam boolean ladies_accompanied,
            @RequestParam String notes) {
        return ((DbTransaction) () -> carpoolFlow.putCarpoolRequest(id, no_of_people, ladies_accompanied, notes))
                .write();

    }

    @PutMapping("request/{id}/respond")
    @IsUser
    public ResponseEntity<MapResponse> respondToCarpoolRequest(
            @PathVariable Long id,
            @RequestParam boolean response) {
        return ((DbTransaction) () -> carpoolFlow.respondToCarpoolRequest(id, response)).write();
    }

    @GetMapping("event/{id}")
    @IsUser
    public ResponseEntity<MapResponse> viewCarpool(
            @PathVariable Long id) {
        return ((DbTransaction) () -> carpoolFlow.viewCarpool(id)).read();
    }

    @GetMapping("{id}/guest")
    @IsUser
    public ResponseEntity<MapResponse> viewCarpoolGuests(@PathVariable Long id) {
        return ((DbTransaction) () -> carpoolFlow.viewCarpoolGuests(id)).read();
    }

    @GetMapping("my-carpools")
    @IsUser
    public ResponseEntity<MapResponse> viewMyCarpools() {
        return ((DbTransaction) () -> carpoolFlow.viewMyCarpools()).read();
    }

    @GetMapping("my-requests")
    @IsUser
    public ResponseEntity<MapResponse> viewMyCarpoolRequests() {
        return ((DbTransaction) () -> carpoolFlow.viewMyCarpoolRequests()).read();
    }
}
