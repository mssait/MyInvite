package com.hionstudios.iam;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hionstudios.time.TimeUtil;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private HionUserDetailsService jwtUserDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private Authenticator authenticator;

    /**
     * If the JWT is about to expire, refresh it with a new token
     * If the token is valid, manually authenticate the request
     *
     * @param request  HttpServletRequest from Client
     * @param response HttpServletResponse to Client
     * @param chain    FilterChain to process the request further
     * @throws ServletException while processing the request further
     * @throws IOException      while decoding the Auth Value
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        String username = null;
        String jwtToken = null;
        long expiry = TimeUtil.currentTime();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(JwtTokenUtil.TOKEN_NAME)) {
                    jwtToken = cookie.getValue();
                    if (jwtTokenUtil.getExpirationDateFromToken(jwtToken).getTime() > expiry) {
                        username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                    }
                }
            }
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            HionUserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

            // if token is valid configure Spring Security to manually set
            // authentication
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                WebAuthenticationDetails authenticationDetails = new WebAuthenticationDetailsSource()
                        .buildDetails(request);
                authenticationToken.setDetails(authenticationDetails);
                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the
                // Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                if (jwtTokenUtil.checkThreshold(jwtToken)) {
                    JwtResponse jwtResponse = authenticator.construct(userDetails);
                    response.addHeader(HttpHeaders.SET_COOKIE, jwtResponse.toCookieHeader());
                }
            }
        }
        chain.doFilter(request, response);
    }
}