package com.hionstudios;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class WhatsAppUtil {
    private static final String TOKEN = "EAADIZA6PzKO0BP9MXBZCMzAtSDo9VFOKartgySZC5KxjFZApHM6su1508r4ZBROHZCp5dp8j4vJcdvHa4Y0qqsH3qB16B6L41ZCm5hOpdVGqYvi9cIFOBtZA3zhsKW7H8lnZAN9MQOM5wbbfpqw8ts7VCjWwxvfEZBpeQnXSN9hvm5urF09jCWkeKz4mxRJZCLfdIRTGwpB7gZDZD";
    private static final String PHONE_NUMBER_ID = "535677082966022";

    public static void sendWhatsAppMessage(String phoneNumber, String messageBody) {
        String url = "https://graph.facebook.com/v22.0/" + PHONE_NUMBER_ID + "/messages";

        Map<String, Object> text = new HashMap<>();
        text.put("body", messageBody);

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", phoneNumber);
        payload.put("type", "text");
        payload.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TOKEN);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            System.out.println("WhatsApp API Response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Error sending WhatsApp message: " + e.getMessage());
        }
    }
}
