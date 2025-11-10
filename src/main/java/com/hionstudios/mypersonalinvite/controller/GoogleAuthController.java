package com.hionstudios.mypersonalinvite.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.api.client.auth.oauth2.Credential;
import com.hionstudios.MapResponse;
import com.hionstudios.db.DbUtil;
import com.hionstudios.mypersonalinvite.model.GoogleOauth;
import com.hionstudios.mypersonalinvite.model.User;
import com.hionstudios.oauth.GoogleOauthService;

@RestController
@RequestMapping
public class GoogleAuthController {

    private final GoogleOauthService googleService;

    @Autowired
    public GoogleAuthController(GoogleOauthService googleService) {
        this.googleService = googleService;
    }

    /** Step 1: Redirect user to Google for authentication */
    @GetMapping("/google/authorize/{userId}")
    public void authorize(@PathVariable String userId, HttpServletResponse response) throws Exception {
        String url = googleService.getAuthorizationUrl(userId);
        response.sendRedirect(url);
    }

    /** Step 2: Handle callback after user grants access */
    @GetMapping("/oauth2/callback")
    public ResponseEntity<MapResponse> oauthCallback(@RequestParam("code") String code,
            @RequestParam("state") String userId) throws Exception {

        try {
            DbUtil.openTransaction();
            Long user_Id = Long.parseLong(userId);
            Credential credential = googleService.getCredentialsFromCode(code, userId);
            if (credential == null || credential.getAccessToken() == null) {
                throw new RuntimeException("Failed to retrieve valid Google credentials");
            }

            Oauth2 oauth2 = new Oauth2.Builder(
                    googleService.getHttpTransport(),
                    googleService.getJsonFactory(),
                    credential)
                    .setApplicationName("My Invite")
                    .build();

            // CORRECTED: This will now work with the right import
            Userinfo userInfo = oauth2.userinfo().get()
                    .setOauthToken(credential.getAccessToken())
                    .execute();

            String googleEmail = userInfo.getEmail();
            System.out.println("Connected Google email: " + googleEmail);

            User user = User.findById(user_Id);
            if (user != null) {
                user.set("email", googleEmail);
                user.saveIt();
            }

            GoogleOauth oauth = GoogleOauth.findFirst("user_id = ?", user_Id);
            // Store or update access + refresh tokens in DB
            if (oauth == null) {
                oauth = new GoogleOauth()
                        .set("user_id", user_Id)
                        .set("access_token", credential.getAccessToken())
                        .set("refresh_token", credential.getRefreshToken())
                        .set("expiry", credential.getExpirationTimeMilliseconds());
                oauth.insert();
            } else {
                oauth.set("access_token", credential.getAccessToken())
                        .set("refresh_token", credential.getRefreshToken())
                        .set("expiry", credential.getExpirationTimeMilliseconds())
                        .saveIt();
            }

            DbUtil.commitTransaction();
            return ResponseEntity.ok(MapResponse.success("Google Calendar access granted for user: " + userId));

        } catch (Exception e) {
            DbUtil.rollback();
            e.printStackTrace();
            return ResponseEntity.ok(MapResponse.failure(e.getMessage()));
        } finally {
            DbUtil.close();
        }
    }

    /** Step 3: Create event (example call) */
    @PostMapping("/event")
    public String createEvent(@RequestParam String userId) throws Exception {
        // Retrieve user's stored credential (for demo, we’ll skip DB)
        Credential credential = googleService.getStoredCredentials(userId);

        if (credential == null) {
            // Fallback to DB
            GoogleOauth oauth = GoogleOauth.findFirst("user_id = ?", userId);
            if (oauth == null) {
                return "No Google OAuth tokens found for user: " + userId;
            }
            credential = googleService.buildCredentialFromTokens(
                    oauth.getString("access_token"),
                    oauth.getString("refresh_token"),
                    oauth.getLong("expiry"));
        }

        // Try refreshing if about to expire / expired and we have a refresh token
        try {
            Long secs = credential.getExpiresInSeconds();
            if (secs == null || secs <= 60) { // nearly expired / unknown
                if (credential.getRefreshToken() != null) {
                    boolean refreshed = credential.refreshToken();
                    if (refreshed) {
                        // persist new tokens back to DB
                        GoogleOauth oauth = GoogleOauth.findFirst("user_id = ?", userId);
                        if (oauth != null) {
                            oauth.set("access_token", credential.getAccessToken())
                                    .set("refresh_token", credential.getRefreshToken()) // may or may not change
                                    .set("expiry", credential.getExpirationTimeMilliseconds())
                                    .saveIt();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // If refresh fails, we still attempt with existing token; handle 401 upstream
            // if needed
        }

        // Create event (demo values, replace as needed)
        googleService.createCalendarEvent(
                credential,
                "My Invite - Demo Event",
                "Event created via My Invite",
                "2025-11-10T10:00:00+05:30",
                "2025-11-10T11:00:00+05:30",
                List.of("guest1@example.com", "guest2@example.com"));

        return "Event added to Google Calendar!";
    }
}
