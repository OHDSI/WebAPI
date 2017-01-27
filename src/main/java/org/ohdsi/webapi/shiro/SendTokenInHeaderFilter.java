package org.ohdsi.webapi.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author gennadiy.anisimov
 */
public class SendTokenInHeaderFilter extends AdviceFilter {

  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) {
    String jwt = (String)request.getAttribute("TOKEN");
    
    HttpServletResponse httpResponse = WebUtils.toHttp(response);
    httpResponse.setHeader("Bearer", jwt);
    httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);

    return false;
  }
}
