package org.ohdsi.webapi.shiro;

import static org.ohdsi.webapi.shiro.management.AtlasSecurity.PERMISSIONS_ATTRIBUTE;
import static org.ohdsi.webapi.shiro.management.AtlasSecurity.TOKEN_ATTRIBUTE;

import io.buji.pac4j.subject.Pac4jPrincipal;
import java.security.Principal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.management.AtlasSecurity;

/**
 *
 * @author gennadiy.anisimov
 */
public class UpdateAccessTokenFilter extends AdviceFilter {

  private final PermissionManager authorizer;
  private final int tokenExpirationIntervalInSeconds;
  private final Set<String> defaultRoles;
  private final Map<String,Filter> filters;
  private Map<String,String> filterClassMap;
  private Map<String,Boolean> caseSensitive;

  public UpdateAccessTokenFilter(
          PermissionManager authorizer,
          Set<String> defaultRoles,
          int tokenExpirationIntervalInSeconds,
          Map<String, Filter> filters,
          Map<String, Boolean> caseSensitive) {
    this.authorizer = authorizer;
    this.tokenExpirationIntervalInSeconds = tokenExpirationIntervalInSeconds;
    this.defaultRoles = defaultRoles;
    this.filters = filters;
    this.caseSensitive = caseSensitive;
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
    } else if (principal instanceof Pac4jPrincipal) {
      login = ((Pac4jPrincipal)principal).getProfile().getEmail();
      if (login == null) {
        // user doesn't provide email - send empty token
        jwt = "";
      }
    } else if (principal instanceof String) {
      login = (String)principal;
    } else {
      throw new Exception("Unknown type of principal");
    }

    login = transformLogin(request, login);

    // stop session to make logout of OAuth users possible
    Session session = SecurityUtils.getSubject().getSession(false);
    if (session != null) {
      session.stop();
    }

    if (jwt == null) {
      this.authorizer.registerUser(login, defaultRoles);

      Date expiration = this.getExpirationDate(this.tokenExpirationIntervalInSeconds);
      jwt = TokenManager.createJsonWebToken(login, expiration);
    }

    request.setAttribute(TOKEN_ATTRIBUTE, jwt);
    Collection<String> permissions = this.authorizer.getAuthorizationInfo(login).getStringPermissions();
    request.setAttribute(PERMISSIONS_ATTRIBUTE, StringUtils.join(permissions, "|"));
    return true;
  }

  private String transformLogin(final ServletRequest request, final String login) {

    Boolean isCaseSensitive = Boolean.FALSE;
    String filterClass = (String)request.getAttribute(AtlasSecurity.AUTH_FILTER_ATTRIBUTE);
    if (Objects.nonNull(filterClass) && Objects.nonNull(caseSensitive)) {
        ensureFilterClassMap();
        String filterName = filterClassMap.get(filterClass);
        if (Objects.nonNull(filterName)) {
            isCaseSensitive = caseSensitive.getOrDefault(filterName, Boolean.FALSE);
        }
    }
    return isCaseSensitive ? login : login.toLowerCase();
  }

  private void ensureFilterClassMap(){
      if (Objects.isNull(filterClassMap) || filterClassMap.isEmpty()){
          synchronized (UpdateAccessTokenFilter.class){
              if (Objects.isNull(filterClassMap) || filterClassMap.isEmpty()){
                  filterClassMap = filters.keySet().stream()
                          .filter(k -> filters.get(k) instanceof AuthenticatingFilter)
                          .collect(Collectors.toMap(k -> filters.get(k).getClass().getName(), k -> k));
              }
          }
      }
  }

  private Date getExpirationDate(final int expirationIntervalInSeconds) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, expirationIntervalInSeconds);
    return calendar.getTime();
  }
}
