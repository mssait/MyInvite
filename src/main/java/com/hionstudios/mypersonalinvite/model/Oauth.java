package com.hionstudios.mypersonalinvite.model;

import org.javalite.activejdbc.Model;

public class Oauth extends Model {
    public static final String PROVIDER = "provider";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String EXPIRY = "expiry";
}
