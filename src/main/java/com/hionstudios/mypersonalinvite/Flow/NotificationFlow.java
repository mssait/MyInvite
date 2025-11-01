package com.hionstudios.mypersonalinvite.Flow;

import com.hionstudios.MapResponse;
import com.hionstudios.db.Handler;
import com.hionstudios.iam.UserUtil;
// import com.hionstudios.iam.UserUtil;
import com.hionstudios.mypersonalinvite.model.FcmDeviceToken;

public class NotificationFlow {

    public MapResponse addFcmToken(long id, String fcm_token) {

        // long userId = UserUtil.getUserid();

        FcmDeviceToken fcm = new FcmDeviceToken();

        fcm.set("fcm_token", fcm_token);

        return fcm.insert() ? MapResponse.success() : MapResponse.failure();
    }

    public MapResponse getFcmToken() {

        long id = UserUtil.getUserid();

        String sql = "Select Fcm_Token from Fcm_Tokens Where UserId = ";
        MapResponse fcmToken = Handler.findFirst(sql, id);
        
        return fcmToken;
    }
}
