package com.hionstudios.iam;

import java.io.Serializable;
import java.util.Date;

import com.hionstudios.time.TimeUtil;

public class JwtResponse implements Serializable {
    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwt;
    private final Date expiry;

    public JwtResponse(String jwt, Date expiry) {
        this.jwt = jwt;
        this.expiry = expiry;
    }

    public String getJwt() {
        return jwt;
    }

    public Date getExpiry() {
        return expiry;
    }

    public String toCookieHeader() {
        long expiry = getExpiry().getTime();
        return String.format("%s=%s;Path=/;SameSite=None;Secure=None;max-age=%d",
                JwtTokenUtil.TOKEN_NAME,
                getJwt(),
                ((expiry - TimeUtil.currentTime()) / 1000));

    }
}