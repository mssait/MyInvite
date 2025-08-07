package com.hionstudios.mypersonalinvite.controller;

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

    @PostMapping("event/{id}/add")
    @IsUser
    public ResponseEntity<MapResponse> addCarpool(
            @PathVariable int event_id,
            @RequestParam String car_model,
            @RequestParam String car_number,
            @RequestParam String car_color,
            @RequestParam int available_seats,
            @RequestParam boolean ladies_accompanied,
            @RequestParam String start_location,
            @RequestParam String start_date_time,
            @RequestParam String end_date_time,
            @RequestParam(required = false) String notes) {
        return ((DbTransaction) () -> new CarpoolFlow().postCarpool(event_id, car_model, car_number, car_color,
                available_seats, ladies_accompanied, start_location, start_date_time, end_date_time, notes)).write();

    }

    @PutMapping("{id}/edit")
    @IsUser
    public ResponseEntity<MapResponse> editCarpool(
            @PathVariable int id,
            @RequestParam(required = false) String car_model,
            @RequestParam(required = false) String car_number,
            @RequestParam(required = false) String car_color,
            @RequestParam(required = false) int available_seats,
            @RequestParam(required = false) boolean ladies_accompanied,
            @RequestParam(required = false) String start_location,
            @RequestParam(required = false) String start_date_time,
            @RequestParam(required = false) String end_date_time,
            @RequestParam(required = false) String notes) {
        return ((DbTransaction) () -> new CarpoolFlow().putCarpool(id, car_model, car_number, car_color,
                available_seats, ladies_accompanied, start_location, start_date_time, end_date_time, notes)).write();

    }

    @PostMapping("{id}/request")
    @IsUser
    public ResponseEntity<MapResponse> addCarpoolRequest(
            @PathVariable int id,
            @RequestParam String no_of_people,
            @RequestParam boolean ladies_accompanied,
            @RequestParam String notes) {
        return ((DbTransaction) () -> new CarpoolFlow().postCarpoolRequest(id, no_of_people, ladies_accompanied, notes))
                .write();

    }

    @DeleteMapping("request/{id}/delete")
    @IsUser
    public ResponseEntity<MapResponse> deleteCarpoolRequest(
            @PathVariable int id) {
        return ((DbTransaction) () -> new CarpoolFlow().deleteCarpoolRequest(id)).write();
    }

    @PutMapping("request/{id}/edit")
    @IsUser
    public ResponseEntity<MapResponse> editCarpoolRequest(
            @PathVariable int id,
            @RequestParam String no_of_people,
            @RequestParam boolean ladies_accompanied,
            @RequestParam String notes) {
        return ((DbTransaction) () -> new CarpoolFlow().putCarpoolRequest(id, no_of_people, ladies_accompanied, notes))
                .write();

    }

    @PutMapping("request/{id}/respond")
    @IsUser
    public ResponseEntity<MapResponse> respondToCarpoolRequest(
            @PathVariable int id,
            @RequestParam boolean response) {
        return ((DbTransaction) () -> new CarpoolFlow().respondToCarpoolRequest(id, response)).write();
    }

    @GetMapping("event/{id}")
    @IsUser
    public ResponseEntity<MapResponse> viewCarpool(
            @PathVariable int event_id) {
        return ((DbTransaction) () -> new CarpoolFlow().viewCarpool(event_id)).read();
    }

}
