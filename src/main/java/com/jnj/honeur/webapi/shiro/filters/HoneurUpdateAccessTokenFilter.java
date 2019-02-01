package com.jnj.honeur.webapi.shiro.filters;

import com.jnj.honeur.security.SecurityUtils2;
import com.jnj.honeur.webapi.shiro.HoneurTokenManager;
import io.buji.pac4j.subject.Pac4jPrincipal;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.PermissionManager;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import static com.jnj.honeur.webapi.shiro.management.AtlasCustomSecurity.FINGERPRINT_ATTRIBUTE;
import static org.ohdsi.webapi.shiro.management.AtlasSecurity.PERMISSIONS_ATTRIBUTE;
import static org.ohdsi.webapi.shiro.management.AtlasSecurity.TOKEN_ATTRIBUTE;

/**
 *
 * @author gennadiy.anisimov
 */
public class HoneurUpdateAccessTokenFilter extends AdviceFilter {
  
  private final PermissionManager authorizer;
  private final int tokenExpirationIntervalInSeconds;
  private final Set<String> defaultRoles;
  private final String cookieDomain;
  
  public HoneurUpdateAccessTokenFilter(
          PermissionManager authorizer,
          Set<String> defaultRoles,
          int tokenExpirationIntervalInSeconds,
          String cookieDomain) {
    this.authorizer = authorizer;
    this.tokenExpirationIntervalInSeconds = tokenExpirationIntervalInSeconds;
    this.defaultRoles = defaultRoles;
    this.cookieDomain = cookieDomain;
  }
  
  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    if (!SecurityUtils.getSubject().isAuthenticated()) {
      WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }

    String login;
    String jwt = null;
    final PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
    Object principal = principals.getPrimaryPrincipal();

    if (principal instanceof Principal) {
      login = ((Principal)principal).getName();
    } else if (principal instanceof Pac4jPrincipal){
      login = ((Pac4jPrincipal)principal).getProfile().getId();
    } else if (principal instanceof String) {
      login = (String)principal;
    } else {
      throw new Exception("Unknown type of principal");
    }

    // stop session to make logout of OAuth users possible
    Session session = SecurityUtils.getSubject().getSession(false);
    if (session != null) {
      session.stop();
    }

    if (jwt == null) {
      this.authorizer.registerUser(login, defaultRoles);

      Date expiration = HoneurTokenManager.getExpirationDate(this.tokenExpirationIntervalInSeconds);
      String fingerprint = SecurityUtils2.generateFingerprint();
      ArrayList<String> setCookieHeader = new ArrayList<>();

      setCookieHeader.add(String.format("userFingerprint=%s", fingerprint));
      setCookieHeader.add(String.format("Domain=%s", cookieDomain));
      setCookieHeader.add("Path=/");
      setCookieHeader.add(String.format("Max-Age=%s", tokenExpirationIntervalInSeconds));
      setCookieHeader.add("Secure");
      setCookieHeader.add("HttpOnly");

      request.setAttribute(FINGERPRINT_ATTRIBUTE, String.join(";", setCookieHeader));

      jwt = HoneurTokenManager.createJsonWebToken(login, expiration, fingerprint);
    }

    request.setAttribute(TOKEN_ATTRIBUTE, jwt);
    Collection<String> permissions = this.authorizer.getAuthorizationInfo(login).getStringPermissions();
    request.setAttribute(PERMISSIONS_ATTRIBUTE, StringUtils.join(permissions, "|"));
    return true;
  }
}
