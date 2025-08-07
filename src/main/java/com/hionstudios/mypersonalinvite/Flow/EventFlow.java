package com.hionstudios.mypersonalinvite.Flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.iam.UserUtil;
import com.hionstudios.mypersonalinvite.model.Event;
import com.hionstudios.mypersonalinvite.model.EventBudget;
import com.hionstudios.mypersonalinvite.model.EventInvite;
import com.hionstudios.mypersonalinvite.model.EventThumbnail;
import com.hionstudios.mypersonalinvite.model.GuestRsvp;
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

        String sql_1 = "Select Events.*, COALESCE(json_agg(Distinct json_build_object('image', Event_Thumbnails.image)) Filter (Where Event_Thumbnails.Id Is Not Null), '[]') As Thumbnails, COALESCE(Json_agg(Distinct json_build_object('amount', Event_Budgets.Amount, 'description', Event_Budgets.Description, 'budget_type', Budget_Types.Type)) Filter (Where Event_Budgets.Id Is Not Null), '[]') As Budgets From Events Left Join Event_Thumbnails On Events.Id = Event_Thumbnails.Event_Id Left Join Event_Budgets On Events.Id = Event_Budgets.Event_Id Left Join Budget_Types On Event_Budgets.Budget_Type_Id = Budget_Types.Id Where Events.Owner_Id = ? Group By Events.Id Order By Events.Date Desc";

        List<MapResponse> events = Handler.findAll(sql_1, userId);

        MapResponse response = new MapResponse().put("OwnersEvents", events);
        return response;

    }

    public MapResponse postEventGuest(int id, List<String> emailList, List<Map<String, String>> guestList) {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        int added = 0;
        // Handle the email parameter for the registering user
        if (emailList != null && !emailList.isEmpty()) {
            for (String email : emailList) {
                if (email == null || email.isEmpty())
                    continue;

                User user = User.findFirst("email = ?", email);
                if (user != null) {
                    EventInvite invite = EventInvite.findFirst("Event_Id = ? And Email = ? And Guest_Id Is Null", id,
                            email);
                    if (invite != null) {
                        // Rule: Invite’s GUEST_ID IS NULL → Update with registering user's ID
                        invite.set("guest_id", user.getId());
                        invite.insert();
                        added++;
                    } else {
                        // Check if user is already invited
                        EventInvite existing = EventInvite.findFirst("Event_Id = ? And Email = ? And Guest_Id = ?", id,
                                email, user.getId());
                        if (existing == null) {
                            // Rule: Invite’s GUEST_ID IS NOT NULL or No invite (public RSVP) → Create new
                            EventInvite newInvite = new EventInvite();
                            newInvite.set("event_id", id);
                            newInvite.set("email", email);
                            newInvite.set("guest_id", user.getId());
                            newInvite.set("name", user.getString("name"));
                            newInvite.set("phone_number", user.getString("phone_number"));
                            newInvite.insert();
                            added++;
                        }
                    }
                } else {
                    // Unregistered user: Create invite with GUEST_ID = null
                    EventInvite existing = EventInvite.findFirst("Event_Id = ? And Email = ? And Guest_Id Is Null", id,
                            email);
                    if (existing == null) {
                        EventInvite newInvite = new EventInvite();
                        newInvite.set("event_id", id);
                        newInvite.set("email", email);
                        newInvite.insert();
                        added++;
                    }
                }
            }
        }

        if (guestList != null && !guestList.isEmpty()) {
            // Handle guest list
            for (Map<String, String> guestMap : guestList) {
                String phone = guestMap.get("phone_number");
                String name = guestMap.get("name");

                if (phone == null)
                    continue;

                // Check if user exists
                User user = null;
                if (phone != null) {
                    user = User.findFirst("phone_number = ?", phone);
                }

                if (user != null) {
                    // Registered user: Fill all details
                    EventInvite invite = null;
                    if (phone != null) {
                        invite = EventInvite.findFirst("Event_id = ? And Phone_Number = ? And Guest_Id Is Null", id,
                                phone);
                    }

                    if (invite != null) {
                        // Rule: Invite’s GUEST_ID IS NULL → Update with registering user's ID
                        invite.set("guest_id", user.getId());
                        invite.set("name", name);
                        if (phone != null)
                            invite.set("phone_number", phone);
                        invite.insert();
                        added++;
                        continue;
                    }

                    // Check if user is already invited
                    EventInvite existing = null;
                    if (phone != null) {
                        existing = EventInvite.findFirst("Event_id = ? And Phone_Number = ? And Guest_Id = ?", id,
                                phone, user.getId());
                    }

                    if (existing != null)
                        continue;

                    EventInvite newInvite = new EventInvite();
                    newInvite.set("event_id", id);
                    newInvite.set("guest_id", user.getId());
                    if (name != null)
                        newInvite.set("name", name);
                    if (phone != null)
                        newInvite.set("phone_number", phone);
                    newInvite.insert();
                    added++;
                } else {
                    // Unregistered user: Create invite with GUEST_ID = null
                    EventInvite existing = null;
                    if (phone != null) {
                        existing = EventInvite.findFirst("Event_id = ? And Phone_Number = ? And Guest_Id Is Null", id,
                                phone);
                    }

                    if (existing == null) {
                        EventInvite newInvite = new EventInvite();
                        newInvite.set("event_id", id);
                        newInvite.set("name", name);
                        if (phone != null)
                            newInvite.set("phone_number", phone);
                        newInvite.insert();
                        added++;
                    }
                }
            }
        }

        return MapResponse.success("Guests added: " + added);
    }

    public MapResponse rsvpEvent(int eventId, String rsvp, int no_of_attendees, String comment,
            boolean carpool_expecting, Long carpool_guest_status_id) {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        // Find or create EVENT_INVITES row for the user
        User user = User.findFirst("id = ?", userId);
        if (user == null)
            return MapResponse.failure("User not found");

        EventInvite invite = EventInvite.findFirst("event_id = ? AND guest_id = ?", eventId, userId);
        if (invite == null) {
            // Check for an invite with matching email or phone_number and GUEST_ID = null
            String email = user.getString("email");
            String phoneNumber = user.getString("phone_number");

            invite = EventInvite.findFirst("event_id = ? AND (email = ? OR phone_number = ?) AND guest_id IS NULL",
                    eventId, email, phoneNumber);
            if (invite != null) {
                // Rule: Invite’s GUEST_ID IS NULL → Update with registering user's ID
                invite.set("guest_id", userId);
                invite.set("name", user.getString("name"));
                invite.set("email", email);
                invite.set("phone_number", phoneNumber);
                invite.saveIt();
            } else {
                // Rule: No invite (public RSVP) → Create new EVENT_INVITES row
                invite = new EventInvite();
                invite.set("event_id", eventId);
                invite.set("guest_id", userId);
                invite.set("name", user.getString("name"));
                invite.set("email", email);
                invite.set("phone_number", phoneNumber);
                invite.insert();
            }
        }

        // Update or create GUEST_RSVPS
        GuestRsvp guestRsvp = GuestRsvp.findFirst("event_invite_id = ?", invite.getId());
        if (guestRsvp == null) {
            guestRsvp = new GuestRsvp();
            guestRsvp.set("event_invite_id", invite.getId());
        }

        guestRsvp.set("rsvp", rsvp);
        guestRsvp.set("no_of_attendees", no_of_attendees);
        guestRsvp.set("comment", comment);
        guestRsvp.set("carpool_expecting", carpool_expecting);
        if (carpool_expecting && carpool_guest_status_id != null) {
            guestRsvp.set("carpool_guest_status_id", carpool_guest_status_id);
        } else {
            guestRsvp.set("carpool_guest_status_id", null);
        }

        guestRsvp.saveIt();
        return MapResponse.success("RSVP submitted");
    }

    // public MapResponse postEventGuest(int eventId, List<Map<String, String>>
    // guestList) {
    // Long hostId = UserUtil.getUserid();
    // if (hostId == null || hostId <= 0)
    // return MapResponse.failure("User not authenticated");

    // int added = 0;
    // for (Map<String, String> guestMap : guestList) {
    // String name = guestMap.get("name");
    // String phone = guestMap.get("phone_number");
    // String email = guestMap.get("email");

    // if ((phone == null && email == null) || name == null)
    // continue;

    // // Check if user exists
    // User user = null;
    // if (phone != null) {
    // user = User.findFirst("phone_number = ?", phone);
    // } else if (email != null) {
    // user = User.findFirst("email = ?", email);
    // }

    // // Check if already invited
    // EventInvite existing = null;
    // if (phone != null) {
    // existing = EventInvite.findFirst("event_id = ? AND phone_number = ?",
    // eventId, phone);
    // } else if (email != null) {
    // existing = EventInvite.findFirst("event_id = ? AND email = ?", eventId,
    // email);
    // }

    // if (existing != null)
    // continue;

    // // Insert invite
    // EventInvite invite = new EventInvite();
    // invite.set("event_id", eventId);
    // invite.set("name", name);
    // invite.set("phone_number", phone);
    // invite.set("email", email);
    // if (user != null) {
    // invite.set("guest_id", user.getId());
    // }
    // invite.insert();
    // added++;
    // }

    // return MapResponse.success("Guests added: " + added);
    // }

    // public MapResponse rsvpEvent(int id, String rsvp, int no_of_attendees, String
    // comment, boolean carpool_expecting,
    // Long carpool_guest_status_id) {
    // Long userId = UserUtil.getUserid();
    // if (userId == null || userId <= 0)
    // return MapResponse.failure("User not authenticated");

    // EventInvite guest = EventInvite.findFirst("event_id = ? And guest_id = ?",
    // id, userId);
    // guest.set("rsvp", rsvp);
    // guest.set("no_of_attendees", no_of_attendees);
    // guest.set("comment", comment);
    // if (carpool_expecting && carpool_guest_status_id != null) {
    // guest.set("carpool_guest_status_id", carpool_guest_status_id);
    // } else {
    // guest.set("guest_carpool_status", null);
    // }

    // guest.saveIt();
    // return MapResponse.success("RSVP submitted");

    // }

    public MapResponse addBudget(long id, long budget_type_id, String amount, String description) {
        EventBudget budget = new EventBudget();
        budget.set("event_id", id);
        budget.set("budget_type_id", budget_type_id);
        budget.set("description", description);
        budget.set("amount", amount);
        budget.insert();
        return MapResponse.success("Budget item added");
    }

    public MapResponse updateBudget(long id, Long budget_type_id, String amount, String description) {
        EventBudget budget = EventBudget.findById(id);
        if (budget == null)
            return MapResponse.failure("Budget item not found");

        if (budget_type_id != null)
            budget.set("budget_type_id", budget_type_id);
        if (description != null)
            budget.set("description", description);
        if (amount != null)
            budget.set("amount", amount);

        return budget.saveIt() ? MapResponse.success("Budget item updated") : MapResponse.failure();
    }

    public MapResponse deleteBudget(long id) {
        EventBudget budget = EventBudget.findById(id);
        if (budget == null)
            return MapResponse.failure("Budget item not found");
        budget.delete();
        return MapResponse.success("Budget item deleted");
    }

    public MapResponse getUpcomingEvents() {
        String sql = "Select Events.*, COALESCE(json_agg(Distinct json_build_object('image', Event_Thumbnails.image)) Filter (Where Event_Thumbnails.Id Is Not Null), '[]') As Thumbnails, COALESCE(Json_agg(Distinct json_build_object('amount', Event_Budgets.Amount, 'description', Event_Budgets.Description, 'budget_type', Budget_Types.Type)) Filter (Where Event_Budgets.Id Is Not Null), '[]') As Budgets From Events Left Join Event_Thumbnails On Events.Id = Event_Thumbnails.Event_Id Left Join Event_Budgets On Events.Id = Event_Budgets.Event_Id Left Join Budget_Types On Event_Budgets.Budget_Type_Id = Budget_Types.Id Where Events.Completed = ? Group By Events.Id Order By Events.Date Desc";

        List<MapResponse> events = Handler.findAll(sql, false);
        MapResponse response = new MapResponse().put("UpcomingEvents", events);
        return response;
    }

    public MapResponse getCompletedEvents() {
        String sql = "Select Events.*, COALESCE(json_agg(Distinct json_build_object('image', Event_Thumbnails.image)) Filter (Where Event_Thumbnails.Id Is Not Null), '[]') As Thumbnails, COALESCE(Json_agg(Distinct json_build_object('amount', Event_Budgets.Amount, 'description', Event_Budgets.Description, 'budget_type', Budget_Types.Type)) Filter (Where Event_Budgets.Id Is Not Null), '[]') As Budgets From Events Left Join Event_Thumbnails On Events.Id = Event_Thumbnails.Event_Id Left Join Event_Budgets On Events.Id = Event_Budgets.Event_Id Left Join Budget_Types On Event_Budgets.Budget_Type_Id = Budget_Types.Id Where Events.Completed = ? Group By Events.Id Order By Events.Date Desc";

        List<MapResponse> events = Handler.findAll(sql, true);
        MapResponse response = new MapResponse().put("CompletedEvents", events);
        return response;
    }

    public MapResponse getGuestList(long id) {
        String sql = "Select Event_Guests.* Users.Name, Users.Phone_Nummber, Users.Profile_Pic From Event_Guests Join Users On Users.Id = Event_Guests.Guest_Id Where Event_Guests.Event_Id =? Order By Event_Guests.Created_Time Desc";

        List<MapResponse> guest = Handler.findAll(sql, id);
        MapResponse response = new MapResponse().put("GuestList", guest);
        return response;
    }

}
