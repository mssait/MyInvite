package com.hionstudios.security;

import org.springframework.security.crypto.password.PasswordEncoder;

public final class NoPasswordEncoder implements PasswordEncoder {
    private static final NoPasswordEncoder INSTANCE = new NoPasswordEncoder();

    private NoPasswordEncoder() {
    }

    public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return rawPassword.toString().equals(encodedPassword);
    }

    public static NoPasswordEncoder getInstance() {
        return INSTANCE;
    }
}
