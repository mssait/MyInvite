package com.hionstudios.iam;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hionstudios.MapResponse;
import com.hionstudios.db.DbUtil;
import com.hionstudios.db.Handler;

@Service
public class HionUserDetailsService implements UserDetailsService {
    @Override
    public HionUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            DbUtil.open();
            String sql = "Select Users.Id, Users.Name, Users.Email, Users.Password, User_Types.Type, Array(Select Role From Roles Join User_Roles On User_Roles.Role_Id = Roles.Id And User_Roles.User_Id = Users.Id) Roles From Users Join User_Types On User_Types.Id = Users.Type_Id Where Email = ? Or Phone_Number = ?";
            return getUserDetails(sql, username);
        } finally {
            DbUtil.close();
        }
    }

    private static HionUserDetails getUserDetails(String sql, Object... params) {
        MapResponse user = Handler.findFirst(sql, params);
        if (user == null) {
            throw new UsernameNotFoundException("ERROR 404!");
        }
        return new HionUserDetails(user);
    }
}
