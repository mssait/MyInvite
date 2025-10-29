package com.hionstudios.mypersonalinvite.Flow;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.iam.UserUtil;
import com.hionstudios.mypersonalinvite.model.Event;
import com.hionstudios.mypersonalinvite.model.EventBudget;
import com.hionstudios.mypersonalinvite.model.EventInvite;
import com.hionstudios.mypersonalinvite.model.EventThumbnail;
import com.hionstudios.mypersonalinvite.model.EventTodoList;
import com.hionstudios.mypersonalinvite.model.GuestRsvp;
import com.hionstudios.mypersonalinvite.model.NotificationType;
import com.hionstudios.mypersonalinvite.model.User;
import com.hionstudios.oauth.WorkDrive;
import com.hionstudios.oauth.WorkDrive.Folder;
import com.hionstudios.time.TimeUtil;

public class EventFlow {

    public MapResponse getAllEvents() {
        // String sql = "Select Events.Title, Events.Date, Events.Location_Latitude,
        // Events.Location_Logitude, Event_Types.Type, Users.Name,
        // COALESCE(jsonb_agg(Distinct jsonb_build_object('image',
        // Event_Thumbnails.image)) Filter (Where Event_Thumbnails.Id Is Not Null),
        // '[]'::jsonb) As Thumbnails, COALESCE(Jsonb_agg(Distinct
        // jsonb_build_object('amount', Event_Budgets.Amount, 'description',
        // Event_Budgets.Description, 'budget_type', Budget_Types.Type)) Filter (Where
        // Event_Budgets.Id Is Not Null), '[]'::jsonb) As Budgets From Events Left Join
        // Event_Thumbnails On Events.Id = Event_Thumbnails.Event_Id Left Join
        // Event_Types On Events.Event_Type_Id = Event_Types.Id Left Join Users On
        // Events.Owner_Id = Users.Id Left Join Event_Budgets On Events.Id =
        // Event_Budgets.Event_Id Left Join Budget_Types On Event_Budgets.Budget_Type_Id
        // = Budget_Types.Id Group By Events.Id, Events.Title, Events.Date,
        // Events.Location_Latitude, Events.Location_Logitude, Event_Types.Type,
        // Users.Name Order By Events.Date Desc";

        // List<MapResponse> events = Handler.findAll(sql);
        // MapResponse response = new MapResponse().put("AllEvents", events);
        // return response;

        String sql = "Select Events.Id, Events.Title, Events.Date, Events.Location_Latitude, Events.Location_Longitude, Event_Types.Type, Users.Name From Events Join Event_Types On Events.Event_Type_Id = Event_Types.Id Join Users On Events.Owner_Id = Users.Id Order By Events.Date Desc";

        return Handler.toDataGrid(sql);

    }

    public MapResponse getUpcomingEvents() {
        long now = System.currentTimeMillis();
        String sql = "Select Events.Id, Events.Title, Events.Date, Events.Location_Latitude, Events.Location_Longitude, Event_Types.Type, Users.Name From Events Join Event_Types On Events.Event_Type_Id = Event_Types.Id Join Users On Events.Owner_Id = Users.Id Where Events.Date >= ? Order By Events.Date Asc";
        return Handler.eventtoDataGrid(sql, now);
    }

    public MapResponse getCompletedEvents() {
        long now = System.currentTimeMillis();
        String sql = "Select Events.Id, Events.Title, Events.Date, Events.Location_Latitude, Events.Location_Longitude, Event_Types.Type, Users.Name From Events Join Event_Types On Events.Event_Type_Id = Event_Types.Id Join Users On Events.Owner_Id = Users.Id Where Events.Date <= ? Order By Events.Date Desc";
        return Handler.eventtoDataGrid(sql, now);
    }

