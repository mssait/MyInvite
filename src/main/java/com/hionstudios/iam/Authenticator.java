package com.hionstudios.iam;

import static com.hionstudios.MapResponse.failure;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.hionstudios.MapResponse;

@Component
public class Authenticator {
    @Autowired
    private HionUserDetailsService userDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private AuthenticationManager authenticationManager;

    public ResponseEntity<?> authenticate(
            JwtRequest authRequest,
            HttpServletResponse response) {
        String username = authRequest.getUsername();
        String password = authRequest.getPassword();
        try {
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
            authenticationManager.authenticate(authentication);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(failure(e.getMessage()));
        }
        final HionUserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(MapResponse.failure());
        }
        JwtResponse jwtResponse = construct(userDetails);
        response.addHeader(HttpHeaders.SET_COOKIE, jwtResponse.toCookieHeader());
        MapResponse newResponse = MapResponse.success();
        newResponse.put("name", userDetails.getName());
        newResponse.put("phone", userDetails.getPhone());
        newResponse.put("email", userDetails.getEmail());
        newResponse.put("type", userDetails.getType());
        newResponse.put("roles", userDetails.getRoles());
        return ResponseEntity.ok(newResponse);
    }

    public JwtResponse construct(HionUserDetails userDetails) {
        final String token = jwtTokenUtil.generateToken(userDetails);
        final Date expiry = jwtTokenUtil.getExpirationDateFromToken(token);
        return new JwtResponse(token, expiry);
    }
}
