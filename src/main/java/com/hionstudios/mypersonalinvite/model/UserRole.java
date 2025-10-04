package com.hionstudios.mypersonalinvite.model;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.CompositePK;
@CompositePK({ "role_id", "user_id" })
public class UserRole extends Model {
    
}
