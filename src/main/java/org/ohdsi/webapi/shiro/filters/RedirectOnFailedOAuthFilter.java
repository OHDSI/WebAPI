package org.ohdsi.webapi.shiro.filters;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.ohdsi.webapi.shiro.ServletBridge;
import org.apache.shiro.web.servlet.AdviceFilter;

/**
 *
 * @author gennadiy.anisimov
 */
public class RedirectOnFailedOAuthFilter extends AdviceFilter {

  private String redirectUrl;

  public RedirectOnFailedOAuthFilter(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    if (ServletBridge.toHttp(request).getParameter("code") == null) {
      ServletBridge.toHttp(response).sendRedirect(redirectUrl);
      return false;
    }
    return true;
  }
}
