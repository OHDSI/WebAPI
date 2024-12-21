package org.ohdsi.webapi.shiro.filters;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author gennadiy.anisimov
 */
public class UrlBasedAuthorizingFilter extends AdviceFilter {
  
  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    HttpServletRequest httpRequest = WebUtils.toHttp(request);
    
    String path = httpRequest.getPathInfo()
                              .replaceAll("^/+", "")
                              .replaceAll("/+$", "")
                              // replace special characters
                              .replace(":", "&colon;")
                              .replace(",", "&comma;")
                              .replace("*", "&asterisk;");

    String method = httpRequest.getMethod();    
    String permission = "%s:%s".formatted(path.replace("/", ":"), method).toLowerCase();

    if (this.isPermitted(permission))
      return true;
    
    HttpServletResponse httpResponse = WebUtils.toHttp(response);
    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
    return false;
  }

  protected boolean isPermitted(String permission) {
    return SecurityUtils.getSubject().isPermitted(permission);
  };
}
