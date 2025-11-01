package com.hionstudios.mypersonalinvite.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hionstudios.FirebaseNotification;
import com.hionstudios.MapResponse;
import com.hionstudios.db.DbTransaction;
import com.hionstudios.mypersonalinvite.Flow.NotificationFlow;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("api/notification")
public class NotificationController {

    private final FirebaseNotification notificationService;

    public NotificationController(FirebaseNotification notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<String> sendNotification(@RequestBody Map<String, String> data) {
        String token = data.get("token");
        String title = data.get("title");
        String body = data.get("body");

        notificationService.sendNotification(token, title, body);
        return ResponseEntity.ok("Notification sent successfully!");
    }

    @PostMapping("fcm-tokens")
    public ResponseEntity<MapResponse> addFcmToken(
            @PathVariable long id,
            @RequestParam String fcm_token
    ) {
        return ((DbTransaction) () -> new NotificationFlow().addFcmToken(id, fcm_token)).write();
    }

    @GetMapping("fcm-tokens")
    public ResponseEntity<MapResponse> getFcmToken() {
        return ((DbTransaction) () -> new NotificationFlow().getFcmToken()).read();
    }
    

}
