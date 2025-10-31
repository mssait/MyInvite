package com.hionstudios.mypersonalinvite.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hionstudios.FirebaseNotification;

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

}
