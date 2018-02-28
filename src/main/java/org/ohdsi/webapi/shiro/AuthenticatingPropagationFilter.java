package org.ohdsi.webapi.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.management.AtlasSecurity;

public abstract class AuthenticatingPropagationFilter extends AuthenticatingFilter {

    public static final String HEADER_AUTH_ERROR = "x-auth-error";

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {

        request.setAttribute(AtlasSecurity.AUTH_FILTER_ATTRIBUTE, this.getClass().getName());
        return true;
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {

        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        if (e instanceof LockedAccountException) {
            httpResponse.setHeader(HEADER_AUTH_ERROR, e.getMessage());
        }
        return super.onLoginFailure(token, e, request, response);
    }
}