    public MapResponse getEventDetails(Long id) {
        String sql = "Select Events.*, COALESCE(json_agg(DISTINCT jsonb_build_object('image', Event_Thumbnails.image)) FILTER (WHERE Event_Thumbnails.Id IS NOT NULL), '[]') AS Thumbnails, COALESCE(json_agg(DISTINCT jsonb_build_object('amount', Event_Budgets.Actual_Amount, 'planned_amount', Event_Budgets.Planned_Amount, 'budget_type', Budget_Types.Type)) FILTER (WHERE Event_Budgets.Id IS NOT NULL), '[]') AS Budgets, COALESCE(SUM(Event_Budgets.Planned_Amount), 0) AS Total_Planned_Budget, COALESCE(SUM(Event_Budgets.Actual_Amount), 0) AS Total_Actual_Budget, COALESCE(SUM(CASE WHEN Rsvp_Status_Id = 1 THEN 1 ELSE 0 END), 0) AS RSVP_Attending, COALESCE(SUM(CASE WHEN Rsvp_Status_Id = 2 THEN 1 ELSE 0 END), 0) AS RSVP_Not_Attending, COALESCE(SUM(CASE WHEN Rsvp_Status_Id = 3 THEN 1 ELSE 0 END), 0) AS RSVP_Maybe, COALESCE(SUM(CASE WHEN Event_Todo_Lists.Status = true THEN 1 ELSE 0 END), 0) AS Todo_Completed, COALESCE(SUM(CASE WHEN Event_Todo_Lists.Status = false THEN 1 ELSE 0 END), 0) AS Todo_Pending FROM Events LEFT JOIN Event_Thumbnails ON Events.Id = Event_Thumbnails.Event_Id LEFT JOIN Event_Budgets ON Events.Id = Event_Budgets.Event_Id LEFT JOIN Budget_Types ON Event_Budgets.Budget_Type_Id = Budget_Types.Id LEFT JOIN Event_Invites ON Events.Id = Event_Invites.Event_Id LEFT JOIN Event_Todo_Lists ON Events.Id = Event_Todo_Lists.Event_Id WHERE Events.Id = ? GROUP BY Events.Id";

        List<MapResponse> events = Handler.findAll(sql, id);
        if (events.isEmpty()) {
            return MapResponse.failure("Event not found");
        }
        return new MapResponse().put("EventDetails", events);
    }

    public MapResponse getEventGuestList(Long id) {
        String sql = "Select Event_Invites.*, Users.Name, Users.Phone_Number, Users.Profile_Pic, Guest_Rsvps.Rsvp, Guest_Rsvps.Comment, Guest_Rsvps.No_Of_Attendees, Guest_Rsvps.Carpool_Expecting From Event_Invites Join Users On Users.Id = Event_Invites.Guest_Id Join Guest_Rsvps On Guest_Rsvps.Event_Invite_Id = Event_Invites.Id Where Event_Invites.Event_Id =? Order By Event_Invites.Created_Time Desc";

        List<MapResponse> guests = Handler.findAll(sql, id);
        MapResponse response = new MapResponse().put("GuestList", guests);
        return response;
    }

    public MapResponse getEventTodoList(Long id) {
        String sql = "Select EVENT_TODO_LIST.*, Events.Id From EVENT_TODO_LIST Join Events On Events.Id = EVENT_TODO_LIST.Event_Id Where EVENT_TODO_LIST.Event_Id = ? Order By EVENT_TODO_LIST.Created_Time Desc";

        List<MapResponse> todos = Handler.findAll(sql, id);
        MapResponse response = new MapResponse().put("Todos", todos);
        return response;
    }

    public MapResponse getEventCarpool(Long id) {
        String sql = "Select Carpools.*, Users.Name, Users.Phone_Number, Users.Profile_Pic From Carpools Join Users On Users.Id = Carpools.User_Id Where Carpools.Event_Id = ? Order By Carpools.Created_Time Desc";

        List<MapResponse> carpools = Handler.findAll(sql, id);
        MapResponse response = new MapResponse().put("CarpoolList", carpools);
        return response;
    }

    public MapResponse getEventGroupChat(Long id) {
        String sql = "Select Event_Messages.*, Users.Name, Users.Phone_Number, Users.Profile_Pic From Event_Messages Join Users On Users.Id = Event_Messages.Sender_Id Where Event_Messages.Event_Id = ? Order By Event_Messages.Created_Time Desc";

        List<MapResponse> groupChats = Handler.findAll(sql, id);
        MapResponse response = new MapResponse().put("GroupChatList", groupChats);
        return response;
    }

