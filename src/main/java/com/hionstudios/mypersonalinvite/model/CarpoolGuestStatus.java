package com.hionstudios.mypersonalinvite.model;

import org.javalite.activejdbc.Model;

public class CarpoolGuestStatus extends Model {
    public static final String PENDING = "Pending";
    public static final String ACCEPTED = "Accepted";
    public static final String REJECTED = "Rejected";
    public static final String CANCELLED = "Cancelled";

    public static Long getId(String status) {
        return CarpoolGuestStatus.findFirst("status = ?", status).getLongId();
    }

}
