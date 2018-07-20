package org.ohdsi.webapi.shiro.filters;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    String permission = String.format("%s:%s", path.replace("/", ":"), method).toLowerCase();

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
