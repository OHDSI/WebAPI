package org.ohdsi.webapi.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

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
    HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
    if (httpServletRequest.getParameter("code") == null && httpServletRequest.getParameter("ticket") == null) {
      WebUtils.toHttp(response).sendRedirect(redirectUrl);
      return false;
    }
    return true;
  }
}
