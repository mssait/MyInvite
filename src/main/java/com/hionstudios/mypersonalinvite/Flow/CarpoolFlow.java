package com.hionstudios.mypersonalinvite.Flow;

import java.util.List;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.iam.UserUtil;
import com.hionstudios.mypersonalinvite.model.Carpool;
import com.hionstudios.mypersonalinvite.model.CarpoolGuest;
import com.hionstudios.mypersonalinvite.model.CarpoolGuestStatus;
import com.hionstudios.mypersonalinvite.model.CarpoolRequest;
import com.hionstudios.mypersonalinvite.model.Notification;
import com.hionstudios.mypersonalinvite.model.NotificationType;
import com.hionstudios.mypersonalinvite.model.User;
import com.hionstudios.time.TimeUtil;

public class CarpoolFlow {

    public MapResponse postCarpool(Long id, String car_model, String car_number, String car_color,
            int available_seats, boolean ladies_accompanied, double start_location_latitude,
            double start_location_longitude, String address, String start_date_time,
            String end_date_time, String notes) {

        Long user_id = UserUtil.getUserid();

        Long parsedStartDate = TimeUtil.parse(start_date_time, "dd-MM-yyyy HH:mm:ss a");
        Long parsedEndDate = TimeUtil.parse(end_date_time, "dd-MM-yyyy HH:mm:ss a");

        // DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        // LocalTime parsedStartTime = LocalTime.parse(start_date_time, timeFormatter);
        // long startTimeMillis = parsedStartTime.toSecondOfDay() * 1000L;

        // LocalTime parsedEndTime = LocalTime.parse(end_date_time, timeFormatter);
        // long endTimeMillis = parsedEndTime.toSecondOfDay() * 1000L;

        Carpool carpool = new Carpool();
        carpool.set("event_id", id);
        carpool.set("user_id", user_id);
        carpool.set("car_model", car_model);
        carpool.set("car_color", car_color);
        carpool.set("car_number", car_number);
        carpool.set("available_seat", available_seats);
        carpool.set("ladies_accompanied", ladies_accompanied);
        carpool.set("address", address);
        carpool.set("start_location_latitude", start_location_latitude);
        carpool.set("start_location_longitude", start_location_longitude);
        carpool.set("start_date_time", parsedStartDate);
        carpool.set("end_date_time", parsedEndDate);
        carpool.set("notes", notes);

        return carpool.insert() ? MapResponse.success() : MapResponse.failure("Failed to create carpool");
    }

    public MapResponse getCarpoolDetails(Long id) {
        String sql = "Select Carpools.*, Users.Name, Users.Profile_Pic, Events.Address As Event_Address, Events.Location_Latitude As end_location_latitude, Events.Location_Longitude As end_location_longitude From Carpools Join Users On Users.Id = Carpools.User_Id Join Events On Events.Id = Carpools.Event_Id Where Carpools.Id = ?";
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
        String sql = "Select Carpool_Requests.*, Users.Name, Users.Profile_Pic From Carpool_Requests Join Users On Carpool_Requests.Guest_Id = Users.Id Where Carpool_Requests.Id = ?";
        MapResponse requests = Handler.findFirst(sql, id);
        if (requests == null) {
            return MapResponse.failure("Carpool request not found");
        }
        return new MapResponse().put("request", requests);
    }

