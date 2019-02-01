package com.jnj.honeur.webapi.shiro.filters;

import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.helper.Guard;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import static com.jnj.honeur.webapi.shiro.management.AtlasCustomSecurity.FINGERPRINT_ATTRIBUTE;
import static org.ohdsi.webapi.shiro.management.AtlasSecurity.TOKEN_ATTRIBUTE;

public class HoneurSendTokenInUrlFilter extends AdviceFilter {

    private String url;

    public HoneurSendTokenInUrlFilter(String url) {
        this.url = url;
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        String jwt = (String)request.getAttribute(TOKEN_ATTRIBUTE);

        String fingerprint = (String)request.getAttribute(FINGERPRINT_ATTRIBUTE);
        if (Guard.isNullOrEmpty(jwt)) {
            WebUtils.toHttp(response).sendRedirect(url);
        }
        else {
            WebUtils.toHttp(response).setHeader("Set-Cookie", fingerprint);
            WebUtils.toHttp(response).sendRedirect(url.replaceAll("/+$", "") + "/" + jwt);
        }

        return false;
    }
}
