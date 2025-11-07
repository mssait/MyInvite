package com.hionstudios.oauth;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;

import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleOauthService {
        private static final String CREDENTIALS_FILE_PATH = "googleOauth.json";
        private static final String REDIRECT_URI = "http://localhost:8080/oauth2/callback";
        private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

        private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        private static final com.google.api.client.http.HttpTransport HTTP_TRANSPORT;

        static {
                try {
                        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                } catch (GeneralSecurityException | java.io.IOException e) {
                        throw new RuntimeException("Failed to initialize HTTP_TRANSPORT", e);
                }
        }

        private final GoogleClientSecrets clientSecrets;
        private final GoogleAuthorizationCodeFlow flow;

        public GoogleOauthService() throws Exception {
                // Load client secrets
                InputStream in = getClass().getClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH);
                if (in == null) {
                        throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
                }

                clientSecrets = GoogleClientSecrets.load(
                                JacksonFactory.getDefaultInstance(),
                                new InputStreamReader(in));

                // Initialize the flow
                flow = new GoogleAuthorizationCodeFlow.Builder(
                                GoogleNetHttpTransport.newTrustedTransport(),
                                JacksonFactory.getDefaultInstance(),
                                clientSecrets,
                                SCOPES)
                                .setDataStoreFactory(MemoryDataStoreFactory.getDefaultInstance())
                                .setAccessType("offline")
                                .build();
        }

        /** Step 1: Generate Google OAuth Consent URL */
        public String getAuthorizationUrl(String userId) {
                return flow.newAuthorizationUrl()
                                .setRedirectUri(REDIRECT_URI)
                                .setState(userId)
                                .setAccessType("offline")
                                .set("prompt", "consent")
                                .build();
        }

        /** Step 2: Exchange code for tokens */
        public Credential getCredentialsFromCode(String code, String userId) throws Exception {
                GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                                .setRedirectUri(REDIRECT_URI)
                                .execute();

                return flow.createAndStoreCredential(tokenResponse, userId);
        }

        /** Step 3: Create and insert calendar event */
        public void createCalendarEvent(Credential credential, String summary, String description,
                        String startTime, String endTime, List<String> attendeeEmails) throws Exception {

                Calendar service = new Calendar.Builder(
                                GoogleNetHttpTransport.newTrustedTransport(),
                                JacksonFactory.getDefaultInstance(),
                                credential).setApplicationName("My Invite").build();

                String calendarId = "primary";
                com.google.api.services.calendar.model.Calendar calendar = service.calendars().get(calendarId)
                                .execute();
                System.out.println("Authorized as: " + calendar.getSummary());

                // Create event attendees
                List<EventAttendee> attendees = null;
                if (attendeeEmails != null) {
                        attendees = attendeeEmails.stream()
                                        .map(email -> new EventAttendee().setEmail(email))
                                        .collect(Collectors.toList());
                }

                Event event = new Event()
                                .setSummary(summary)
                                .setDescription(description)
                                .setStart(new EventDateTime()
                                                .setDateTime(new com.google.api.client.util.DateTime(startTime)))
                                .setEnd(new EventDateTime()
                                                .setDateTime(new com.google.api.client.util.DateTime(endTime)));

                if (attendees != null) {
                        event.setAttendees(attendees);
                }

                service.events().insert("primary", event).execute();
        }

        /** Additional method: Get stored credentials for a user */
        public Credential getStoredCredentials(String userId) throws Exception {
                return flow.loadCredential(userId);
        }

        /** Additional method: Check if user has valid credentials */
        public boolean hasValidCredentials(String userId) throws Exception {
                Credential credential = flow.loadCredential(userId);
                return credential != null && credential.getAccessToken() != null &&
                                (credential.getRefreshToken() != null || !credential.getExpiresInSeconds().equals(0L));
        }

        public Credential buildCredentialFromTokens(String accessToken, String refreshToken, Long expiry)
                        throws IOException, GeneralSecurityException {

                // Load client secrets from resources
                InputStream in = getClass().getResourceAsStream("/googleOauth.json");
                if (in == null) {
                        throw new IOException("Resource not found: /googleOauth.json");
                }

                GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

                // Build credential using GoogleCredential
                return new GoogleCredential.Builder()
                                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                                .setJsonFactory(JSON_FACTORY)
                                .setClientSecrets(clientSecrets)
                                .build()
                                .setAccessToken(accessToken)
                                .setRefreshToken(refreshToken)
                                .setExpirationTimeMilliseconds(expiry);
        }

}