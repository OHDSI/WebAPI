package org.ohdsi.webapi.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;

/**
 *
 * @author gennadiy.anisimov
 */
public class InvalidateAccessTokenFilter extends AdviceFilter {

  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    HttpServletResponse httpResponse = WebUtils.toHttp(response);
    
    String jwt = TokenManager.extractToken(request);
    if (jwt == null) 
      return false;      
    
    TokenManager.invalidate(jwt);
    
    httpResponse.setStatus(HttpStatus.OK.value());

    return false;
  }
}
