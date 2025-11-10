package com.hionstudios.mypersonalinvite.Flow;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.auth.oauth2.Credential;
import com.hionstudios.FirebaseNotificationService;
import com.hionstudios.MapResponse;
import com.hionstudios.WhatsAppUtil;
import com.hionstudios.db.Handler;
import com.hionstudios.iam.UserUtil;
import com.hionstudios.mypersonalinvite.model.Event;
import com.hionstudios.mypersonalinvite.model.EventBudget;
import com.hionstudios.mypersonalinvite.model.EventInvite;
import com.hionstudios.mypersonalinvite.model.EventThumbnail;
import com.hionstudios.mypersonalinvite.model.EventTodoList;
import com.hionstudios.mypersonalinvite.model.FcmDeviceToken;
import com.hionstudios.mypersonalinvite.model.GoogleOauth;
import com.hionstudios.mypersonalinvite.model.GuestRsvp;
import com.hionstudios.mypersonalinvite.model.Notification;
import com.hionstudios.mypersonalinvite.model.NotificationType;
import com.hionstudios.mypersonalinvite.model.User;
import com.hionstudios.oauth.GoogleOauthService;
import com.hionstudios.oauth.WorkDrive;
import com.hionstudios.oauth.WorkDrive.Folder;
import com.hionstudios.time.TimeUtil;

@Service
public class EventFlow {

    private final FirebaseNotificationService firebaseNotificationService;
    private final GoogleOauthService googleService;

    @Autowired
    public EventFlow(FirebaseNotificationService firebaseNotificationService,
            GoogleOauthService googleService) {
        this.firebaseNotificationService = firebaseNotificationService;
        this.googleService = googleService;
    }

    public MapResponse getAllEvents() {
        String sql = "Select Events.Id, Events.Title, Events.Date, Events.Location_Latitude, Events.Location_Longitude, Event_Types.Type, Users.Name From Events Join Event_Types On Events.Event_Type_Id = Event_Types.Id Join Users On Events.Owner_Id = Users.Id Order By Events.Date Desc";

        return Handler.toDataGrid(sql);

    }

    public MapResponse getUpcomingEvents() {
        long now = System.currentTimeMillis();
        String sql = "Select Events.Id, Events.Title, Events.Date, Events.Location_Latitude, Events.Location_Longitude, Event_Types.Type, Users.Name From Events Join Event_Types On Events.Event_Type_Id = Event_Types.Id Join Users On Events.Owner_Id = Users.Id Where Events.Date >= ? Order By Events.Date Asc";
        return Handler.eventToDataGrid(sql, now);
    }

    public MapResponse getCompletedEvents() {
        long now = System.currentTimeMillis();
        String sql = "Select Events.Id, Events.Title, Events.Date, Events.Location_Latitude, Events.Location_Longitude, Event_Types.Type, Users.Name From Events Join Event_Types On Events.Event_Type_Id = Event_Types.Id Join Users On Events.Owner_Id = Users.Id Where Events.Date <= ? Order By Events.Date Desc";
        return Handler.eventToDataGrid(sql, now);
    }

