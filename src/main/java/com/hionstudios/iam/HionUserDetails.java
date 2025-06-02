package com.hionstudios.iam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.hionstudios.ListResponse;

public class HionUserDetails implements UserDetails {
    public static final long serialVersionUID = 1;
    private String password;
    private long userid;
    private String name;
    private String phone;
    private String email;
    private String username;
    private String type;
    private String avatar;

    private ListResponse roles = new ListResponse();
    private final Set<SimpleGrantedAuthority> authorities = new HashSet<>();

    public HionUserDetails() {
        this.userid = 1;
    }

    /**
     * Username is a mandatory field
     * For the Org Users Email is the Username
     * For the Customers and the Distributors, the Code is the username
     *
     * @param userDetails Map containing the user details
     * @param roles       Role Set of the user
     */
    public HionUserDetails(HashMap<String, Object> userDetails) {
        this.email = this.username = (String) userDetails.get("email");
        this.password = (String) userDetails.get("password");
        this.userid = (long) userDetails.get("id");
        this.name = (String) userDetails.get("name");
        this.type = (String) userDetails.get("type");
        Object roleObj = userDetails.get("roles");
        if (roleObj instanceof ArrayList<?>) {
            ((ArrayList<?>) roleObj).forEach(role -> {
                this.roles.add(role);
                this.authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            });
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public long getUserid() {
        return userid;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getType() {
        return type;
    }

    public ListResponse getRoles() {
        return roles;
    }

    public String getAvatar() {
        return avatar;
    }
}
