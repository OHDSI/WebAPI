package org.ohdsi.webapi.shiro.management;

import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;

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
  
  public abstract ModularRealmAuthenticator getAuthenticator();
}