    public MapResponse addEvent(int event_type_id, String title, String description, int no_of_guest, String date,
            String start_time, String end_time, String address, String gift_suggestion, double location_latitude,
            double location_longitude, MultipartFile[] thumbnail) {
        Long userId = UserUtil.getUserid();

        if (userId == null || userId <= 0) {
            return MapResponse.failure("User not authenticated");
        }

        Long parsedDate = TimeUtil.parse(date, "yyyy-MM-dd");

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime parsedStartTime = LocalTime.parse(start_time, timeFormatter);
        long startTimeMillis = parsedStartTime.toSecondOfDay() * 1000L;

        LocalTime parsedEndTime = LocalTime.parse(end_time, timeFormatter);
        long endTimeMillis = parsedEndTime.toSecondOfDay() * 1000L;

        Event event = new Event();
        event.set("owner_id", userId);
        event.set("event_type_id", event_type_id);
        event.set("title", title);
        event.set("description", description);
        event.set("no_of_guest", no_of_guest);
        event.set("date", parsedDate);
        event.set("start_time", startTimeMillis);
        event.set("end_time", endTimeMillis);
        event.set("address", address);
        event.set("gift_suggestion", gift_suggestion);
        event.set("location_latitude", location_latitude);
        event.set("location_longitude", location_longitude);
        event.insert();

        if (thumbnail != null) {
            for (MultipartFile file : thumbnail) {
                MapResponse response = WorkDrive.upload(file, Folder.MYPERSONALINVITE, false);
                String image = response != null ? response.getString("resource_id") : null;
                EventThumbnail event_thumbnail = new EventThumbnail();
                event_thumbnail.set("event_id", event.getLongId());
                event_thumbnail.set("image", image);
                event_thumbnail.insert();
            }
        }
        return MapResponse.success();
    }

    public MapResponse editEvent(Long id, int event_type_id, String title, String description,
            int no_of_guest,
            String date, String start_time, String end_time, String address, String gift_suggestion,
            double latitude, double longitude, List<Object> thumbnail) {

        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        Event event = Event.findById(id);
        if (event == null)
            return MapResponse.failure("Event not found");

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
        boolean isSaved = event.saveIt();

        if (isSaved) {
            List<Long> guestIds = Handler
                    .findAll("Select Guest_Id From Event_Invites Where Event_Id = ? And Guest_Id Is Not Null", id)
                    .stream()
                    .map(map -> map.getLong("guest_id"))
                    .collect(Collectors.toList());
            if (guestIds != null && !guestIds.isEmpty()) {
                for (Long guestId : guestIds) {
                    com.hionstudios.mypersonalinvite.model.Notification notification = new com.hionstudios.mypersonalinvite.model.Notification();
                    notification.set("sender_id", userId);
                    notification.set("receiver_id", guestId);
                    notification.set("event_id", id);
                    notification.set("notification_type_id", NotificationType.EVENT);
                    notification.set("content", "The event '" + title + "' has been updated.");
                    notification.set("is_read", false);
                    notification.set("href", "/events/" + id);
                    notification.insert();
                }
            }
        }

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
        }

