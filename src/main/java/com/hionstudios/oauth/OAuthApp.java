package com.hionstudios.oauth;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.hionstudios.MapResponse;
import com.hionstudios.http.HttpUtil;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;

/**
 * Abstract OAuthApp Class
 *
 * @author Karthikeyan K
 */
public abstract class OAuthApp {
    private URI tokenURI;
    private URI authURI;
    private URI callbackURI;
    private final Scope scope;
    private final ClientID clientID;
    private final Secret clientSecret;
    private final State state;
    protected static final String UTF_8 = "UTF-8";

    /**
     * Initialize a OAuth 2.0 App with the details
     *
     * @param tokenURL     Token URL
     * @param authURL      Authentication URL
     * @param callbackURL  Callback URL
     * @param scope        Scope
     * @param clientId     Client ID
     * @param clientSecret Client Secret
     * @param state        State
     */
    public OAuthApp(String tokenURL, String authURL, String callbackURL, String scope, String clientId,
            String clientSecret, String state) {
        try {
            this.tokenURI = new URI(tokenURL);
            this.authURI = new URI(authURL);
            this.callbackURI = new URI(callbackURL);
        } catch (Exception ignored) {
        }
        this.scope = Scope.parse(scope);
        this.clientID = new ClientID(clientId);
        this.clientSecret = new Secret(clientSecret);
        this.state = new State(state);
    }

    /**
     * Method to be override to persist the tokens in the database.
     *
     * @param tokens OAuthTokens
     */
    abstract public void persistToken(OAuthTokens tokens);

    /**
     * Method to get the persisted tokens
     *
     * @return OAuth Tokens
     */
    abstract public OAuthTokens getOAuthTokensFromDB();

    /**
     * Method to generate the code request URL
     *
     * @return Code Request URL
     */
    abstract public String getUrl();

    /**
     * Method to get the URL redirected by the OAuth provider.
     * This contains the Code and the State
     *
     * @param params the list of parameters
     * @return AuthCode URI
     */

    public URI getCodeRequestURI(Map<String, String[]> params) {
        return OAuthUtil.getUtil().getCodeRequestURI(authURI, callbackURI, scope, state, clientID, params);
    }

    /**
     * Parse Auth Code from Code URI
     *
     * @param codeURI Code URI
     * @return Auth Code
     */
    public AuthorizationCode getAuthorizationCode(URI codeURI) throws Exception {
        return OAuthUtil.getUtil().getAuthorizationCode(codeURI);
    }

    /**
     * Get the Auth Code from the HttpRequest
     *
     * @param request HttpServletRequest
     * @return AuthCode
     */
    public AuthorizationCode getAuthorizationCode(HttpServletRequest request) throws Exception {
        return OAuthUtil.getUtil().getAuthorizationCode(request);
    }

    public OAuthTokens getOAuthTokens() throws Exception {
        return OAuthUtil.getUtil().getOAuthTokens(this);
    }

    /**
     * Get Auth Tokens from the Auth Code.
     * A new API call is made to gt the Auth tokens.
     * The new tokens are persisted in the database also.
     *
     * @param code Auth Code
     * @return OAuth Code
     */
    public OAuthTokens getOAuthTokens(AuthorizationCode code) throws Exception {
        OAuthTokens tokens = OAuthUtil.getUtil().getOAuthTokens(callbackURI, code, tokenURI, clientID, clientSecret);
        persistToken(tokens);
        return tokens;
    }

    /**
     * Overloaded method to get the OAuth Code from the Code URI
     *
     * @param codeURI Code URI
     * @return OAuthTokens
     */
    public OAuthTokens getOAuthTokens(URI codeURI) throws Exception {
        return getOAuthTokens(getAuthorizationCode(codeURI));
    }

    /**
     * Overloaded method to get the OAuth Code from the Code URI
     *
     * @param codeURI codeURI Code URI
     * @return OAuthTokens
     */
    public OAuthTokens getOAuthTokens(String codeURI) throws Exception {
        return getOAuthTokens(new URI(codeURI));
    }

    /**
     * Overloaded method to get the OAuth Code from the HttpServletRequest
     *
     * @param request HttpServletRequest
     * @return OAuthTokens
     */
    public OAuthTokens getOAuthTokens(HttpServletRequest request) throws Exception {
        return getOAuthTokens(getAuthorizationCode(request));
    }

    public MapResponse getResources(String url) {
        return getResources(url, "GET");
    }

    public MapResponse getResources(String url, String method) {
        return getResources(url, method, null, null);
    }

    public MapResponse getResources(String url, String methodType, HashMap<String, String> params) {
        return getResources(url, methodType, params, null);
    }

    public MapResponse getResources(String url, String methodType, JSONObject body) {
        return getResources(url, methodType, null, body);

    }

    public MapResponse getResources(String url, String methodType, HashMap<String, String> params, JSONObject body) {
        return getResources(url, methodType, params, body, null);
    }

    public MapResponse getResources(String url, String methodType, HashMap<String, String> params,
            JSONObject body, HttpUtil.FileParam file) {
        List<HttpUtil.FileParam> fileParams = new ArrayList<>(1);
        fileParams.add(file);
        try {
            return getResources(getOAuthTokens(), url, methodType, params, body, fileParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return MapResponse.failure();
    }

    /**
     * This method is to make an API call.
     *
     * @param tokens     OAuthToken
     * @param url        URL Endpoint
     * @param methodType Method type {GET, POST, etc}
     * @param params     Parameters to be specified in the API call
     * @return Response data of the API call
     */
    public MapResponse getResources(OAuthTokens tokens, String url, String methodType, HashMap<String, String> params,
            JSONObject body, List<HttpUtil.FileParam> files) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", tokens.getAccessToken().toAuthorizationHeader());
        headers.put("Accept", "application/vnd.api+json");
        headers.put("Content-Type", "application/json");
        try {
            String response = HttpUtil.fetch(url, methodType, params, body, headers, files);
            if (response != null) {
                return new MapResponse(new JSONObject(response));
            } else {
                return MapResponse.failure();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return MapResponse.failure(e.getMessage());
        }
    }

    // Getters START
    public URI getTokenURI() {
        return tokenURI;
    }

    public URI getAuthURI() {
        return authURI;
    }

    public URI getCallbackURI() {
        return callbackURI;
    }

    public Scope getScope() {
        return scope;
    }

    public ClientID getClientID() {
        return clientID;
    }

    public Secret getClientSecret() {
        return clientSecret;
    }

    public State getState() {
        return state;
    }
    // Getters END
}
