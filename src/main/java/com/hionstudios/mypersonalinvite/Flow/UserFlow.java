package com.hionstudios.mypersonalinvite.Flow;


import java.util.List;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.mypersonalinvite.model.Role;
import com.hionstudios.mypersonalinvite.model.User;
import com.hionstudios.mypersonalinvite.model.UserRole;


public class UserFlow {
    public MapResponse getUsers() {
        String sql = "Select * From Users";
        return Handler.toDataGrid(sql);
    }

    public MapResponse getUserDetails(Long id) {
        String sql = "Select * From Users Where id = ?";
        List<MapResponse> userDetails = Handler.findAll(sql, id);
        MapResponse response = new MapResponse().put("userDetails", userDetails);
        return response;
    }

    public MapResponse addUser(String name, String password, String phone_number) {

        User user = new User();
        user.set("name", name);
        user.set("password", password);
        user.set("phone_number", phone_number);
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
}
