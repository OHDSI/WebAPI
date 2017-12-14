package org.ohdsi.webapi.shiro.filters;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.tokens.SpnegoToken;

public class KerberosAuthFilter extends AuthenticatingFilter {

    private String getAuthHeader(ServletRequest servletRequest) {

        HttpServletRequest request = WebUtils.toHttp(servletRequest);
        return request.getHeader("Authorization");
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {

        String authHeader = getAuthHeader(servletRequest);
        AuthenticationToken authToken = null;

        if (authHeader != null) {
            byte[] token = Base64.decode(authHeader.replaceAll("^Negotiate ", ""));
            authToken = new SpnegoToken(token);
        }

        return authToken;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {

        boolean loggedIn = false;
        String authHeader = getAuthHeader(servletRequest);

        if (authHeader != null) {
            try {
                loggedIn = executeLogin(servletRequest, servletResponse);
            } catch (AuthenticationException ae) {
                loggedIn = false;
            }
        }

        if (!loggedIn) {
            HttpServletResponse response = WebUtils.toHttp(servletResponse);
            response.addHeader("WWW-Authenticate", "Negotiate");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return loggedIn;
    }
}
