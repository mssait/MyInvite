package com.hionstudios.mypersonalinvite.Flow;

import java.util.List;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.iam.UserUtil;
// import com.hionstudios.iam.UserUtil;
import com.hionstudios.mypersonalinvite.model.FcmDeviceToken;
import com.hionstudios.mypersonalinvite.model.Notification;

public class NotificationFlow {

    public MapResponse addFcmToken(String fcm_token) {
        long userId = UserUtil.getUserid();

        // Check if this user already has a token saved
        FcmDeviceToken fcm = FcmDeviceToken.findFirst("user_id = ?", userId);

        if (fcm != null) {
            // Update the existing token if it's different
            if (!fcm_token.equals(fcm.getString("fcm_token"))) {
                fcm.set("fcm_token", fcm_token);
                return fcm.saveIt() ? MapResponse.success("Token updated") : MapResponse.failure("Update failed");
            }
            // Token is the same — no need to update
            return MapResponse.success("Token already up-to-date");
        }

        // If no record found, insert a new one
        fcm = new FcmDeviceToken();
        fcm.set("fcm_token", fcm_token);
        fcm.set("user_id", userId);

        return fcm.insert() ? MapResponse.success("Token saved") : MapResponse.failure("Insert failed");
    }

    public MapResponse getFcmToken() {

        long id = UserUtil.getUserid();

        String sql = "Select Fcm_Token from Fcm_Tokens Where UserId = ";
        MapResponse fcmToken = Handler.findFirst(sql, id);

        return fcmToken;
    }

    // public MapResponse sendNotification(long userId, String title, String body) {

    // FcmDeviceToken token = FcmDeviceToken.findFirst("user_id = ?", userId);
    // MapResponse response = new MapResponse().put("fcm", token);

    // if (response == null) {
    // return MapResponse.failure("No FCM token found for user.");
    // }
    // notificationService.sendNotification(response.getString("fcm_token"), title,
    // body);
    // return MapResponse.success("Notification sent.");
    // }

    public MapResponse getUserNotifications() {

        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        // Fetch unread notifications
        List<MapResponse> unreadList = Handler.findAll(
                "Select Notifications.*, Notification_Types.Type, Users.Name From Notifications Join Notification_Types On Notification_Types.Id = Notifications.Notification_Type_Id Join Users On Users.Id = Notifications.Sender_Id Where Notifications.Receiver_Id = ? And Notifications.Is_Read = ? Order By Notifications.Time DESC",
                userId, false);

        // Fetch read notifications
        List<MapResponse> readList = Handler.findAll(
                "Select Notifications.*, Notification_Types.Type, Users.Name From Notifications Join Notification_Types On Notification_Types.Id = Notifications.Notification_Type_Id Join Users On Users.Id = Notifications.Sender_Id Where Notifications.Receiver_Id = ? And Notifications.Is_Read = ? Order By Notifications.Time DESC",
                userId, true);

        int unreadCount = unreadList.size();
        int readCount = readList.size();

        return MapResponse.success()
                .put("unread_count", unreadCount)
                .put("read_count", readCount)
                .put("unread_notifications", unreadList)
                .put("read_notifications", readList);
    }

    public MapResponse markAllAsRead() {
        Long userId = UserUtil.getUserid();
        if (userId == null || userId <= 0)
            return MapResponse.failure("User not authenticated");

        // Fetch all unread notifications for the user
        List<Notification> unreadNotifications = Notification.where("Receiver_Id = ? And Is_Read = ?", userId, false);

        if (unreadNotifications.isEmpty()) {
            return MapResponse.success("No unread notifications found").put("updated_count", 0);
        }

        // Mark each notification as read
        for (Notification notif : unreadNotifications) {
            notif.set("is_read", true);
            notif.saveIt();
        }

        return MapResponse.success("Marked all as read")
                .put("updated_count", unreadNotifications.size());
    }

}
