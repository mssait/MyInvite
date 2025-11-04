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
            return MapResponse.success("Phone numbr not found!");
        }

        String token = UUID.randomUUID().toString();
        long expiry = System.currentTimeMillis() + 1000 * 60 * 30; // 30 mins

        User users = User.findById(user.getLong("id"));
        users.set("password_reset_token", token);
        users.set("password_reset_token_expiry", expiry);
        users.save();

        // String resetLink = "https://localhost:3000/reset-password?token=" + token;
        // MailUtil.sendResetPasswordLinkEmail(phone_number, resetLink);
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
    
    // public static MapResponse changePassword(String reset_otp, String password){
    //     String sql = "Select * From Users Where Reset_Otp = ?";

    //     MapResponse user = Handler.findFirst(sql, reset_otp);
    //     if (user == null) {
    //         return MapResponse.failure("Reset OTP is invalid");
    //     }
    //     User users = User.findById(user.getLong("id"));
    //     users.set("password", password);
    //     users.set("reset_otp", null);

    //     return users.save() ? MapResponse.success() : MapResponse.failure();
    // }

    public static MapResponse changePassword(String old_password, String new_password){
        
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
