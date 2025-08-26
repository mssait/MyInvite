package com.hionstudios.mypersonalinvite.Flow;

import java.util.List;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.iam.UserUtil;
import com.hionstudios.mypersonalinvite.model.Carpool;
import com.hionstudios.mypersonalinvite.model.CarpoolGuest;
import com.hionstudios.mypersonalinvite.model.CarpoolGuestStatus;
import com.hionstudios.mypersonalinvite.model.CarpoolRequest;

public class CarpoolFlow {

    public MapResponse postCarpool(Long event_id, String car_model, String car_number, String car_color,
            int available_seats, boolean ladies_accompanied, String start_location, String start_date_time,
            String end_date_time, String notes) {

        Long user_id = UserUtil.getUserid();
        Carpool carpool = new Carpool();
        carpool.set("event_id", event_id);
        carpool.set("user_id", user_id);
        carpool.set("car_model", car_model);
        carpool.set("car_color", car_color);
        carpool.set("car_number", car_number);
        carpool.set("available_seats", available_seats);
        carpool.set("ladies_accompanied", ladies_accompanied);
        carpool.set("start_location", start_location);
        carpool.set("start_date_time", start_date_time);
        carpool.set("end_date_time", end_date_time);
        carpool.set("notes", notes);

        return carpool.insert() ? MapResponse.success() : MapResponse.failure("Failed to create carpool");
    }

    public MapResponse getCarpoolDetails(Long id) {
        String sql = "Select * From Carpool Where id = ?";
        MapResponse carpoolDetails = Handler.findFirst(sql, id);
        if (carpoolDetails == null) {
            return MapResponse.failure("Carpool not found");
        }
        return new MapResponse().put("carpool", carpoolDetails);
    }

    public MapResponse deleteCarpool(Long id) {
        Carpool carpool = Carpool.findById(id);
        if (carpool != null) {
            carpool.delete();
            return MapResponse.success();
        } else {
            return MapResponse.failure("Carpool not found");
        }
    }

    public MapResponse viewCarpoolRequest(Long id) {
        String sql = "Select * From Carpool_Requests Where id = ?";
        MapResponse requests = Handler.findFirst(sql, id);
        if (requests == null) {
            return MapResponse.failure("Carpool request not found");
        }
        return new MapResponse().put("request", requests);
    }

    public MapResponse viewCarpoolRequestDetails(Long id) {
        String sql = "Select * From Carpool_Requests Where carpool_id = ?";
        List<MapResponse> requests = Handler.findAll(sql, id);
        return new MapResponse().put("requests", requests);
    }

    public MapResponse putCarpool(Long id, String car_model, String car_number, String car_color,
            int available_seats, boolean ladies_accompanied, String start_location, String start_date_time,
            String end_date_time, String notes) {

        Carpool carpool = Carpool.findById(id);
        carpool.set("car_model", car_model);
        carpool.set("car_color", car_color);
        carpool.set("car_number", car_number);
        carpool.set("available_seats", available_seats);
        carpool.set("ladies_accompanied", ladies_accompanied);
        carpool.set("start_location", start_location);
        carpool.set("start_date_time", start_date_time);
        carpool.set("end_date_time", end_date_time);
        carpool.set("notes", notes);

        return carpool.save() ? MapResponse.success() : MapResponse.failure("Failed to create carpool");
    }

    public MapResponse postCarpoolRequest(Long id, String no_of_people, boolean ladies_accompanied,
            String notes) {

        Long user_id = UserUtil.getUserid();
        CarpoolRequest request = new CarpoolRequest();
        request.set("carpool_id", id);
        request.set("guest_id", user_id);
        request.set("no_of_people", no_of_people);
        request.set("ladies_accompanied", ladies_accompanied);
        request.set("notes", notes);
        request.set("carpool_guest_status_id", CarpoolGuestStatus.getId(CarpoolGuestStatus.PENDING));

        return request.insert() ? MapResponse.success() : MapResponse.failure("Failed to create carpool request");
    }

    public MapResponse deleteCarpoolRequest(Long id) {
        CarpoolRequest request = CarpoolRequest.findById(id);
        if (request != null) {
            request.delete();
            return MapResponse.success();
        } else {
            return MapResponse.failure("Carpool request not found");
        }
    }

    public MapResponse putCarpoolRequest(Long id, String no_of_people, boolean ladies_accompanied,
            String notes) {

        CarpoolRequest request = CarpoolRequest.findById(id);
        request.set("no_of_people", no_of_people);
        request.set("ladies_accompanied", ladies_accompanied);
        request.set("notes", notes);

        return request.save() ? MapResponse.success() : MapResponse.failure("Failed to create carpool request");
    }

    public MapResponse respondToCarpoolRequest(Long id, boolean response) {
        CarpoolRequest request = CarpoolRequest.findById(id);
        if (response) {
            request.set("carpool_guest_status_id", CarpoolGuestStatus.getId(CarpoolGuestStatus.ACCEPTED));

            CarpoolGuest guest = new CarpoolGuest();
            guest.set("carpool_id", request.getLong("carpool_id"));
            guest.set("Carpool_Request_id", request.getLong("id"));
            guest.set("carpool_guest_status_id", CarpoolGuestStatus.getId(CarpoolGuestStatus.ACCEPTED));
            guest.insert();

        } else {
            request.set("carpool_guest_status_id", CarpoolGuestStatus.getId(CarpoolGuestStatus.REJECTED));
        }
        return request.saveIt() ? MapResponse.success() : MapResponse.failure("Failed to respond to carpool request");

    }

    public MapResponse viewCarpool(Long event_id) {

        String sql = "Select * From Carpool Where event_id = ?";

        List<MapResponse> carpool = Handler.findAll(sql, event_id);
        MapResponse response = new MapResponse().put("carpool", carpool);
        return response;
    }

    public MapResponse viewCarpoolGuests(Long id) {
        String sql = "Select Carpool_Guests.*, Carpool_Guest_Statuses.Status, Carpool_Requests.*, Users.Username, Users.Full_name From Carpool_Guests Inner Join Carpool_Requests On Carpool_Guests.Carpool_Request_id = Carpool_Requests.ID Inner Join Carpool_Guest_Statuses On Carpool_Guests.Carpool_Guest_Status_id = Carpool_Guest_Statuses.ID Inner Join Users On Carpool_Requests.Guest_id = Users.ID Where Carpool_Guests.Carpool_id = ?";
        List<MapResponse> guests = Handler.findAll(sql, id);
        if (guests.isEmpty()) {
            return MapResponse.failure("No guests found for this carpool");
        }
        return new MapResponse().put("guests", guests);
    }
}
