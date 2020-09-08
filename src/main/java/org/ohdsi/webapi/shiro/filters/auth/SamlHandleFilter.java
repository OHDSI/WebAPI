package org.ohdsi.webapi.shiro.filters.auth;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.ohdsi.webapi.shiro.filters.AtlasAuthFilter;
import org.ohdsi.webapi.shiro.tokens.JwtAuthToken;
import org.pac4j.core.context.JEEContext;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.credentials.SAML2Credentials;
import org.pac4j.saml.profile.SAML2Profile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.ohdsi.webapi.shiro.management.AtlasSecurity.AUTH_CLIENT_ATTRIBUTE;
import static org.ohdsi.webapi.shiro.management.AtlasSecurity.AUTH_CLIENT_SAML;

/**
 * SAML authentication callback filter
 */
public class SamlHandleFilter extends AtlasAuthFilter {
    private final SAML2Client saml2Client;

    public SamlHandleFilter(SAML2Client saml2Client) {
        this.saml2Client = saml2Client;
    }

    /**
     * @see org.apache.shiro.web.filter.authc.AuthenticatingFilter#createToken(ServletRequest,
     *      ServletResponse)
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest,
                                              ServletResponse servletResponse) throws Exception {
        final ShiroHttpServletRequest request = (ShiroHttpServletRequest) servletRequest;
        AuthenticationToken token = null;
        if (request.getSession() != null) {
            if (!SecurityUtils.getSubject().isAuthenticated()) {
                request.setAttribute(AUTH_CLIENT_ATTRIBUTE, AUTH_CLIENT_SAML);

                HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
                HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
                JEEContext context = new JEEContext(httpRequest, httpResponse);

                SAML2Credentials credentials = saml2Client.getCredentials(context).get();
                SAML2Profile samlProfile = (SAML2Profile)saml2Client.getUserProfile(credentials, context).get();

                token = new JwtAuthToken(samlProfile.getEmail());
            }
        }
        return token;
    }

    /**
     * @see org.apache.shiro.web.filter.AccessControlFilter#onAccessDenied(ServletRequest,
     *      ServletResponse)
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        boolean loggedIn = executeLogin(request, response);
        
        return loggedIn;
    }
}
