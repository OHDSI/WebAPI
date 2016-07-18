package org.ohdsi.webapi.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.management.Security;

/**
 *
 * @author gennadiy.anisimov
 */
public class UrlBasedAuthorizingFilter extends AdviceFilter {
  
  private final Security security;
  
  public UrlBasedAuthorizingFilter(Security security) {
    this.security = security;
  }
  
  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    HttpServletRequest httpRequest = WebUtils.toHttp(request);
    
    String path = httpRequest.getPathInfo()
                              .replaceAll("^/+", "")
                              .replaceAll("/+$", "");
    String method = httpRequest.getMethod();    
    String permission = String.format("%s:%s", method, path.replace("/", ":")).toLowerCase();
    
    if (security.isPermitted(permission))    
      return true;
    
    HttpServletResponse httpResponse = WebUtils.toHttp(response);
    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
    return false;
  }
}
