package org.ohdsi.webapi.shiro.filters;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.helper.Guard;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import static org.ohdsi.webapi.shiro.management.AtlasSecurity.AUTH_CLIENT_ALL;
import static org.ohdsi.webapi.shiro.management.AtlasSecurity.AUTH_CLIENT_ATTRIBUTE;

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
    String client = (String)request.getAttribute(AUTH_CLIENT_ATTRIBUTE);
    String clientValue = StringUtils.isNotEmpty(client) ? AUTH_CLIENT_ALL : client;
    String urlValue = url.replaceAll("/+$", "");
    if (!Guard.isNullOrEmpty(jwt)) {
        urlValue = urlValue + "/" + clientValue + "/" + jwt;
    }
    WebUtils.toHttp(response).sendRedirect(urlValue);

    return false;
  }
}
