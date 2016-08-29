package org.ohdsi.webapi.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author gennadiy.anisimov
 */
public class UrlBasedAuthorizingFilter extends AdviceFilter {
  
  private final PermissionManager authorizer;
  
  public UrlBasedAuthorizingFilter(PermissionManager authorizer) {
    this.authorizer = authorizer;
  }
  
  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    HttpServletRequest httpRequest = WebUtils.toHttp(request);
    
    String path = httpRequest.getPathInfo()
                              .replaceAll("^/+", "")
                              .replaceAll("/+$", "");
    String method = httpRequest.getMethod();    
    String permission = String.format("%s:%s", path.replace("/", ":"), method).toLowerCase();
    
    if (this.authorizer.isPermitted(permission))
      return true;
    
    HttpServletResponse httpResponse = WebUtils.toHttp(response);
    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
    return false;
  }
}
