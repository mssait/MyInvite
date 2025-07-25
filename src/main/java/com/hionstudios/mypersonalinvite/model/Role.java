package com.hionstudios.mypersonalinvite.model;

import org.javalite.activejdbc.Model;

public class Role extends Model {
    public static final String ADMIN = "USER_ADMIN";
    public static final String USER = "USER";

    public static long getId(String role) {
        return Role.findFirst("role = ?", role).getLongId();
    }


}
