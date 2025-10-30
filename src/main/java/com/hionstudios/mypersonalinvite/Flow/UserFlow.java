package com.hionstudios.mypersonalinvite.Flow;

import java.util.List;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.iam.UserUtil;
import com.hionstudios.mypersonalinvite.model.Role;
import com.hionstudios.mypersonalinvite.model.User;
import com.hionstudios.mypersonalinvite.model.UserRole;
import com.hionstudios.mypersonalinvite.model.UserType;
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

    public MapResponse editProfile(String name, String phone_number, String password, String email) {

        long userId = UserUtil.getUserid();

        User user = User.findById(userId);
        if (user == null) {
            return MapResponse.failure("User not found");
        }
        user.set("name", name);
        user.set("email", email);
        user.set("phone_number", phone_number);
        user.set("password", password);

        return user.save() ? MapResponse.success() : MapResponse.failure();
    }

    public MapResponse addUser(String name, String phone_number, String password) {

        User user = new User();
        user.set("name", name);
        user.set("phone_number", phone_number);
        user.set("password", password);
        user.set("type_id", UserType.getId(UserType.USER));
        user.insert();

        UserRole role = new UserRole();
        role.set("user_id", user.getLongId());
        role.set("role_id", Role.getId(Role.USER));

        return role.insert() ? MapResponse.success() : MapResponse.failure();
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
