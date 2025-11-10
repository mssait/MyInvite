package com.hionstudios.mypersonalinvite.controller;

import javax.websocket.server.PathParam;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hionstudios.MapResponse;
import com.hionstudios.db.DbTransaction;
import com.hionstudios.iam.IsAdmin;
import com.hionstudios.iam.IsUser;
import com.hionstudios.mypersonalinvite.Flow.UserFlow;

@RestController
@RequestMapping("api/user")
public class UserController {
    @GetMapping("view")
    @IsAdmin
    public ResponseEntity<MapResponse> users() {
        return ((DbTransaction) () -> new UserFlow().getUsers()).read();
    }

    @PutMapping("updateProfile")
    public ResponseEntity<MapResponse> editProfile(
            String name,
            String phone_number,
            String password,
            String email,
            @RequestParam(required = false) MultipartFile profile_pic) {
        return ((DbTransaction) () -> new UserFlow().editProfile(name, phone_number, password, email, profile_pic))
                .write();
    }

    @GetMapping("view-profile")
    @IsUser
    public ResponseEntity<MapResponse> viewProfile() {
        return ((DbTransaction) () -> new UserFlow().viewProfile()).read();
    }

    @GetMapping("{id}/view-details")
    @IsAdmin
    public ResponseEntity<MapResponse> userDetails(@PathVariable Long id) {
        return ((DbTransaction) () -> new UserFlow().getUserDetails(id)).read();
    }

    @PostMapping("registration")
    public ResponseEntity<MapResponse> registration(
            @RequestParam String name,
            @RequestParam String phone_number,
            @RequestParam String password) {
        return ((DbTransaction) () -> new UserFlow().addUser(name, phone_number, password)).write();
    }

    @PutMapping("verify-phone")
    public ResponseEntity<MapResponse> verifyPhone(
            @RequestParam String phone_number,
            @RequestParam String otp) {
        return ((DbTransaction) () -> new UserFlow().verifyPhone(phone_number, otp)).write();
    }

    @PostMapping("{id}/change-status")
    @IsAdmin
    public ResponseEntity<MapResponse> editUsers(
            @PathVariable Long id,
            @RequestParam(required = false) boolean is_active) {
        return ((DbTransaction) () -> new UserFlow().editUsers(id, is_active))
                .write();
    }

    @GetMapping("dashboard")
    @IsAdmin
    public ResponseEntity<MapResponse> dashboard() {
        return ((DbTransaction) () -> new UserFlow().dashboard()).read();
    }

    @GetMapping("calendar-connection")
    public ResponseEntity<MapResponse> calendarConnection() {
        return ((DbTransaction) () -> new UserFlow().getCalendarConnection()).read();
    }
}
