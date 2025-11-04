package com.hionstudios;

import org.springframework.stereotype.Service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
public class FirebaseNotificationService {

    private final FirebaseApp firebaseApp;

    public FirebaseNotificationService(FirebaseApp firebaseApp) {
        this.firebaseApp = firebaseApp;
    }

    public void sendNotification(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            // Use the injected FirebaseApp
            String response = FirebaseMessaging.getInstance(firebaseApp).send(message);
            System.out.println("Sent message: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
