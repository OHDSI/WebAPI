package com.jnj.honeur.webapi.shiro.filters;

import com.jnj.honeur.webapi.shiro.HoneurTokenManager;
import io.jsonwebtoken.JwtException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.filters.AtlasAuthFilter;
import org.ohdsi.webapi.shiro.tokens.JwtAuthToken;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author gennadiy.anisimov
 */
public final class HoneurJwtAuthFilter extends AtlasAuthFilter {

    @Override
    protected JwtAuthToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        String jwt = HoneurTokenManager.extractToken(request);
        String subject;

        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        String serverName = httpServletRequest.getServerName();
        Optional<Cookie> fingerprintCookie = Arrays.stream(httpServletRequest.getCookies())
                .filter(cookie -> cookie.getName().equals("userFingerprint"))
                .filter(cookie -> cookie.getDomain().contains(serverName))
                .findFirst();

        try {
            if(fingerprintCookie.isPresent()) {
                subject = HoneurTokenManager.getSubject(jwt, fingerprintCookie.get().getValue());
            } else {
                throw new AuthenticationException("No fingerprint cookie!");
            }
        } catch (JwtException e) {
            throw new AuthenticationException(e);
        }

        return new JwtAuthToken(subject);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        boolean loggedIn = false;

        if (isLoginAttempt(request, response)) {
            try {
                loggedIn = executeLogin(request, response);
            } catch (AuthenticationException ae) {
                loggedIn = false;
            }
        }

        if (!loggedIn) {
            HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return loggedIn;
    }

    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        return HoneurTokenManager.extractToken(request) != null;
    }
}
