package com.hionstudios.mypersonalinvite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hionstudios.MapResponse;
import com.hionstudios.MixMultipartFileAndString;
import com.hionstudios.db.DbTransaction;
import com.hionstudios.iam.IsAdmin;
import com.hionstudios.mypersonalinvite.Flow.UserFlow;

@RestController
@RequestMapping("api/user")
public class UserController {
    @GetMapping()
    @IsAdmin
    public ResponseEntity<MapResponse> users() {
        return ((DbTransaction) () -> new UserFlow().getUsers()).read();
    }

    @PostMapping("registration")
    public ResponseEntity<MapResponse> registration(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone_number) {
        return ((DbTransaction) () -> new UserFlow().addUser(name, email, phone_number)).write();
    }

    @PostMapping()
    @IsAdmin
    public ResponseEntity<MapResponse> editUsers(
            @PathVariable int id,
            String name, String email,
            String phone_number,
            @MixMultipartFileAndString Object profile_pic,
            boolean is_active) {
        return ((DbTransaction) () -> new UserFlow().editUsers(id, name, email, phone_number, profile_pic, is_active))
                .write();
    }

}
