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
public class HideResourceFilter extends AdviceFilter {

  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {

    HttpServletResponse httpResponse = WebUtils.toHttp(response);
    httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);

    return false;
  }
}
