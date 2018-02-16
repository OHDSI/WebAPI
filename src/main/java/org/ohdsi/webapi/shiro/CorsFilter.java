package org.ohdsi.webapi.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 *
 * @author gennadiy.anisimov
 */
@Component
public class CorsFilter extends AdviceFilter{

  @Value("${security.origin}")
  private String origin;
  @Value("${security.cors.enabled}")
  private Boolean corsEnabled;

  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {

    if (Objects.isNull(corsEnabled) || !corsEnabled) {
      return true;
    }
    // check if it's CORS request
    //
    HttpServletRequest httpRequest = WebUtils.toHttp(request);
    String requestOrigin = httpRequest.getHeader("Origin");
    if (requestOrigin == null) {
      return true;
    }

    // set headers
    //
    HttpServletResponse httpResponse = WebUtils.toHttp(response);
    httpResponse.setHeader("Access-Control-Allow-Origin", this.origin);
    httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

    // stop processing if it's preflight request
    //
    String requestMethod = httpRequest.getHeader("Access-Control-Request-Method");
    String method = httpRequest.getMethod();
    if (requestMethod != null && "OPTIONS".equalsIgnoreCase(method)) {
      httpResponse.setHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
      httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
      httpResponse.setHeader("Access-Control-Max-Age", "1209600");
      httpResponse.setStatus(HttpServletResponse.SC_OK);

      return false;
    }

    // continue processing request
    //
    httpResponse.setHeader("Access-Control-Expose-Headers", "Bearer");
    return true;
  }
}
