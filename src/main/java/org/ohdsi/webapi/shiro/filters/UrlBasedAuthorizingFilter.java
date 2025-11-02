package org.ohdsi.webapi.shiro.filters;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ohdsi.webapi.shiro.ServletBridge;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.servlet.AdviceFilter;

/**
 *
 * @author gennadiy.anisimov
 */
public class UrlBasedAuthorizingFilter extends AdviceFilter {
  
  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    HttpServletRequest httpRequest = ServletBridge.toHttp(request);
    
    String path = httpRequest.getPathInfo()
                              .replaceAll("^/+", "")
                              .replaceAll("/+$", "")
                              // replace special characters
                              .replace(":", "&colon;")
                              .replace(",", "&comma;")
                              .replace("*", "&asterisk;");

    String method = httpRequest.getMethod();    
    String permission = String.format("%s:%s", path.replace("/", ":"), method).toLowerCase();

    if (this.isPermitted(permission))
      return true;
    
    HttpServletResponse httpResponse = ServletBridge.toHttp(response);
    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
    return false;
  }

  protected boolean isPermitted(String permission) {
    return SecurityUtils.getSubject().isPermitted(permission);
  };
}
