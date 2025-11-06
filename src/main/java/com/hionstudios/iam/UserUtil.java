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

    public static MapResponse forgotPassword(String phone_number) {

        String sql = "Select * From Users Where Phone_Number = ? ";
        MapResponse user = Handler.findFirst(sql, phone_number);

        if (user == null) {
            return MapResponse.failure("Phone number not found!");
        }

        int otp = (int) (Math.random() * 900000) + 100000;
        long expiry = System.currentTimeMillis() + (30 * 60 * 1000); // 30 mins

        User users = User.findById(user.getLong("id"));
        users.set("password_reset_otp", String.valueOf(otp));
        users.set("password_reset_otp_expiry", expiry);

        if (!users.save()) {
            return MapResponse.failure("Failed to generate OTP");
        }

        // String resetLink = "https://localhost:3000/reset-password?token=" + token;
        // MailUtil.sendResetPasswordLinkEmail(phone_number, resetLink);
        return MapResponse.success("Otp Sent to your Mobile number.");
    }

    public static MapResponse resetPassword(String otp, String password) {

        String sql = "SELECT * FROM users WHERE password_reset_otp = ?";
        MapResponse user = Handler.findFirst(sql, otp);

        if (user == null) {
            return MapResponse.failure("Invalid OTP");
        }

        Long expiry = user.getLong("password_reset_otp_expiry");
        if (expiry == null || expiry < System.currentTimeMillis()) {
            return MapResponse.failure("OTP has expired");
        }

        User users = User.findById(user.getLong("id"));
        users.set("password", password);
        users.set("password_reset_otp", null);
        users.set("password_reset_otp_expiry", null);

        return users.save() ? MapResponse.success() : MapResponse.failure();

    }

    public static MapResponse changePassword(String old_password, String new_password) {

        Long userId = getUserid();
        String sql = "Select * From Users Where Id = ?";

        MapResponse user = Handler.findFirst(sql, userId);
        if (user == null || !user.getString("password").equals(old_password)) {
            return MapResponse.failure("Old password is incorrect");
        }
        User users = User.findById(user.getLong("id"));
        users.set("password", new_password);
        return users.save() ? MapResponse.success() : MapResponse.failure();
    }
}
