package com.jnj.honeur.webapi.shiro;

import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.TokenManager;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author gennadiy.anisimov
 */
public class HoneurInvalidateAccessTokenFilter extends AdviceFilter {

  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    HttpServletResponse httpResponse = WebUtils.toHttp(response);
    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    String jwt = HoneurTokenManager.extractToken(request);
    
    if (HoneurTokenManager.invalidate(jwt))
      httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);

    return true;
  }
}
