package com.hionstudios.app.controller;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hionstudios.iam.Authenticator;
import com.hionstudios.iam.JwtRequest;

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
}
