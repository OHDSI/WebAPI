package org.ohdsi.webapi.shiro.filters;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;

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
    if (WebUtils.toHttp(request).getParameter("code") == null) {
      WebUtils.toHttp(response).sendRedirect(redirectUrl);
      return false;
    }
    return true;
  }
}
