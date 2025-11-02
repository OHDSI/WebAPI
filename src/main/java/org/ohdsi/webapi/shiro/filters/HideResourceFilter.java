package org.ohdsi.webapi.shiro.filters;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.ohdsi.webapi.shiro.ServletBridge;
import org.apache.shiro.web.servlet.AdviceFilter;

/**
 *
 * @author gennadiy.anisimov
 */
public class HideResourceFilter extends AdviceFilter {

  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {

    HttpServletResponse httpResponse = ServletBridge.toHttp(response);
    httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);

    return false;
  }
}
