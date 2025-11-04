package com.hionstudios.mypersonalinvite.Flow;

import org.springframework.beans.factory.annotation.Autowired;

import com.hionstudios.FirebaseNotificationService;
import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.iam.UserUtil;
// import com.hionstudios.iam.UserUtil;
import com.hionstudios.mypersonalinvite.model.FcmDeviceToken;

public class NotificationFlow {

    @Autowired
    private FirebaseNotificationService notificationService;

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

    public MapResponse sendNotification(long userId, String title, String body) {

        FcmDeviceToken token = FcmDeviceToken.findFirst("user_id = ?", userId);
        MapResponse response = new MapResponse().put("fcm", token);

        if (response == null) {
            return MapResponse.failure("No FCM token found for user.");
        }
        notificationService.sendNotification(response.getString("fcm_token"), title, body);
        return MapResponse.success("Notification sent.");
    }
}
