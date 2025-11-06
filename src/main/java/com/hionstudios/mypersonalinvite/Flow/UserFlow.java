package com.hionstudios.mypersonalinvite.Flow;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.hionstudios.MapResponse;
import com.hionstudios.WhatsAppUtil;
import com.hionstudios.db.Handler;
import com.hionstudios.iam.UserUtil;
import com.hionstudios.mypersonalinvite.model.Role;
import com.hionstudios.mypersonalinvite.model.User;
import com.hionstudios.mypersonalinvite.model.UserRole;
import com.hionstudios.mypersonalinvite.model.UserType;
import com.hionstudios.oauth.WorkDrive;
import com.hionstudios.oauth.WorkDrive.Folder;
import com.hionstudios.time.TimeUtil;

public class UserFlow {
    public MapResponse getUsers() {
        String sql = "Select Id, Name, Email, Phone_Number, Profile_pic as Avatar From Users";
        return Handler.toDataGrid(sql);
    }

    public MapResponse getUserDetails(Long id) {
        String sql = "Select * From Users Where id = ?";
        List<MapResponse> userDetails = Handler.findAll(sql, id);
        MapResponse response = new MapResponse().put("userDetails", userDetails);
        return response;
    }

    public MapResponse editProfile(String name, String phone_number, String password, String email,
            MultipartFile profile_pic) {

        long userId = UserUtil.getUserid();

        User user = User.findById(userId);
        if (user == null) {
            return MapResponse.failure("User not found");
        }

        String avatar = null;

        if (profile_pic != null) {
            MapResponse response = WorkDrive.upload((MultipartFile) profile_pic, Folder.MYPERSONALINVITE, false);
            avatar = response != null ? response.getString("resource_id") : null;
        }

        user.set("name", name);
        user.set("email", email);
        user.set("phone_number", phone_number);
        user.set("password", password);
        if (avatar != null) {
            user.set("profile_pic", avatar);
        }

        return user.save() ? MapResponse.success() : MapResponse.failure();
    }

    public MapResponse viewProfile() {
        long userId = UserUtil.getUserid();

        String sql = "Select Id, Name, Email, Phone_Number, Profile_pic as Avatar From Users Where id = ?";

        MapResponse profile = Handler.findFirst(sql, userId);
        MapResponse response = new MapResponse().put("profile", profile);
        return response;
    }

    public MapResponse addUser(String name, String phone_number, String password) {

        int otp = (int) (Math.random() * 900000) + 100000;
        long expiry = System.currentTimeMillis() + (30 * 60 * 1000); // 30 mins

        User existing = User.findFirst("phone_number = ? And phone_verified = ?", phone_number, true);
        if (existing != null) {
            return MapResponse.failure("Phone number already registered");
        }

        User verified = User.findFirst("phone_number = ? And phone_verified = ?", phone_number, false);
        if (verified != null) {

            User user = User.findFirst("phone_number = ?", phone_number);
            user.set("name", name);
            user.set("password", password);
            user.set("otp_code", otp);
            user.set("otp_expiry", expiry);
            user.saveIt();

            return MapResponse.success();
        }

        User user = new User();
        user.set("name", name);
        user.set("phone_number", phone_number);
        user.set("password", password);
        user.set("type_id", UserType.getId(UserType.USER));
        user.set("otp_code", otp);
        user.set("otp_expiry", expiry);
        user.insert();

        UserRole role = new UserRole();
        role.set("user_id", user.getLongId());
        role.set("role_id", Role.getId(Role.USER));

        String eventLink = "https://mypersonalinvite.com";
        String msg = "Hi " + name + "! 🎉 Welcome to MY Invite!\n\n" +
                " Registration Successful!\n ✅" +
                "You can now create and manage your events "
                + eventLink;
        WhatsAppUtil.sendWhatsAppMessage(phone_number, msg);

        // sendOtpMessage(phone_number, otp);

        return role.insert() ? MapResponse.success() : MapResponse.failure();
    }

    public MapResponse verifyPhone(String phone_number, String otp) {
        User user = User.findFirst("phone_number = ?", phone_number);
        if (user == null) {
            return MapResponse.failure("User not found");
        }

        Long expiry = user.getLong("otp_expiry");
        if (expiry == null || System.currentTimeMillis() > expiry) {
            return MapResponse.failure("OTP expired");
        }

        if (!String.valueOf(otp).equals(user.getString("otp_code"))) {
            return MapResponse.failure("Invalid OTP");
        }

        user.set("phone_verified", true);
        user.set("otp_code", null);
        user.set("otp_expiry", null);
        user.saveIt();

        return MapResponse.success("Phone verified successfully");
    }

    public MapResponse editUsers(Long id, boolean is_active) {
        User user = User.findById(id);
        if (user == null) {
            return MapResponse.failure("User not found");
        }
        user.set("is_active", is_active);

        return user.save() ? MapResponse.success() : MapResponse.failure();
    }

    public MapResponse dashboard() {
        String sql = "Select (Select Count(*) From Users) As Total_Users, (Select Count(*) From Events) As Total_Events, (Select Count(*) From Events Where TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM') = TO_CHAR(NOW(), 'YYYY-MM')) As Events_Created_This_Month, (Select Count(*) From Events Where Date > ?) As Upcoming_Events, (Select Count(*) From Events Where Date < ?) As Completed_Events, (Select Count(*) From Users Where TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM') = TO_CHAR(NOW(), 'YYYY-MM')) As Users_Joined_This_Month";

        long currentTime = TimeUtil.currentTime();

        MapResponse dashboard = Handler.findFirst(sql, currentTime, currentTime);
        MapResponse response = new MapResponse().put("dashboard", dashboard);
        return response;
    }
}
