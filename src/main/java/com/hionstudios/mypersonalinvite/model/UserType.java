package com.hionstudios.mypersonalinvite.model;

import org.javalite.activejdbc.Model;

public class UserType extends Model {
    public static final String ADMIN = "Admin";
    public static final String USER = "User";


    public static long getId(String type) {
        return UserType.findFirst("type = ?", type).getLongId();
    }
}
