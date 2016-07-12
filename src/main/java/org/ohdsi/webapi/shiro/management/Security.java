package org.ohdsi.webapi.shiro.management;

import java.util.Map;
import java.util.Set;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.realm.Realm;
import org.ohdsi.webapi.exceptions.HttpForbiddenException;
import org.springframework.stereotype.Component;

/**
 *
 * @author gennadiy.anisimov
 */
@Component
public abstract class Security {

  public abstract Set<Realm> getRealms();
  
  public abstract Map<String, javax.servlet.Filter> getFilters();
  
  public abstract Map<String, String> getFilterChain();
  
  public abstract Authenticator getAuthenticator();
  
  public abstract void registerUser(String login);

  public abstract void login(AuthenticationToken token);

  public abstract String getAccessToken(String login);

  public abstract void checkPermission(String permission) throws HttpForbiddenException ;
}
