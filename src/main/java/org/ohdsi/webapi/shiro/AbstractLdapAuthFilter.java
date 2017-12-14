package org.ohdsi.webapi.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;

public abstract class AbstractLdapAuthFilter<T extends UsernamePasswordToken> extends AuthenticatingFilter {
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {

        final String name = request.getParameter("login");
        final String password = request.getParameter("password");
        T token;
        if (name != null && password != null) {
            token = getToken();
            token.setUsername(name);
            token.setPassword(password.toCharArray());
        } else {
            throw new AuthenticationException("Empty credentials");
        }

        return token;
    }

    protected abstract T getToken();

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {

        boolean loggedIn = false;

        if (request.getParameter("login") != null) {
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
}
