package com.hionstudios.mypersonalinvite.model;

import org.javalite.activejdbc.Model;

public class NotificationType extends Model {
    public static final String RSVP = "Rsvp";
    public static final String CARPOOL = "Carpool";
    public static final String EVENT = "Event";
    public static final String ADMIN = "Admin";

    public static int getId(String type) {
        return NotificationType.findFirst("type = ?", type)
                .getInteger("id");
    }
}
