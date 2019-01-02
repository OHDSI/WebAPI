package com.jnj.honeur.webapi.shiro.filters;

import com.jnj.honeur.webapi.shiro.HoneurTokenManager;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;

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
public class HoneurLogoutFilter extends AdviceFilter {

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String jwt = HoneurTokenManager.extractToken(request);

        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        Optional<Cookie> fingerprintCookie = Arrays.stream(httpServletRequest.getCookies())
                .filter(cookie -> cookie.getName().equals("userFingerprint")).findFirst();

        if (fingerprintCookie.isPresent() && HoneurTokenManager.invalidate(jwt, fingerprintCookie.get().getValue())) {
            httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

        return false;
    }
}
