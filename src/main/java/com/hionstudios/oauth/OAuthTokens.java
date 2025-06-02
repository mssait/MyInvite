package com.hionstudios.oauth;

import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;

import java.util.concurrent.TimeUnit;

/**
 * @author Karthikeyan K
 */
public class OAuthTokens extends Tokens {

    /**
     * A threshold of 10 minutes is provided to expire the token for a safety.
     */
    private static final int SAFETY_FACTOR = 600; //seconds
    private final long accessTokenExpiry;

    /**
     * The constructor that will be used to store the data from DB.
     */
    public OAuthTokens(AccessToken accessToken, RefreshToken refreshToken, Long accessTokenExpiry) {
        super(accessToken, refreshToken);
        this.accessTokenExpiry = accessTokenExpiry;
    }

    /**
     * The constructor that will be used when the token is fetched.
     * The accessToken is set to expire before 10 minutes of it's actual expiry for a safety.
     */
    public OAuthTokens(AccessToken accessToken, RefreshToken refreshToken) {
        super(accessToken, refreshToken);
        accessTokenExpiry = System.currentTimeMillis() +
                TimeUnit.SECONDS.toMillis(accessToken.getLifetime() - SAFETY_FACTOR);
    }

    /**
     * The constructor that will be used when the token is fetched.
     */
    public OAuthTokens(Tokens tokens) {
        this(tokens.getAccessToken(), tokens.getRefreshToken());
    }

    /**
     * Getter method for accessTokenExpiry.
     */
    public long getAccessTokenExpiry() {
        return accessTokenExpiry;
    }
}
