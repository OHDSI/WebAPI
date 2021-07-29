package org.ohdsi.webapi.shiro.filters.auth;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.helper.Guard;
import org.ohdsi.webapi.shiro.filters.AtlasAuthFilter;
import org.ohdsi.webapi.shiro.tokens.JwtAuthToken;
import org.pac4j.core.context.JEEContext;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.credentials.SAML2Credentials;
import org.pac4j.saml.exceptions.SAMLAuthnInstantException;
import org.pac4j.saml.profile.SAML2Profile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.ohdsi.webapi.shiro.management.AtlasSecurity.AUTH_CLIENT_ATTRIBUTE;
import static org.ohdsi.webapi.shiro.management.AtlasSecurity.AUTH_CLIENT_SAML;

/**
 * SAML authentication callback filter
 */
public class SamlHandleFilter extends AtlasAuthFilter {
    private final SAML2Client saml2Client;
    private final SAML2Client saml2ForceClient;
    protected final String oauthUiCallback;
    private final static String AUTH_COOKIE = "forceAuth";

    public SamlHandleFilter(SAML2Client saml2Client, SAML2Client saml2ForceClient, String oauthUiCallback) {
        this.saml2Client = saml2Client;
        this.saml2ForceClient = saml2ForceClient;
        this.oauthUiCallback = oauthUiCallback;
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

                SAML2Client client;
                if (isForceAuth(request)) {
                    client = saml2ForceClient;
                } else {
                    client = saml2Client;
                }
                SAML2Credentials credentials = client.getCredentials(context).get();
                SAML2Profile samlProfile = (SAML2Profile)client.getUserProfile(credentials, context).get();

                token = new JwtAuthToken(samlProfile.getId());
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
        try {
            boolean loggedIn = executeLogin(request, response);

            return loggedIn;
        } catch (SAMLAuthnInstantException e) {
            if (!isForceAuth(request)) {
                createForceAuthCookie(response);

                WebUtils.toHttp(response).sendRedirect(WebUtils.toHttp(request).getContextPath() + "/user/login/samlForce");
            } else {
                deleteForceAuthCookie(response);

                String urlValue = oauthUiCallback.replaceAll("/+$", "");
                urlValue = urlValue + "/" + AUTH_CLIENT_SAML + "/reloginRequired";
                WebUtils.toHttp(response).sendRedirect(urlValue);
            }
        }
        return false;
    }

    private boolean isForceAuth(ServletRequest request) {
        Cookie[] cookies = WebUtils.toHttp(request).getCookies();
        if (Objects.nonNull(cookies)) {
            for (Cookie cookie: cookies) {
                if (AUTH_COOKIE.equals(cookie.getName())) {
                    return Boolean.TRUE.toString().equals(cookie.getValue());
                }
            }
        }
        return false;
    }

    private void createForceAuthCookie(ServletResponse response) {
        Cookie cookie = new Cookie(AUTH_COOKIE, Boolean.TRUE.toString());
        cookie.setPath("/");
        WebUtils.toHttp(response).addCookie(cookie);
    }

    private void deleteForceAuthCookie(ServletResponse response) {
        Cookie cookie = new Cookie(AUTH_COOKIE, Boolean.TRUE.toString());
        cookie.setMaxAge(0);
        cookie.setPath("/");
        WebUtils.toHttp(response).addCookie(cookie);
    }
}
