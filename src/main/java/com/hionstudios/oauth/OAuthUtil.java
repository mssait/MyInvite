package com.hionstudios.oauth;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Karthikeyan K
 * <p>
 * Handle all the OAuth operations here.
 */

public class OAuthUtil {
    private static OAuthUtil oAuthUtil;

    /**
     * Marking the constructor private to make it Singleton class.
     */
    private OAuthUtil() {
    }

    /**
     * Instance method
     *
     * @return Instance of OAuthUtil
     */
    static OAuthUtil getUtil() {
        if (oAuthUtil == null) {
            oAuthUtil = new OAuthUtil();
        }
        return oAuthUtil;
    }

    /**
     * Get response URI redirected by the OAuth provider.
     * This URI contains the Access Code.
     *
     * @param authURI     Auth URI
     * @param callbackURI Callback URI
     * @param scope       Scope
     * @param state       State
     * @param clientID    Client ID
     * @return Auth Code URI
     */
    URI getCodeRequestURI(URI authURI, URI callbackURI, Scope scope, State state, ClientID clientID,
                          Map<String, String[]> params) {
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(new ResponseType(ResponseType.Value.CODE), clientID)
                        .scope(scope).state(state).redirectionURI(callbackURI).endpointURI(authURI);
        if (params != null) {
            params.forEach(builder::customParameter);
        }
        return builder.build().toURI();
    }

    /**
     * Get OAuthTokens for the provided OAuthApp Instance
     * If there are no persisted tokens, then null is returned
     * If the persisted tokens are expired new tokens are generated with the refresh tokens
     *
     * @param app OAuthApp instance
     * @return OAuthTokens
     */
    OAuthTokens getOAuthTokens(OAuthApp app) throws Exception {
        OAuthTokens oAuthTokens = app.getOAuthTokensFromDB();
        if (oAuthTokens != null && !isAccessTokenValid(oAuthTokens)) {
            oAuthTokens = getOAuthTokens(app, oAuthTokens);
            app.persistToken(oAuthTokens);
        }
        return oAuthTokens;
    }

    /**
     * Generate New OAuthTokens with the Refresh token
     *
     * @param app    OAuthApp instance
     * @param tokens Expired OAuthTokens
     * @return New OAuth Tokens
     */
    OAuthTokens getOAuthTokens(OAuthApp app, OAuthTokens tokens) throws Exception {
        return getOAuthTokens(tokens.getRefreshToken(), app.getClientID(), app.getClientSecret(), app.getTokenURI());
    }

    /**
     * Method to get the OAuth Tokens with
     *
     * @param redirectUri  Callback URI
     * @param code         Access Code
     * @param tokenURI     Token URI
     * @param clientID     Client ID
     * @param clientSecret Client Secret
     * @return OAuthTokens
     */
    OAuthTokens getOAuthTokens(URI redirectUri, AuthorizationCode code, URI tokenURI, ClientID clientID,
                                                      Secret clientSecret) throws Exception {
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, redirectUri);
        ClientAuthentication clientSecretBasic = new ClientSecretBasic(clientID, clientSecret);
        TokenRequest request = new TokenRequest(tokenURI, clientSecretBasic, codeGrant);
        HTTPResponse resp = request.toHTTPRequest().send();
        TokenResponse response = TokenResponse.parse(resp);
        if (response.indicatesSuccess()) {
            AccessTokenResponse successResponse = response.toSuccessResponse();
            return new OAuthTokens(successResponse.getTokens());
        } else {
            TokenErrorResponse errorResponse = response.toErrorResponse();
            throw new Exception(errorResponse.getErrorObject().toJSONObject().toJSONString());
        }
    }

    /**
     * Method to get OAuth Tokens with Refresh Tokens
     *
     * @param refreshToken Refresh Token
     * @param clientID     Client ID
     * @param clientSecret Client Secret
     * @param tokenURI     Token URL by provider
     * @return OAuthToken
     */
    OAuthTokens getOAuthTokens(RefreshToken refreshToken, ClientID clientID, Secret clientSecret, URI tokenURI)
            throws Exception {
        AuthorizationGrant refreshTokenGrant = new RefreshTokenGrant(refreshToken);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        TokenRequest request = new TokenRequest(tokenURI, clientAuth, refreshTokenGrant);
        TokenResponse response = TokenResponse.parse(request.toHTTPRequest().send());
        if (!response.indicatesSuccess()) {
            TokenErrorResponse errorResponse = response.toErrorResponse();
            throw new Exception(errorResponse.getErrorObject().toJSONObject().toJSONString());
        }
        AccessTokenResponse successResponse = response.toSuccessResponse();
        return new OAuthTokens(successResponse.getTokens());
    }

    /**
     * Method to parse the Auth Code from the Auth Code URI
     *
     * @param codeURI Auth Code URI
     * @return AuthorizationCode
     */
    AuthorizationCode getAuthorizationCode(URI codeURI) throws Exception {
        AuthorizationResponse response = AuthorizationResponse.parse(codeURI);
        return getAuthorizationCode(response);
    }

    AuthorizationCode getAuthorizationCode(AuthorizationResponse response) throws Exception {
        if (!response.indicatesSuccess()) {
            throw new Exception(response.toErrorResponse().toString());
        }
        AuthorizationSuccessResponse successResponse = (AuthorizationSuccessResponse) response;
        return successResponse.getAuthorizationCode();
    }

    AuthorizationCode getAuthorizationCode(HttpServletRequest request) throws Exception {
        Map<String, List<String>> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Arrays.asList(e.getValue())));
        AuthorizationResponse response = AuthorizationResponse.parse(URI.create(request.getRequestURI()), params);
        return getAuthorizationCode(response);
    }

    /**
     * Check if the Access Token is valid or not
     *
     * @param tokens OAuthTokens
     * @return true if token is not expired.
     */
    boolean isAccessTokenValid(OAuthTokens tokens)  {
        return tokens != null && tokens.getAccessToken() != null && System.currentTimeMillis() < tokens.getAccessTokenExpiry();
    }
}
