package com.hionstudios.mypersonalinvite.Flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.iam.UserUtil;
import com.hionstudios.mypersonalinvite.model.Event;
import com.hionstudios.mypersonalinvite.model.EventGuest;
import com.hionstudios.mypersonalinvite.model.EventThumbnail;
import com.hionstudios.mypersonalinvite.model.User;
import com.hionstudios.oauth.WorkDrive;
import com.hionstudios.oauth.WorkDrive.Folder;

public class EventFlow {

    public MapResponse addEvent(int event_type_id, String title, String description, int no_of_guest, String date,
            String start_time, String end_time, String address, String gift_suggestion, double latitude,
            double longitude, MultipartFile[] thumbnail) {
        Long userId = UserUtil.getUserid();

        if (userId == null || userId <= 0) {
            return MapResponse.failure("User not authenticated");
        }

        Event event = new Event();
        event.set("owner_id", userId);
        event.set("event_type_id", event_type_id);
        event.set("title", title);
        event.set("description", description);
        event.set("no_of_guest", no_of_guest);
        event.set("date", date);
        event.set("start_time", start_time);
        event.set("end_time", end_time);
        event.set("address", address);
        event.set("gift_suggestion", gift_suggestion);
        event.set("latitude", latitude);
        event.set("longitude", longitude);
        event.insert();

        if (thumbnail != null) {
            for (MultipartFile file : thumbnail) {
                MapResponse response = WorkDrive.upload(file, Folder.MYPERSONALINVITE, false);
                String image = response != null ? response.getString("resource_id") : null;
                EventThumbnail event_thumbnail = new EventThumbnail();
                event_thumbnail.set("event_id", event.getLongId());
                event_thumbnail.set("thumbnail", image);
                event_thumbnail.insert();
            }
        }
        return MapResponse.success();
    }

    public MapResponse editEvent(int id, int event_type_id, String title, String description, int no_of_guest,
            String date, String start_time, String end_time, String address, String gift_suggestion,
            double latitude, double longitude, List<Object> thumbnail) {

        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        Event event = Event.findById(id);
        if (!event.getLong("owner_id").equals(userId))
            return MapResponse.failure("Not allowed");

        event.set("event_type_id", event_type_id);
        event.set("title", title);
        event.set("description", description);
        event.set("no_of_guest", no_of_guest);
        event.set("date", date);
        event.set("start_time", start_time);
        event.set("end_time", end_time);
        event.set("address", address);
        event.set("gift_suggestion", gift_suggestion);
        event.set("latitude", latitude);
        event.set("longitude", longitude);
        event.saveIt();

        List<EventThumbnail> existingThumbs = EventThumbnail.where("event_id = ?", id);
        List<String> keepThumbIds = new ArrayList<>();

        if (thumbnail != null) {
            for (Object item : thumbnail) {
                if (item instanceof String) {
                    keepThumbIds.add((String) item); // keep existing
                } else if (item instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) item;
                    MapResponse response = WorkDrive.upload(file, Folder.MYPERSONALINVITE, false);
                    String imageId = response != null ? response.getString("resource_id") : null;
                    if (imageId != null) {
                        EventThumbnail newThumb = new EventThumbnail();
                        newThumb.set("event_id", id);
                        newThumb.set("thumbnail", imageId);
                        newThumb.insert();
                    }
                }
            }
        }

        for (EventThumbnail thumb : existingThumbs) {
            String thumbId = thumb.getString("thumbnail");
            if (!keepThumbIds.contains(thumbId)) {
                WorkDrive.delete(thumbId);
                thumb.delete();
            }
            return MapResponse.success();
        }
        return MapResponse.success();
    }

    public MapResponse getEventsForOwners() {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        String sql_1 = "Select Events.*, COALESCE(json_agg(Distinct jsonb_build_object('image', Event_Thumbnails.image)) Filter (Where Event_Thumbnails.Id Is Not Null), '[]') As Thumbnails, COALESCE(Json_agg(Distinct jsonb_build_object('amount', Event_Budgets.Amount, 'description', Event_Budgets.Description, 'budget_type', Budget_Types.Type)) Filter (Where Event_Budgets.Id Is Not Null), '[]') As Budgets From Events Left Join Event_Thumbnails On Events.Id = Event_Thumbnails.Event_Id Left Join Event_Budgets On Events.Id = Event_Budgets.Event_Id Left Join Budget_Types On Event_Budgets.Budget_Type_Id = Budget_Types.Id Where Events.Owner_Id = ? Group By Events.Id Order By Events.Date Desc";

        List<MapResponse> events = Handler.findAll(sql_1, userId);

        MapResponse response = new MapResponse().put("OwnersEvents", events);
        return response;

    }

    public MapResponse postEventGuest(int id, List<Map<String, String>> guestList) {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        int added = 0;
        for (Map<String, String> guestMap : guestList) {
            String name = guestMap.get("name");
            String phone = guestMap.get("phone_number");

            if (phone == null || name == null)
                continue;

            // Check if user exists
            User user = User.findFirst("phone_number = ?", phone);
            if (user == null)
                continue;

            // Check if already added to this event
            EventGuest existing = EventGuest.findFirst("event_id = ? AND phone_number = ?", id, phone);
            if (existing != null)
                continue;

            // Add guest
            EventGuest guest = new EventGuest();
            guest.set("event_id", id);
            guest.set("guest_id", user.getId());
            guest.set("name", name);
            guest.set("phone_number", phone);
            guest.insert();
            added++;
        }

        return MapResponse.success("Guests added: " + added);
    }

    public MapResponse rsvpEvent(int id, String rsvp, int no_of_attendees, String comment, boolean carpool_expecting,
            Long carpool_guest_status_id) {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        EventGuest guest = EventGuest.findFirst("event_id = ? And guest_id = ?", id, userId);
        guest.set("rsvp", rsvp);
        guest.set("no_of_attendees", no_of_attendees);
        guest.set("comment", comment);
        if (carpool_expecting && carpool_guest_status_id != null) {
            guest.set("carpool_guest_status_id", carpool_guest_status_id);
        } else {
            guest.set("guest_carpool_status", null);
        }

        guest.saveIt();
        return MapResponse.success("RSVP submitted");

    }

}
