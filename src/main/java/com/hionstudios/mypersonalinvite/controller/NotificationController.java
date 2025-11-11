package com.hionstudios.mypersonalinvite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hionstudios.FirebaseNotificationService;
import com.hionstudios.MapResponse;
import com.hionstudios.db.DbTransaction;
import com.hionstudios.iam.IsUser;
import com.hionstudios.mypersonalinvite.Flow.NotificationFlow;
import com.hionstudios.mypersonalinvite.model.FcmDeviceToken;

@RestController
@RequestMapping("api/notification")
public class NotificationController {

    @Autowired
    private FirebaseNotificationService notificationService;

    // @PostMapping("/send")
    // public ResponseEntity<String> send(
    // @RequestParam long userId,
    // @RequestParam String title,
    // @RequestParam String body) {
    // return ((DbTransaction) () -> {
    // FcmDeviceToken token = FcmDeviceToken.findFirst("user_id = ?", userId);
    // if (token == null) {
    // return "No token found for userId: " + userId;
    // }
    // notificationService.sendNotification(token.getString("fcm_token"), title,
    // body);
    // return "Notification sent!";
    // }).read(); // or .write() if you ever do inserts/updates here
    // }

    // @PostMapping(path = "/send", consumes =
    // MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    // public ResponseEntity<MapResponse> sendNotification(
    // @RequestParam long userId,
    // @RequestParam String title,
    // @RequestParam String body) {

    // return ((DbTransaction) () -> {
    // FcmDeviceToken token = FcmDeviceToken.findFirst("user_id = ?", userId);
    // if (token == null)
    // return MapResponse.failure("No FCM token found for user.");

    // String fcm = token.getString("fcm_token");
    // if (fcm == null || fcm.isBlank())
    // return MapResponse.failure("Empty FCM token for user.");

    // notificationService.sendNotification(fcm, title, body); // controller has
    // @Autowired service
    // return MapResponse.success("Notification sent.");
    // }).read();
    // }

    // Explicit path + consumes + param name
    @PostMapping(path = "/fcm-tokens", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<MapResponse> addFcmToken(@RequestParam("fcm_token") String fcmToken) {
        return ((DbTransaction) () -> new NotificationFlow().addFcmToken(fcmToken)).write();
    }

    @GetMapping("fcm-tokens")
    public ResponseEntity<MapResponse> getFcmToken() {
        return ((DbTransaction) () -> new NotificationFlow().getFcmToken()).read();
    }

    @GetMapping("all")
    // @IsUser
    public ResponseEntity<MapResponse> getUnreadNotifications() {
        return ((DbTransaction) () -> new NotificationFlow().getUserNotifications()).read();
    }

    @PutMapping("mark-read")
    @IsUser
    public ResponseEntity<MapResponse> markAllAsRead() {
        return ((DbTransaction) () -> new NotificationFlow().markAllAsRead()).write();
    }

    @PutMapping("{id}/mark-read")
    @IsUser
    public ResponseEntity<MapResponse> markAsRead(@PathVariable long id) {
        return ((DbTransaction) () -> new NotificationFlow().markAsRead(id)).write();
    }

}