        return MapResponse.success();
    }

    public MapResponse getUpcomingEventsForOwners() {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        String sql_1 = "Select Events.*, COALESCE(jsonb_agg(DISTINCT jsonb_build_object('image', Event_Thumbnails.image)) FILTER (WHERE Event_Thumbnails.Id IS NOT NULL), '[]') AS Thumbnails, COALESCE(jsonb_agg(DISTINCT jsonb_build_object('Planned_amount', Event_Budgets.Planned_Amount, 'Actual_Amount', Event_Budgets.Actual_Amount, 'budget_type', Budget_Types.Type)) FILTER (WHERE Event_Budgets.Id IS NOT NULL), '[]') AS Budgets From Events Left Join Event_Thumbnails On Events.Id = Event_Thumbnails.Event_Id Left Join Event_Budgets On Events.Id = Event_Budgets.Event_Id Left Join Budget_Types On Event_Budgets.Budget_Type_Id = Budget_Types.Id Where Events.Owner_Id = ? And Events.Date > ? Group By Events.Id Order By Events.Date Desc";

        long currentTime = TimeUtil.currentTime();
        List<MapResponse> events = Handler.findAll(sql_1, userId, currentTime);

        MapResponse response = new MapResponse().put("OwnersEvents", events);
        return response;

    }

    public MapResponse getCompletedEventsForOwners() {
        Long userId = UserUtil.getUserid();
        long now = System.currentTimeMillis();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        String sql_1 = "Select Events.*, COALESCE(json_agg(Distinct json_build_object('image', Event_Thumbnails.image)) Filter (Where Event_Thumbnails.Id Is Not Null), '[]') As Thumbnails, COALESCE(Json_agg(Distinct json_build_object('amount', Event_Budgets.Planned_Amount, 'Actual_Amount', Event_Budgets.Actual_Amount, 'budget_type', Budget_Types.Type)) Filter (Where Event_Budgets.Id Is Not Null), '[]') As Budgets From Events Left Join Event_Thumbnails On Events.Id = Event_Thumbnails.Event_Id Left Join Event_Budgets On Events.Id = Event_Budgets.Event_Id Left Join Budget_Types On Event_Budgets.Budget_Type_Id = Budget_Types.Id Where Events.Owner_Id = ? And Events.Date < ? Group By Events.Id Order By Events.Date Desc";

        List<MapResponse> events = Handler.findAll(sql_1, userId, now);

        MapResponse response = new MapResponse().put("OwnersEvents", events);
        return response;
    }

    public MapResponse getAllEventsForOwners() {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        String sql_1 = "Select Events.*, COALESCE(json_agg(Distinct json_build_object('image', Event_Thumbnails.image)) Filter (Where Event_Thumbnails.Id Is Not Null), '[]') As Thumbnails, COALESCE(Json_agg(Distinct json_build_object('amount', Event_Budgets.Planned_Amount, 'Actual_Amount', Event_Budgets.Actual_Amount, 'budget_type', Budget_Types.Type)) Filter (Where Event_Budgets.Id Is Not Null), '[]') As Budgets From Events Left Join Event_Thumbnails On Events.Id = Event_Thumbnails.Event_Id Left Join Event_Budgets On Events.Id = Event_Budgets.Event_Id Left Join Budget_Types On Event_Budgets.Budget_Type_Id = Budget_Types.Id Where Events.Owner_Id = ? Group By Events.Id Order By Events.Date Desc";

        List<MapResponse> events = Handler.findAll(sql_1, userId);

        MapResponse response = new MapResponse().put("OwnersEvents", events);
        return response;
    }

    public MapResponse deleteEvent(Long id) {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        Event event = Event.findById(id);
        if (event == null || !event.getLong("owner_id").equals(userId))
            return MapResponse.failure("Not allowed");

        List<EventThumbnail> thumbnails = EventThumbnail.where("event_id = ?", id);
        for (EventThumbnail thumb : thumbnails) {
            WorkDrive.delete(thumb.getString("thumbnail"));
            thumb.delete();
        }

        List<EventBudget> budgets = EventBudget.where("event_id = ?", id);
        for (EventBudget budget : budgets) {
            budget.delete();
        }

        List<EventTodoList> todo = EventTodoList.where("event_id = ?", id);
        for (EventTodoList item : todo) {
            item.delete();
        }

        List<EventInvite> invites = EventInvite.where("event_id = ?", id);
        for (EventInvite invite : invites) {
            if (invite.getLong("guest_id") != null) {
                GuestRsvp rsvp = GuestRsvp.findFirst("event_invite_id = ?", invite.getId());
                if (rsvp != null) {
                    rsvp.delete();
                }
            }
            invite.delete();
        }
        boolean isDeleted = event.delete();
        if (isDeleted) {
            List<Long> guestIds = Handler
                    .findAll("Select Guest_Id From Event_Invites Where Event_Id = ? And Guest_Id Is Not Null", id)
                    .stream()
                    .map(map -> map.getLong("guest_id"))
                    .collect(Collectors.toList());
            if (guestIds != null && !guestIds.isEmpty()) {
                for (Long guestId : guestIds) {
                    com.hionstudios.mypersonalinvite.model.Notification notification = new com.hionstudios.mypersonalinvite.model.Notification();
                    notification.set("sender_id", userId);
                    notification.set("receiver_id", guestId);
                    notification.set("event_id", id);
                    notification.set("notification_type_id", NotificationType.EVENT);
                    notification.set("content", "The event '" + event.getString("title") + "' has been updated.");
                    notification.set("is_read", false);
                    notification.set("href", "/events/" + id);
                    notification.insert();
                }
            }
        }

        return MapResponse.success("Event deleted successfully");
    }

    public MapResponse guestUpcomingEvents() {
        Long userId = UserUtil.getUserid();
        long now = System.currentTimeMillis();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        String sql = "Select *, Event_Thumbnails.Image, Events.Date As Event_Date From Event_Invites Join Event_Thumbnails On Event_Thumbnails.Event_Id = Event_Invites.Event_Id Join Events On Events.Id = Event_Invites.Event_Id Where Event_Invites.Guest_Id = ? And Events.Date > ?";
        List<MapResponse> events = Handler.findAll(sql, userId, now);

        MapResponse response = new MapResponse().put("InvitedToUpcomingEvents", events);
        return response;
    }

    public MapResponse guestCompletedEvents() {
        Long userId = UserUtil.getUserid();
        long now = System.currentTimeMillis();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        String sql = "Select *, Event_Thumbnails.Image, Events.Date As Event_Date From Event_Invites Join Event_Thumbnails On Event_Thumbnails.Event_Id = Event_Invites.Event_Id Join Events On Events.Id = Event_Invites.Event_Id Where Event_Invites.Guest_Id = ? And Events.Date < ?";
        List<MapResponse> events = Handler.findAll(sql, userId, now);

        MapResponse response = new MapResponse().put("InvitedToCompletedEvents", events);
        return response;
    }

    public MapResponse getInvitedList(Long id) {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        String sql = "Select * From Event_Invites Where Event_id = ?";
        List<MapResponse> events = Handler.findAll(sql, id);

        MapResponse response = new MapResponse().put("InvitedList", events);
        return response;
    }

    public MapResponse getInvitedToEventDetails(Long id) {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        String sql = "Select Event_Invites.Guest_Id, Event_Invites.Event_Id, Event_Invites.Rsvp_Status_Id, Events.*, ARRAY_AGG(Distinct Event_Thumbnails.Image) As Images, Count(Guest_Rsvps.Rsvp) As Rsvp_Count, Count(Guest_Rsvps.Carpool_Expecting) As Carpool_Expecting_Count, Count(*) Filter (Where Guest_Rsvps.Rsvp = 'Attending') As Attending_Count FROM Event_Invites Join Events ON Events.Id = Event_Invites.Event_Id Join Event_Thumbnails ON Event_Thumbnails.Event_Id = Events.Id LEFT Join Guest_Rsvps ON Guest_Rsvps.Event_Invite_Id = Event_Invites.Id Where Event_Invites.Guest_Id = ? And Event_Invites.Event_Id = ? GROUP BY Event_Invites.Guest_Id, Event_Invites.Event_Id, Event_Invites.Rsvp_Status_Id, Events.Id";

        MapResponse events = Handler.findFirst(sql, userId, id);
        MapResponse response = new MapResponse().put("InvitedToEventDetails", events);
        return response;
    }

    public MapResponse deleteGuestList(Long id, Long guestId) {
        EventInvite invite = EventInvite.findFirst("event_id = ? AND guest_id = ?", id, guestId);
        if (invite != null) {
            invite.delete();
            return MapResponse.success("Guest list deleted successfully");
        } else {
            return MapResponse.failure("Guest not found in the event");
        }
    }

    public MapResponse updateRsvp(Long id, Long rsvp) {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        EventInvite invite = EventInvite.findFirst("event_id = ? AND guest_id = ?", id, userId);
        if (invite == null) {
            return MapResponse.failure("They are not invited to this event");
        }

        invite.set("rsvp_status_id", rsvp);
        invite.saveIt();

        return MapResponse.success("RSVP updated successfully");
    }

    public MapResponse getCarpoolList(Long id) {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        String sql = "Select * From Carpools Where Event_id = ?";
        List<MapResponse> carpools = Handler.findAll(sql, id);

        MapResponse response = new MapResponse().put("CarpoolList", carpools);
        return response;
    }

    public MapResponse viewBudget(Long id) {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        String sql = "Select * From Event_Budgets Where Event_id = ?";
        List<MapResponse> budgets = Handler.findAll(sql, id);

        MapResponse response = new MapResponse().put("BudgetList", budgets);
        return response;
    }

    public MapResponse postEventGuest(Long id, List<String> emailList, List<Map<String, String>> guestList) {
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

    public MapResponse rsvpEvent(Long eventId, String rsvp, int no_of_attendees, String comment,
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

        boolean isSaved = guestRsvp.saveIt();
        if (isSaved) {
            // Notify event owner
            Event event = Event.findById(eventId);
            if (event != null) {
                Long ownerId = event.getLong("owner_id");
                if (ownerId != null && !ownerId.equals(userId)) {
                    com.hionstudios.mypersonalinvite.model.Notification notification = new com.hionstudios.mypersonalinvite.model.Notification();
                    notification.set("sender_id", userId);
                    notification.set("receiver_id", ownerId);
                    notification.set("event_id", eventId);
                    notification.set("notification_type_id", NotificationType.RSVP);
                    notification.set("content",
                            user.getString("name") + " has RSVP'd to your event '" + event.getString("title") + "'.");
                    notification.set("is_read", false);
                    notification.set("href", "/events/" + eventId);
                    notification.insert();
                }
            }
        }
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

    public MapResponse addBudget(Long id, Long budget_type_id, Long actual_amount, Long planned_amount) {
        EventBudget budget = new EventBudget();
        budget.set("event_id", id);
        budget.set("budget_type_id", budget_type_id);
        budget.set("planned_amount", planned_amount);
        budget.set("actual_amount", actual_amount);
        budget.insert();
        return MapResponse.success("Budget item added");
    }

    public MapResponse updateBudget(Long id, Long budget_type_id, Long actual_amount, Long planned_amount) {
        EventBudget budget = EventBudget.findById(id);
        if (budget == null)
            return MapResponse.failure("Budget item not found");

        if (budget_type_id != null)
            budget.set("budget_type_id", budget_type_id);
        if (actual_amount != null)
            budget.set("actual_amount", actual_amount);
        if (planned_amount != null)
            budget.set("planned_amount", planned_amount);

        return budget.saveIt() ? MapResponse.success("Budget item updated") : MapResponse.failure();
    }

    public MapResponse deleteBudget(Long id) {
        EventBudget budget = EventBudget.findById(id);
        if (budget == null)
            return MapResponse.failure("Budget item not found");
        budget.delete();
        return MapResponse.success("Budget item deleted");
    }

    public MapResponse getGuestList(Long id) {
        String sql = "Select Event_Invites.* Users.Name, Users.Phone_Nummber, Users.Profile_Pic From Event_Invites Join Users On Users.Id = Event_Invites.Guest_Id Where Event_Invites.Event_Id =? Order By Event_Invites.Created_Time Desc";

        List<MapResponse> guest = Handler.findAll(sql, id);
        MapResponse response = new MapResponse().put("GuestList", guest);
        return response;
    }

    public MapResponse getEventTypes(){
        String sql = "Select * From Event_Types Order By Type Asc";
        List<MapResponse> eventTypes = Handler.findAll(sql);
        MapResponse response = new MapResponse().put("EventTypes", eventTypes);
        return response;
    }

    public MapResponse getRsvpStatuses(){
        String sql = "Select * From Rsvp_Statuses Order By Status Asc";
        List<MapResponse> RsvpTypes = Handler.findAll(sql);
        MapResponse response = new MapResponse().put("RsvpTypes", RsvpTypes);
        return response;
    }

    public MapResponse getBudgetTypes(){
        String sql = "Select * From Budget_Types Order By Id Asc";
        List<MapResponse> budgetTypes = Handler.findAll(sql);
        MapResponse response = new MapResponse().put("BudgetTypes", budgetTypes);
        return response;
    }

}