    public MapResponse getEventDetails(Long id) {
        String sql = "Select Events.*, Coalesce(Event_Thumbnails_agg.Thumbnails, '[]') As Thumbnails, Coalesce(Event_Budgets_agg.Budgets, '[]') As Budgets, Coalesce(Event_Budgets_agg.Total_Planned_Budget, 0) As Total_Planned_Budget, Coalesce(Event_Budgets_agg.Total_Actual_Budget, 0) As Total_Actual_Budget, Coalesce(Event_Invites_agg.RSVP_Attending, 0) As RSVP_Attending, Coalesce(Event_Invites_agg.RSVP_Not_Attending, 0) As RSVP_Not_Attending, Coalesce(Event_Invites_agg.RSVP_Maybe, 0) As RSVP_Maybe, Coalesce(Event_Todo_Lists_agg.Todo_Completed, 0) As Todo_Completed, Coalesce(Event_Todo_Lists_agg.Todo_Pending, 0) As Todo_Pending FROM Events LEFT JOIN (Select Event_Thumbnails.Event_Id, json_agg(jsonb_build_object('image', Event_Thumbnails.image)) As Thumbnails FROM Event_Thumbnails GROUP BY Event_Thumbnails.Event_Id) As Event_Thumbnails_agg ON Events.Id = Event_Thumbnails_agg.Event_Id LEFT JOIN (Select Event_Budgets.Event_Id, json_agg(jsonb_build_object('amount', Event_Budgets.Actual_Amount, 'planned_amount', Event_Budgets.Planned_Amount, 'budget_type', Budget_Types.Type)) As Budgets, SUM(Event_Budgets.Planned_Amount) As Total_Planned_Budget, SUM(Event_Budgets.Actual_Amount) As Total_Actual_Budget FROM Event_Budgets LEFT JOIN Budget_Types ON Event_Budgets.Budget_Type_Id = Budget_Types.Id GROUP BY Event_Budgets.Event_Id) As Event_Budgets_agg ON Events.Id = Event_Budgets_agg.Event_Id LEFT JOIN (Select Event_Invites.Event_Id, SUM(CASE WHEN Event_Invites.Rsvp_Status_Id = 1 THEN 1 ELSE 0 END) As RSVP_Attending, SUM(CASE WHEN Event_Invites.Rsvp_Status_Id = 2 THEN 1 ELSE 0 END) As RSVP_Not_Attending, SUM(CASE WHEN Event_Invites.Rsvp_Status_Id = 3 THEN 1 ELSE 0 END) As RSVP_Maybe FROM Event_Invites GROUP BY Event_Invites.Event_Id) As Event_Invites_agg ON Events.Id = Event_Invites_agg.Event_Id LEFT JOIN (Select Event_Todo_Lists.Event_Id, SUM(CASE WHEN Event_Todo_Lists.Status = true THEN 1 ELSE 0 END) As Todo_Completed, SUM(CASE WHEN Event_Todo_Lists.Status = false THEN 1 ELSE 0 END) As Todo_Pending FROM Event_Todo_Lists GROUP BY Event_Todo_Lists.Event_Id) As Event_Todo_Lists_agg ON Events.Id = Event_Todo_Lists_agg.Event_Id WHERE Events.Id = ?";

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

        // Now add the event to Google Calendar
        try {
            // Fetch event owner info
            User user = User.findById(userId);
            if (user == null) {
                System.err.println("User not found, skipping Google Calendar integration.");
                return MapResponse.success("Event added but calendar sync skipped (user missing).");
            }

            String guestEmail = user.getString("email");
            if (guestEmail == null || guestEmail.isEmpty()) {
                System.out.println("Guest has no email, skipping Google Calendar invite.");
                return MapResponse.success("Event added but no email found for calendar invite.");
            }

            // Find owner's Google OAuth credentials
            GoogleOauth ownerOauth = GoogleOauth.findFirst("user_id = ?", user.getLong("id"));
            if (ownerOauth == null) {
                System.out.println("Event owner has not connected Google Calendar, skipping invite.");
                return MapResponse.success("Event added but Google Calendar not connected.");
            }

            // Build credential
            Credential credential = googleService.buildCredentialFromTokens(
                    ownerOauth.getString("access_token"),
                    ownerOauth.getString("refresh_token"),
                    ownerOauth.getLong("expiry"));

            // Refresh token if needed
            try {
                Long secs = credential.getExpiresInSeconds();
                if (secs == null || secs <= 60) {
                    System.out.println("Access token expired or near expiry. Refreshing...");
                } else {
                    System.out
                            .println("Access token still valid for " + secs + "s, but forcing refresh just in case...");
                }

                boolean refreshed = credential.refreshToken();
                System.out.println("Token refresh attempted, result: " + refreshed);

                if (refreshed) {
                    ownerOauth.set("access_token", credential.getAccessToken())
                            .set("refresh_token", credential.getRefreshToken())
                            .set("expiry", credential.getExpirationTimeMilliseconds())
                            .saveIt();
                } else {
                    System.err.println("Refresh token invalid — user must reconnect Google account.");
                }
            } catch (Exception e) {
                System.err.println("Error refreshing token: " + e.getMessage());
            }

            // Create Google Calendar Event
            try {
                String eventTitle = event.getString("title");
                String eventDescription = "Get ready for your Event: " + eventTitle;

                long start_time_val = event.getLong("start_time");
                long end_time_val = event.getLong("end_time");
                long date_val = event.getLong("date");

                String startTime = TimeUtil.toRFC3339FromDateAndTime(date_val, start_time_val);
                String endTime = TimeUtil.toRFC3339FromDateAndTime(date_val, end_time_val);

                System.out.println("Start Time (RFC3339): " + startTime);
                System.out.println("End Time (RFC3339): " + endTime);

                googleService.createCalendarEvent(
                        credential,
                        eventTitle,
                        eventDescription,
                        startTime,
                        endTime,
                        List.of(guestEmail));

                System.out.println("Google Calendar invite sent to " + guestEmail);
            } catch (Exception ex) {
                System.err.println(
                        "Failed to create Google Calendar event for guest " + guestEmail + ": " + ex.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Error adding Google Calendar invite: " + e.getMessage());
        }

        return MapResponse.success();
    }

    public MapResponse editEvent(
            Long id, int event_type_id, String title, String description,
            int no_of_guest, String date, String start_time, String end_time,
            String address, String gift_suggestion, double latitude,
            double longitude, List<Object> thumbnail) {

        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        Event event = Event.findById(id);
        if (event == null)
            return MapResponse.failure("Event not found");

        if (!event.getLong("owner_id").equals(userId))
            return MapResponse.failure("Not allowed");

        // --- Update event details ---
        Long parsedDate = TimeUtil.parse(date, "yyyy-MM-dd");

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime parsedStartTime = LocalTime.parse(start_time, timeFormatter);
        long startTimeMillis = parsedStartTime.toSecondOfDay() * 1000L;

        LocalTime parsedEndTime = LocalTime.parse(end_time, timeFormatter);
        long endTimeMillis = parsedEndTime.toSecondOfDay() * 1000L;
        event
                .set("event_type_id", event_type_id)
                .set("title", title)
                .set("description", description)
                .set("no_of_guest", no_of_guest)
                .set("date", parsedDate)
                .set("start_time", startTimeMillis)
                .set("end_time", endTimeMillis)
                .set("address", address)
                .set("gift_suggestion", gift_suggestion)
                .set("location_latitude", latitude)
                .set("location_longitude", longitude);
        event.saveIt();

        // ✅ Notify guests only if saved
        // if (isSaved) {
        // List<Long> guestIds = Handler
        // .findAll("SELECT guest_id FROM event_invites WHERE event_id = ? AND guest_id
        // IS NOT NULL", id)
        // .stream().map(map -> map.getLong("guest_id"))
        // .collect(Collectors.toList());

        // if (guestIds != null && !guestIds.isEmpty()) {
        // for (Long guestId : guestIds) {
        // com.hionstudios.mypersonalinvite.model.Notification notification =
        // new com.hionstudios.mypersonalinvite.model.Notification();

        // notification
        // .set("sender_id", userId)
        // .set("receiver_id", guestId)
        // .set("event_id", id)
        // .set("notification_type_id", NotificationType.EVENT)
        // .set("content", "The event '" + title + "' has been updated.")
        // .set("is_read", false)
        // .set("href", "/events/" + id);

        // notification.insert();
        // }
        // }
        // }

        // ✅ Thumbnail update logic
        List<EventThumbnail> existingDbThumbs = EventThumbnail.where("event_id = ?", id);

        // First, collect all resource IDs from incoming thumbnail list
        Set<String> incomingThumbIds = new HashSet<>();
        List<MultipartFile> newFiles = new ArrayList<>();

        // Process incoming thumbnail list to separate existing IDs and new files
        if (thumbnail != null) {
            for (Object input : thumbnail) {
                if (input instanceof String) {
                    // String = existing resource ID → add to keep list
                    incomingThumbIds.add((String) input);
                } else if (input instanceof MultipartFile) {
                    // MultipartFile = new image → will upload later
                    newFiles.add((MultipartFile) input);
                }
            }
        }

        // Now process existing thumbnails - delete only those not in incoming list
        for (EventThumbnail dbThumb : existingDbThumbs) {
            String existingThumbId = dbThumb.getString("image");

            if (!incomingThumbIds.contains(existingThumbId)) {
                // This existing thumbnail is NOT in the incoming list → delete it
                WorkDrive.delete(existingThumbId);

                // Use delete() directly on the model instead of trying to modify frozen object
                dbThumb.delete(); // This should work as delete() doesn't require thaw()
            }
            // If it exists in incomingThumbIds, do nothing → it's preserved
        }

        // Process new files and upload them
        for (MultipartFile file : newFiles) {
            MapResponse response = WorkDrive.upload(file, Folder.MYPERSONALINVITE, false);
            String newResourceId = response != null ? response.getString("resource_id") : null;
            System.out.println("newResourceId" + newResourceId);

            if (newResourceId != null) {
                // Create a NEW EventThumbnail instance for insertion
                EventThumbnail newThumb = new EventThumbnail();
                newThumb.set("event_id", id);
                newThumb.set("image", newResourceId);
                newThumb.insert(); // Use insert() for new objects
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
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        String sql_1 = "Select Events.*, COALESCE(jsonb_agg(DISTINCT jsonb_build_object('image', Event_Thumbnails.image)) FILTER (WHERE Event_Thumbnails.Id IS NOT NULL), '[]') AS Thumbnails, COALESCE(jsonb_agg(DISTINCT jsonb_build_object('Planned_amount', Event_Budgets.Planned_Amount, 'Actual_Amount', Event_Budgets.Actual_Amount, 'budget_type', Budget_Types.Type)) FILTER (WHERE Event_Budgets.Id IS NOT NULL), '[]') AS Budgets From Events Left Join Event_Thumbnails On Events.Id = Event_Thumbnails.Event_Id Left Join Event_Budgets On Events.Id = Event_Budgets.Event_Id Left Join Budget_Types On Event_Budgets.Budget_Type_Id = Budget_Types.Id Where Events.Owner_Id = ? And Events.Date < ? Group By Events.Id Order By Events.Date Desc";

        long currentTime = TimeUtil.currentTime();
        List<MapResponse> events = Handler.findAll(sql_1, userId, currentTime);

        MapResponse response = new MapResponse().put("OwnersEvents", events);
        return response;
    }

    public MapResponse getAllEventsForOwners() {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        String sql_1 = "Select Events.*, COALESCE(jsonb_agg(Distinct jsonb_build_object('image', Event_Thumbnails.image)) Filter (Where Event_Thumbnails.Id Is Not Null), '[]') As Thumbnails, COALESCE(Json_agg(Distinct jsonb_build_object('amount', Event_Budgets.Planned_Amount, 'Actual_Amount', Event_Budgets.Actual_Amount, 'budget_type', Budget_Types.Type)) Filter (Where Event_Budgets.Id Is Not Null), '[]') As Budgets From Events Left Join Event_Thumbnails On Events.Id = Event_Thumbnails.Event_Id Left Join Event_Budgets On Events.Id = Event_Budgets.Event_Id Left Join Budget_Types On Event_Budgets.Budget_Type_Id = Budget_Types.Id Where Events.Owner_Id = ? Group By Events.Id Order By Events.Date Desc";

        String sql_2 = "Select Events.*, COALESCE(jsonb_agg(DISTINCT jsonb_build_object('image', Event_Thumbnails.image)) FILTER (WHERE Event_Thumbnails.Id IS NOT NULL), '[]') AS Thumbnails, COALESCE(jsonb_agg(DISTINCT jsonb_build_object('Planned_amount', Event_Budgets.Planned_Amount, 'Actual_Amount', Event_Budgets.Actual_Amount, 'budget_type', Budget_Types.Type)) FILTER (WHERE Event_Budgets.Id IS NOT NULL), '[]') AS Budgets From Events Left Join Event_Thumbnails On Events.Id = Event_Thumbnails.Event_Id Left Join Event_Budgets On Events.Id = Event_Budgets.Event_Id Left Join Budget_Types On Event_Budgets.Budget_Type_Id = Budget_Types.Id Where Events.Owner_Id = ? And Events.Date > ? Group By Events.Id Order By Events.Date Desc";

        String sql_3 = "Select Events.*, COALESCE(jsonb_agg(DISTINCT jsonb_build_object('image', Event_Thumbnails.image)) FILTER (WHERE Event_Thumbnails.Id IS NOT NULL), '[]') AS Thumbnails, COALESCE(jsonb_agg(DISTINCT jsonb_build_object('Planned_amount', Event_Budgets.Planned_Amount, 'Actual_Amount', Event_Budgets.Actual_Amount, 'budget_type', Budget_Types.Type)) FILTER (WHERE Event_Budgets.Id IS NOT NULL), '[]') AS Budgets From Events Left Join Event_Thumbnails On Events.Id = Event_Thumbnails.Event_Id Left Join Event_Budgets On Events.Id = Event_Budgets.Event_Id Left Join Budget_Types On Event_Budgets.Budget_Type_Id = Budget_Types.Id Where Events.Owner_Id = ? And Events.Date < ? Group By Events.Id Order By Events.Date Desc";

        List<MapResponse> allEvents = Handler.findAll(sql_1, userId);

        long currentTime = TimeUtil.currentTime();
        List<MapResponse> upcomingEvents = Handler.findAll(sql_2, userId, currentTime);
        List<MapResponse> completedEvents = Handler.findAll(sql_3, userId, currentTime);

        return new MapResponse()
                .put("allEvents", allEvents)
                .put("upcomingEvents", upcomingEvents)
                .put("completedEvents", completedEvents);
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
        String name = UserUtil.getName();

        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        EventInvite invite = EventInvite.findFirst("event_id = ? AND guest_id = ?", id, userId);
        if (invite == null) {
            return MapResponse.failure("They are not invited to this event");
        }

        Event event = Event.findById(id);

        invite.set("rsvp_status_id", rsvp);
        boolean isInserted = invite.saveIt();

        if (isInserted) {
            Notification notification = new Notification();
            notification.set("sender_id", userId);
            notification.set("receiver_id", event.getLong("owner_id"));
            notification.set("event_id", id);
            notification.set("notification_type_id", NotificationType.getId(NotificationType.RSVP));
            notification.set("content", name + " " + "responded to your invite");
            notification.set("is_read", false);
            notification.set("href", "/rsvp-tracking/" + id);
            notification.insert();

            if (event != null) {
                Long ownerId = event.getLong("owner_id");
                if (ownerId != null && !ownerId.equals(userId)) {
                    // Build notification content
                    User user = User.findById(userId);
                    String userName = user != null ? user.getString("name") : "A guest";
                    String eventTitle = event.getString("title");
                    String notifTitle = "RSVP Updated";
                    String notifBody = userName + " has updated their RSVP for " + eventTitle;
                    // String notifLink = "/events/" + id + "/guests";

                    // Send push notification to owner
                    try {
                        List<FcmDeviceToken> tokens = FcmDeviceToken.where(
                                "user_id = ? AND fcm_token IS NOT NULL AND fcm_token <> ''",
                                ownerId);

                        for (FcmDeviceToken token : tokens) {
                            String fcmToken = token.getString("fcm_token");
                            if (fcmToken == null || fcmToken.isEmpty())
                                continue;

                            try {
                                firebaseNotificationService.sendNotification(
                                        fcmToken,
                                        notifTitle,
                                        notifBody);
                            } catch (Exception e) {
                                System.err.println("FCM send failed for token " + fcmToken + ": " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error sending notification to event owner: " + e.getMessage());
                    }
                }
            }
        }

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

        String sql = "Select Event_Budgets.*, Budget_Types.Type As budget_type From Event_Budgets Join Budget_Types On Budget_Types.Id = Event_Budgets.Budget_Type_Id Where Event_id = ?";
        List<MapResponse> budgets = Handler.findAll(sql, id);

        double totalActualBudget = 0.0;
        for (MapResponse budget : budgets) {
            Object actualAmountObj = budget.get("actual_amount");
            if (actualAmountObj != null) {
                try {
                    totalActualBudget += Double.parseDouble(actualAmountObj.toString());
                } catch (NumberFormatException e) {
                }
            }
        }

        return MapResponse.success()
                .put("BudgetList", budgets)
                .put("total_actual_budget", totalActualBudget);

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

                phone = phone.trim();
                if (phone.startsWith("+")) {
                    phone = phone.substring(1); // remove '+'
                }

                // If the phone number has 12 digits and starts with 91, remove the prefix
                if (phone.length() == 12 && phone.startsWith("91")) {
                    phone = phone.substring(2);
                }

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
                        String eventLink = "https://mypersonalinvite.com/events/" + id;
                        String msg = "Hi " + name + "! 🎉 You’ve been invited to our event.\nView details: "
                                + eventLink;
                        WhatsAppUtil.sendWhatsAppMessage(phone, msg);
                        try {
                            // Fetch all non-empty tokens for this user
                            List<FcmDeviceToken> tokens = FcmDeviceToken.where(
                                    "user_id = ? AND fcm_token IS NOT NULL AND fcm_token <> ''",
                                    user.getId());

                            if (tokens != null && !tokens.isEmpty()) {
                                // Minimal title/body; keep short to avoid OS truncation
                                // String eventPath =
                                final String notifTitle = "New Event Invite";
                                final String notifBody = "You've been invited to an event: " + eventLink;

                                for (FcmDeviceToken t : tokens) {
                                    final String fcmToken = t.getString("fcm_token");
                                    try {
                                        firebaseNotificationService.sendNotification(fcmToken, notifTitle, notifBody);
                                    } catch (Exception ex) {
                                        // Do not fail the flow for a single-device push failure; log and continue
                                        // Replace with your logger
                                        System.err.println(
                                                "FCM send failed for token " + fcmToken + ": " + ex.getMessage());
                                    }
                                }
                            } else {
                                // No device tokens; optional: log or enqueue for later
                                System.out.println("No FCM tokens for user " + user.getId() + "; skipping push.");
                            }
                        } catch (Exception e) {
                            // Guardrail to prevent push issues from breaking the main flow
                            System.err.println("Push notification error: " + e.getMessage());
                        }

                        Event event = new Event();
                        String title = event.getString("title");

                        Notification notification = new Notification();
                        notification.set("sender_id", userId);
                        notification.set("receiver_id", user.getId());
                        notification.set("event_id", id);
                        notification.set("notification_type_id", NotificationType.getId(NotificationType.EVENT));
                        notification.set("content", "You are invited to this event." + title);
                        notification.set("is_read", false);
                        notification.set("href", "/user-event-details/" + id);
                        notification.insert();
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
                    String eventLink = "https://mypersonalinvite.com/events/" + id;
                    String msg = "Hi " + name + "! 🎉 You’ve been invited to our event.\nView details: "
                            + eventLink;
                    WhatsAppUtil.sendWhatsAppMessage(phone, msg);
                    try {
                        // Fetch all non-empty tokens for this user
                        List<FcmDeviceToken> tokens = FcmDeviceToken.where(
                                "user_id = ? AND fcm_token IS NOT NULL AND fcm_token <> ''",
                                user.getId());

                        if (tokens != null && !tokens.isEmpty()) {
                            // Minimal title/body; keep short to avoid OS truncation
                            // String eventPath =
                            final String notifTitle = "New Event Invite";
                            final String notifBody = "You've been invited to an event: " + eventLink;

                            for (FcmDeviceToken t : tokens) {
                                final String fcmToken = t.getString("fcm_token");
                                try {
                                    firebaseNotificationService.sendNotification(fcmToken, notifTitle, notifBody);
                                } catch (Exception ex) {
                                    // Do not fail the flow for a single-device push failure; log and continue
                                    // Replace with your logger
                                    System.err.println(
                                            "FCM send failed for token " + fcmToken + ": " + ex.getMessage());
                                }
                            }
                        } else {
                            // No device tokens; optional: log or enqueue for later
                            System.out.println("No FCM tokens for user " + user.getId() + "; skipping push.");
                        }
                    } catch (Exception e) {
                        // Guardrail to prevent push issues from breaking the main flow
                        System.err.println("Push notification error: " + e.getMessage());
                    }

                    try {
                        // Check if user has an email
                        String guestEmail = user.getString("email");
                        if (guestEmail != null && !guestEmail.isEmpty()) {

                            // Find the event details
                            Event event = Event.findById(id);
                            if (event != null) {

                                GoogleOauth ownerOauth = GoogleOauth.findFirst("user_id = ?", user.getLong("id"));

                                if (ownerOauth != null) {
                                    // Build credential for owner
                                    Credential credential = googleService.buildCredentialFromTokens(
                                            ownerOauth.getString("access_token"),
                                            ownerOauth.getString("refresh_token"),
                                            ownerOauth.getLong("expiry"));

                                    // Optionally refresh token if needed
                                    try {
                                        Long secs = credential.getExpiresInSeconds();
                                        if (secs == null || secs <= 60) {
                                            System.out.println("Access token expired or near expiry. Refreshing...");
                                        } else {
                                            System.out.println("Access token still valid for " + secs
                                                    + "s, but forcing refresh just in case...");
                                        }

                                        boolean refreshed = credential.refreshToken();
                                        System.out.println("Token refresh attempted, result: " + refreshed);

                                        if (refreshed) {
                                            ownerOauth.set("access_token", credential.getAccessToken())
                                                    .set("refresh_token", credential.getRefreshToken())
                                                    .set("expiry", credential.getExpirationTimeMilliseconds())
                                                    .saveIt();
                                        } else {
                                            System.err.println(
                                                    "Refresh token invalid — user must reconnect Google account.");
                                        }
                                    } catch (Exception e) {
                                        System.err.println("Error refreshing token: " + e.getMessage());
                                    }
                                    // Create Google Calendar event for this guest
                                    try {
                                        String eventTitle = event.getString("title");
                                        String eventDescription = "You’ve been invited to the event: " + eventTitle;
                                        long start_time = event.getLong("start_time"); // e.g., 64800000 (6 PM)
                                        long end_time = event.getLong("end_time"); // e.g., 75600000 (9 PM)
                                        long date = event.getLong("date"); // e.g., 1762605164000

                                        // Convert to RFC3339 correctly
                                        String startTime = TimeUtil.toRFC3339FromDateAndTime(date, start_time);
                                        String endTime = TimeUtil.toRFC3339FromDateAndTime(date, end_time);

                                        System.out.println("Start Time (RFC3339): " + startTime);
                                        System.out.println("End Time (RFC3339): " + endTime);
                                        googleService.createCalendarEvent(
                                                credential,
                                                eventTitle,
                                                eventDescription,
                                                startTime,
                                                endTime,
                                                List.of(guestEmail) // Add guest’s email to calendar invite
                                        );

                                        System.out.println("Google Calendar invite sent to " + guestEmail);
                                    } catch (Exception ex) {
                                        System.err.println("Failed to create Google Calendar event for guest "
                                                + guestEmail + ": " + ex.getMessage());
                                    }
                                } else {
                                    System.out.println(
                                            "Event owner has not connected Google Calendar, skipping invite for "
                                                    + guestEmail);
                                }
                            }
                        } else {
                            System.out.println("Guest has no email, skipping Google Calendar invite.");
                        }
                    } catch (Exception e) {
                        System.err.println("Error adding Google Calendar invite: " + e.getMessage());
                    }

                    Event event = Event.findById(id);
                    String title = event.getString("title");

                    Notification notification = new Notification();
                    notification.set("sender_id", userId);
                    notification.set("receiver_id", user.getId());
                    notification.set("event_id", id);
                    notification.set("notification_type_id", NotificationType.getId(NotificationType.EVENT));
                    notification.set("content", "You are invited to this event:" + title);
                    notification.set("is_read", false);
                    notification.set("href", "/user-event-details/" + id);
                    notification.insert();
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
                        String eventLink = "https://mypersonalinvite.com/events/" + id;
                        String msg = "Hi " + name + "! 🎉 You’ve been invited to our event.\nView details: "
                                + eventLink;
                        WhatsAppUtil.sendWhatsAppMessage(phone, msg);
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

    public MapResponse addBudget(Long id, Long budget_type_id, Long planned_amount, Long actual_amount) {
        EventBudget budget = new EventBudget();
        budget.set("event_id", id);
        budget.set("budget_type_id", budget_type_id);
        budget.set("planned_amount", planned_amount);
        budget.set("actual_amount", actual_amount);
        budget.insert();
        return MapResponse.success("Budget item added");
    }

    public MapResponse updateBudget(Long id, Long budget_type_id, Long planned_amount, Long actual_amount) {
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
        String sql = "Select Event_Invites.*, Rsvp_Statuses.Status, Users.Name, Users.Phone_Number, Users.Profile_Pic, Carpools.Id As Carpool_Id From Event_Invites Join Users On Users.Id = Event_Invites.Guest_Id Join Rsvp_Statuses On Rsvp_Statuses.Id = Event_Invites.Rsvp_Status_Id Left Join Carpools On Carpools.Event_Id = Event_Invites.Event_Id And Carpools.User_Id = Event_Invites.Guest_Id Where Event_Invites.Event_Id = ? Order By Event_Invites.Created_Time Desc";

        List<MapResponse> guest = Handler.findAll(sql, id);
        MapResponse response = new MapResponse().put("GuestList", guest);
        return response;
    }

    public MapResponse getEventTypes() {
        String sql = "Select * From Event_Types Order By Type Asc";
        List<MapResponse> eventTypes = Handler.findAll(sql);
        MapResponse response = new MapResponse().put("EventTypes", eventTypes);
        return response;
    }

    public MapResponse getRsvpStatuses() {
        String sql = "Select * From Rsvp_Statuses Order By Status Asc";
        List<MapResponse> RsvpTypes = Handler.findAll(sql);
        MapResponse response = new MapResponse().put("RsvpTypes", RsvpTypes);
        return response;
    }

    public MapResponse getBudgetTypes() {
        String sql = "Select * From Budget_Types Order By Id Asc";
        List<MapResponse> budgetTypes = Handler.findAll(sql);
        MapResponse response = new MapResponse().put("BudgetTypes", budgetTypes);
        return response;
    }

}
