package org.ohdsi.webapi.shiro.management;

import java.util.Map;
import java.util.Set;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.realm.Realm;

import javax.servlet.Filter;

/**
 *
 * @author gennadiy.anisimov
 */
public abstract class Security {

  public static final String SOURCE_ACCESS_PERMISSION = "source:%s:access";
  public static String PROFILE_VIEW_DATES_PERMISSION = "*:person:*:get:dates";

  public abstract String getSubject();

  public abstract Set<Realm> getRealms();
  
  public abstract Map<FilterTemplates, Filter> getFilters();
  
  public abstract Map<String, String> getFilterChain();
  
  public abstract Authenticator getAuthenticator();
}
