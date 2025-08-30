package com.hionstudios.iam;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.mypersonalinvite.Mail.MailUtil;
import com.hionstudios.mypersonalinvite.model.User;

public class UserUtil {
    private static Object getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getPrincipal() : null;
    }

    public static HionUserDetails getUserDetails() {
        Object principal = getPrincipal();
        return (principal instanceof HionUserDetails) ? (HionUserDetails) principal : new HionUserDetails();
    }

    public static boolean isLoggedIn() {
        return getPrincipal() instanceof HionUserDetails;
    }

    public static long getUserid() {
        return getUserDetails().getUserid();
    }

    public static String getUsername() {
        return getUserDetails().getUsername();
    }

    public static MapResponse forgotPassword(String username) {
        if (username == null || !username.contains("-")) {
            MapResponse.failure("Invalid username format.");
        }

        String[] parts = username.split("-");
        if (parts.length < 2) {
            return MapResponse.failure("Invalid username format.");
        }

        String email = parts[0];
        int typeId;

        try {
            typeId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return MapResponse.failure("Invalid type ID.");
        }

        String sql = "Select * From Users Where Email = ? And Type_Id = ?";
        MapResponse user = Handler.findFirst(sql, email, typeId);

        if (user == null) {
            return MapResponse.success("If the email exists, a reset link has been sent.");
        }

        String token = UUID.randomUUID().toString();
        long expiry = System.currentTimeMillis() + 1000 * 60 * 30; // 30 mins

        User users = User.findById(user.getLong("id"));
        users.set("password_reset_token", token);
        users.set("password_reset_token_expiry", expiry);
        users.save();

        String resetLink = "https://localhost:3000/reset-password?token=" + token;
        MailUtil.sendResetPasswordLinkEmail(email, resetLink);
        return MapResponse.success("Reset link sent successfully.");
    }

    public static MapResponse resetPassword(String token, String password) {
        String sql = "Select * From Users Where Password_Reset_Token = ?";

        MapResponse user = Handler.findFirst(sql, token);
        Long expiry = user != null ? user.getLong("password_reset_token_expiry") : null;
        if (user == null || expiry == null || expiry < System.currentTimeMillis()) {
            return MapResponse.failure("Password reset link expired or invalid");
        }
        User users = User.findById(user.getLong("id"));
        users.set("password", password);
        users.set("password_reset_token", null);
        users.set("password_reset_token_expiry", null);

        return users.save() ? MapResponse.success() : MapResponse.failure();

    }
}
