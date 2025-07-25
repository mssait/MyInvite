package com.hionstudios.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import com.hionstudios.mypersonalinvite.model.Oauth;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

/**
 * @author Karthikeyan K
 */
public class ZohoApp extends OAuthApp {
    public static final String clientId = "1000.ODBVYN1F6SGHSCVSS3M60ZLYCB0EZW";
    public static final String clientSecret = "0ac7a276da46e068060a2d80498ea5a1d1bf3d0e92";
    public static final String callbackURL = "http://localhost:8080/oauth/zoho/callback";
    public static final String tokenURL = "https://accounts.zoho.in/oauth/v2/token";
    public static final String authURL = "https://accounts.zoho.in/oauth/v2/auth";
    public static final String scope = "WorkDrive.workspace.ALL,WorkDrive.files.ALL";
    public static final String state = "serverapp";
    public static final String ZOHO = "zoho";

    public ZohoApp() {
        super(tokenURL, authURL, callbackURL, scope, clientId, clientSecret, state);
    }

    @Override
    public void persistToken(OAuthTokens tokens) {
        Oauth oauth = AppOauthUtil.getOAuth(ZOHO);
        if (oauth == null) {
            oauth = new Oauth();
        }
        oauth.set(Oauth.ACCESS_TOKEN, tokens.getAccessToken().toString());
        RefreshToken refreshToken = tokens.getRefreshToken();
        if (refreshToken != null) {
            oauth.set(Oauth.REFRESH_TOKEN, refreshToken.toString());
        }
        oauth.set(Oauth.PROVIDER, ZOHO)
                .set(Oauth.EXPIRY, tokens.getAccessTokenExpiry())
                .saveIt();
    }

    @Override
    public OAuthTokens getOAuthTokensFromDB() {
        Oauth oauth = AppOauthUtil.getOAuth("zoho");
        return oauth == null ? null
                : new OAuthTokens(
                        new ZohoOauthToken(oauth.getString(Oauth.ACCESS_TOKEN)),
                        new RefreshToken(oauth.getString(Oauth.REFRESH_TOKEN)),
                        oauth.getLong(Oauth.EXPIRY));
    }

    @Override
    public String getUrl() {
        HashMap<String, String[]> params = new HashMap<>(2);
        params.put("prompt", new String[] { "consent" });
        params.put("access_type", new String[] { "offline" });
        try {
            return URLDecoder.decode(new ZohoApp().getCodeRequestURI(params).toString(), UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
