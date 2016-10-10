package org.ohdsi.webapi.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author gennadiy.anisimov
 */
public class SendTokenInUrlFilter extends AdviceFilter {

  private String url;

  public SendTokenInUrlFilter(String url) {
    this.url = url;
  }

  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    String jwt = (String)request.getAttribute("TOKEN");
    String fullUrl = url.replaceAll("/+$", "") + "/" + jwt;
    WebUtils.toHttp(response).sendRedirect(fullUrl);

    return false;
  }
}
