package com.jnj.honeur.webapi.shiro.filters;

import org.apache.shiro.web.servlet.AdviceFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.jnj.honeur.webapi.shiro.management.AtlasCustomSecurity.FINGERPRINT_ATTRIBUTE;
import static org.ohdsi.webapi.shiro.management.AtlasSecurity.TOKEN_ATTRIBUTE;

public class HoneurSendTokenInRedirectFilter extends AdviceFilter {
    private String redirectUrl;

    public HoneurSendTokenInRedirectFilter(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) {
        String jwt = (String)request.getAttribute(TOKEN_ATTRIBUTE);
        String fingerprint = (String)request.getAttribute(FINGERPRINT_ATTRIBUTE);
        try {
            ((HttpServletResponse) response).setHeader("Set-Cookie", fingerprint);
            ((HttpServletResponse) response).sendRedirect(redirectUrl + jwt);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
