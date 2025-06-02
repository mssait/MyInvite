package com.hionstudios.oauth;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.hionstudios.MapResponse;
import com.hionstudios.db.DbTransaction;

@RestController
@RequestMapping("oauth")
public class OAuthController {
    @GetMapping("{provider}/auth")
    public ModelAndView authOAuth2(@PathVariable String provider) throws Exception {
        return new ModelAndView("redirect:" + AppOauthUtil.getURL(provider));
    }

    @GetMapping("{provider}/callback")
    public ResponseEntity<MapResponse> callback(@PathVariable String provider, HttpServletRequest request) {
        return ((DbTransaction) () -> AppOauthUtil.getTokens(provider, request)).write();
    }
}
