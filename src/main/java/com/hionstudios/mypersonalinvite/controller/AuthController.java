package com.hionstudios.mypersonalinvite.controller;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hionstudios.MapResponse;
import com.hionstudios.db.DbTransaction;
import com.hionstudios.iam.Authenticator;
import com.hionstudios.iam.JwtRequest;
import com.hionstudios.iam.UserUtil;

@RestController
public class AuthController {
    @Autowired
    Authenticator authenticator;

    @PostMapping("/authenticate")
    @PermitAll
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody JwtRequest authenticationRequest,
            HttpServletResponse response) {
        return authenticator.authenticate(authenticationRequest, response);
    }

    @PostMapping("forgot-password")
    @PermitAll
    public ResponseEntity<MapResponse> forgotPassword(@RequestParam String username) {
        return ((DbTransaction) () -> UserUtil.forgotPassword(username)).read();
    }

    @PostMapping("reset-password")
    @PermitAll
    public ResponseEntity<MapResponse> resetPassword(@RequestParam String token, @RequestParam String password) {
        return ((DbTransaction) () -> UserUtil.resetPassword(token, password)).read();
    }
}
