package com.hionstudios.mypersonalinvite.Flow;

import org.springframework.web.multipart.MultipartFile;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.mypersonalinvite.model.Role;
import com.hionstudios.mypersonalinvite.model.User;
import com.hionstudios.mypersonalinvite.model.UserRole;
import com.hionstudios.oauth.WorkDrive;
import com.hionstudios.oauth.WorkDrive.Folder;

public class UserFlow {
    public MapResponse getUsers() {
        String sql = "Select * From Users";
        return Handler.toDataGrid(sql);
    }

    public MapResponse addUser(String name, String email, String phone_number) {

        User user = new User();
        user.set("name", name);
        user.set("email", email);
        user.set("phone_number", phone_number);
        user.insert();

        UserRole role = new UserRole();
        role.set("user_id", user.getLongId());
        role.set("role_id", Role.getId(Role.USER));

        return role.insert() ? MapResponse.success() : MapResponse.failure();
    }

    public MapResponse editUsers(int id, String name, String email, String phone_number, Object profile_pic,
            boolean is_active) {
        User user = User.findById(id);
        if (name != null && !name.isEmpty()) {
            user.set("name", name);
        }
        if (email != null && !email.isEmpty()) {
            user.set("email", email);
        }
        if (phone_number != null && !phone_number.isEmpty()) {
            user.set("phone_number", phone_number);
        }
        user.set("is_active", is_active);

        if (profile_pic != null) {
            if (profile_pic instanceof String) {
                user.set("profile_pic", profile_pic);
            } else if (profile_pic instanceof MultipartFile) {
                MultipartFile uploadedImage = (MultipartFile) profile_pic;

                String existingImage = user.getString("profile_pic");
                if (existingImage != null && existingImage.matches("^[a-zA-Z0-9]{20}$")) {
                    WorkDrive.delete(existingImage);
                }
                MapResponse response = WorkDrive.upload(uploadedImage, Folder.MYPERSONALINVITE, false);
                String image = response != null ? response.getString("resource_id") : null;
                user.set("profile_pic", image);

            }
        }
        return user.save() ? MapResponse.success() : MapResponse.failure();
    }
}
