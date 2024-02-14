package org.ohdsi.webapi.shiro.filters;

import static org.ohdsi.webapi.shiro.management.AtlasSecurity.AUTH_CLIENT_ATTRIBUTE;
import static org.ohdsi.webapi.shiro.management.AtlasSecurity.PERMISSIONS_ATTRIBUTE;
import static org.ohdsi.webapi.shiro.management.AtlasSecurity.TOKEN_ATTRIBUTE;

import io.buji.pac4j.subject.Pac4jPrincipal;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.shiro.Entities.UserPrincipal;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.TokenManager;
import org.ohdsi.webapi.util.UserUtils;
import org.pac4j.core.profile.CommonProfile;

/**
 *
 * @author gennadiy.anisimov
 */
public class UpdateAccessTokenFilter extends AdviceFilter {
  
  private final PermissionManager authorizer;
  private final int tokenExpirationIntervalInSeconds;
  private final Set<String> defaultRoles;
  private final String onFailRedirectUrl;

  public UpdateAccessTokenFilter(
          PermissionManager authorizer,
          Set<String> defaultRoles,
          int tokenExpirationIntervalInSeconds,
          String onFailRedirectUrl) {
    this.authorizer = authorizer;
    this.tokenExpirationIntervalInSeconds = tokenExpirationIntervalInSeconds;
    this.defaultRoles = defaultRoles;
    this.onFailRedirectUrl = onFailRedirectUrl;
  }
  
  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    if (!SecurityUtils.getSubject().isAuthenticated()) {
      WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }

    String login;
    String name = null;
    String jwt = null;
    final PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
    Object principal = principals.getPrimaryPrincipal();
    
    if (principal instanceof Pac4jPrincipal) {
      login = ((Pac4jPrincipal)principal).getProfile().getEmail();
      name = ((Pac4jPrincipal)principal).getProfile().getDisplayName();
      
      /**
      * for CAS login
      */
      ShiroHttpServletRequest requestShiro = (ShiroHttpServletRequest) request;
      HttpSession shiroSession = requestShiro.getSession();
      if (login == null && shiroSession.getAttribute(CasHandleFilter.CONST_CAS_AUTHN) != null
              && ((String) shiroSession.getAttribute(CasHandleFilter.CONST_CAS_AUTHN)).equalsIgnoreCase("true")) {
              login = ((Pac4jPrincipal) principal).getProfile().getId();
      }
            
      if (login == null) {
        // user doesn't provide email - send empty token
        request.setAttribute(TOKEN_ATTRIBUTE, "");
        // stop session to make logout of OAuth users possible
        Session session = SecurityUtils.getSubject().getSession(false);
        if (session != null) {
          session.stop();
        }

        HttpServletResponse httpResponse = WebUtils.toHttp(response);

        URI oauthFailURI = getOAuthFailUri();
        httpResponse.sendRedirect(oauthFailURI.toString());
        return false;
      }

      CommonProfile profile = (((Pac4jPrincipal) principal).getProfile());
      if (Objects.nonNull(profile)) {
        String clientName = profile.getClientName();
        request.setAttribute(AUTH_CLIENT_ATTRIBUTE, clientName);
      }
    } else     if (principal instanceof Principal) {
      login = ((Principal) principal).getName();
    } else if (principal instanceof UserPrincipal){
      login = ((UserPrincipal) principal).getUsername();
      name = ((UserPrincipal) principal).getName();
    } else if (principal instanceof String) {
      login = (String)principal;
    } else {
      throw new Exception("Unknown type of principal");
    }

    login = UserUtils.toLowerCase(login);

    // stop session to make logout of OAuth users possible
    Session session = SecurityUtils.getSubject().getSession(false);
    if (session != null) {
      session.stop();
    }

    if (jwt == null) {
      if (name == null) {
        name = login;
      }
      try {
        this.authorizer.registerUser(login, name, defaultRoles);
      } catch (Exception e) {
        WebUtils.toHttp(response).setHeader("x-auth-error", e.getMessage());
        throw new Exception(e);
      }

      String sessionId = (String) request.getAttribute(Constants.SESSION_ID);
      if (sessionId == null) {
        final String token = TokenManager.extractToken(request);
        if (token != null) {
          sessionId = (String) TokenManager.getBody(token).get(Constants.SESSION_ID);
        }
      }

      Date expiration = this.getExpirationDate(this.tokenExpirationIntervalInSeconds);
      jwt = TokenManager.createJsonWebToken(login, sessionId, expiration);
    }

    request.setAttribute(TOKEN_ATTRIBUTE, jwt);
    PermissionManager.PermissionsDTO permissions = this.authorizer.queryUserPermissions(login);
    request.setAttribute(PERMISSIONS_ATTRIBUTE, permissions);
    return true;
  }

  private URI getOAuthFailUri() throws URISyntaxException {
    return getFailUri("oauth_error_email");
  }

  private URI getFailUri(String failFragment) throws URISyntaxException {

    URI oauthFailURI = new URI(onFailRedirectUrl);
    String fragment = oauthFailURI.getFragment();
    StringBuilder sbFragment = new StringBuilder();
    if(fragment == null) {
      sbFragment.append(failFragment).append("/");
    } else if(fragment.endsWith("/")){
      sbFragment.append(fragment).append(failFragment).append("/");
    } else {
      sbFragment.append(fragment).append("/").append(failFragment).append("/");
    }
    return UriBuilder.fromUri(oauthFailURI).fragment(sbFragment.toString()).build();
  }

  private Date getExpirationDate(final int expirationIntervalInSeconds) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, expirationIntervalInSeconds);
    return calendar.getTime();
  }
}