    public MapResponse viewCarpoolRequestDetails(Long id) {
        String sql = "Select Carpool_Requests.*, Users.Name, Users.Profile_Pic From Carpool_Requests Join Users On Carpool_Requests.Guest_Id = Users.Id Where carpool_id = ?";
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

    public MapResponse postCarpoolRequest(Long id, int no_of_people, boolean ladies_accompanied,
            String notes) {

        Long user_id = UserUtil.getUserid();

        Carpool carpool = Carpool.findById(id);
        if (carpool == null)
            return MapResponse.failure("Carpool not found");

        CarpoolRequest request = new CarpoolRequest();
        request.set("carpool_id", id);
        request.set("guest_id", user_id);
        request.set("no_of_people", no_of_people);
        request.set("ladies_accompanied", ladies_accompanied);
        request.set("notes", notes);
        request.set("carpool_guest_status_id", CarpoolGuestStatus.getId(CarpoolGuestStatus.PENDING));

        boolean isInserted = request.insert();
        if (isInserted) {
            // Notify event owner
            Long ownerId = carpool.getLong("user_id");
            Long eventId = carpool.getLong("event_id");
            if (ownerId != null && !ownerId.equals(user_id) && eventId != null) {
                User sender = User.findById(user_id);
                String senderName = sender != null ? sender.getString("name") : "A user";
                Notification notification = new Notification();
                notification.set("sender_id", user_id);
                notification.set("receiver_id", ownerId);
                notification.set("event_id", eventId);
                notification.set("notification_type_id", NotificationType.getId(NotificationType.CARPOOL));
                notification.set("content", "You have a new carpool request from " + senderName);
                notification.set("is_read", false);
                notification.set("href", "/events/" + eventId + "/carpools/" + id);
                notification.insert();
            }
        }
        return isInserted ? MapResponse.success() : MapResponse.failure("Failed to create carpool request");
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
        Carpool carpool = Carpool.findById(request.getLong("carpool_id"));
        int available_seats = carpool.getInteger("available_seat");
        int filled_seats = carpool.getInteger("filled_seats");
        int noOfPeople = request.getInteger("no_of_people");
        int currentSeats = available_seats - filled_seats;
        if (currentSeats >= noOfPeople) {
            if (response) {
                request.set("carpool_guest_status_id", CarpoolGuestStatus.getId(CarpoolGuestStatus.ACCEPTED));
                CarpoolGuest guest = new CarpoolGuest();
                guest.set("carpool_id", request.getLong("carpool_id"));
                guest.set("Carpool_Request_id", request.getLong("id"));
                guest.set("carpool_guest_status_id", CarpoolGuestStatus.getId(CarpoolGuestStatus.ACCEPTED));
                guest.insert();

                carpool.set("filled_seats", filled_seats + noOfPeople);
                boolean isInserted = carpool.saveIt();
                Long user_id = UserUtil.getUserid();
                Long requestedId = request.getLong("guest_id");
                Long eventId = carpool.getLong("event_id");
                if (isInserted) {
                    Notification notification = new Notification();
                    notification.set("sender_id", user_id);
                    notification.set("receiver_id", requestedId);
                    notification.set("event_id", eventId);
                    notification.set("notification_type_id", NotificationType.getId(NotificationType.CARPOOL));
                    notification.set("content", "Your carpool request has been accepted");
                    notification.set("is_read", false);
                    notification.set("href", "/events/" + eventId + "/carpools/" + id);
                    notification.insert();
                }

            } else {
                request.set("carpool_guest_status_id", CarpoolGuestStatus.getId(CarpoolGuestStatus.REJECTED));
            }
        } else {
            return MapResponse.failure("Not enough seats available");
        }

        return request.saveIt() ? MapResponse.success() : MapResponse.failure("Failed to respond to carpool request");

    }

    public MapResponse viewCarpool(Long id) {

        String sql = "Select Carpools.*, Users.Name, Users.Profile_Pic, Events.Address As Event_Address From Carpools Join Events On Events.Id = Carpools.Event_Id Join Users On Users.Id = Carpools.User_Id Where Carpools.Event_Id = ?";
        List<MapResponse> carpool = Handler.findAll(sql, id);
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

    public MapResponse viewMyCarpools() {

        Long user_id = UserUtil.getUserid();

        String sql = "Select Carpools.*, Events.Title, Events.Address As Event_Address, (Select Event_Thumbnails.Image From Event_Thumbnails Where Event_Thumbnails.Event_Id = Events.Id Order By Events.Id Desc Limit 1) As Event_Thummnail From Carpools Join Events On Events.Id = Carpools.Event_Id Where Carpools.User_Id = ?";

        List<MapResponse> carpool = Handler.findAll(sql, user_id);
        MapResponse response = new MapResponse().put("carpool", carpool);
        return response;
    }

    public MapResponse viewMyCarpoolRequests() {
        Long user_id = UserUtil.getUserid();

        String sql = "Select Carpool_Requests.*, Carpool_Guest_Statuses.Status, Carpools.Event_Id, Events.Title, Events.Address As Event_Address, (Select Event_Thumbnails.Image From Event_Thumbnails Where Event_Thumbnails.Event_Id = Events.Id Order By Events.Id Desc Limit 1) As Event_Thummnail From Carpool_Requests Join Carpool_Guest_Statuses On Carpool_Guest_Statuses.Id = Carpool_Requests.Carpool_Guest_Status_Id Join Carpools On Carpools.Id = Carpool_Requests.Carpool_Id Join Events On Events.Id = Carpools.Event_Id Where Carpool_Requests.Guest_Id = ?";

        List<MapResponse> carpoolRequests = Handler.findAll(sql, user_id);
        MapResponse response = new MapResponse().put("carpool_requests", carpoolRequests);
        return response;
    }
}
