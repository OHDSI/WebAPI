package org.ohdsi.webapi.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet filter that redirects HTTP requests to HTTPS when SSL is enabled.
 * 
 * This filter runs before Spring Security and checks if the incoming request
 * is secure. When behind a reverse proxy, configure server.forward-headers-strategy=NATIVE
 * so that X-Forwarded-Proto headers are respected and request.isSecure() returns
 * the correct value.
 * 
 * Only active when server.ssl.enabled=true in application.yaml.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "server.ssl", name = "enabled", havingValue = "true")
public class SslRedirectFilter implements Filter {

  @Value("${server.port:443}")
  private int serverPort;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    if (!httpRequest.isSecure()) {
      String redirectUrl = buildHttpsUrl(httpRequest);
      httpResponse.sendRedirect(redirectUrl);
      return;
    }

    chain.doFilter(request, response);
  }

  /**
   * Builds the HTTPS URL for redirecting the request.
   * Includes the port from server.port if it's not the standard HTTPS port (443).
   */
  private String buildHttpsUrl(HttpServletRequest request) {
    StringBuilder url = new StringBuilder();
    url.append("https://");
    url.append(request.getServerName());
    
    if (serverPort != 443) {
      url.append(":").append(serverPort);
    }
    
    url.append(request.getRequestURI());
    
    String queryString = request.getQueryString();
    if (queryString != null && !queryString.isEmpty()) {
      url.append("?").append(queryString);
    }
    
    return url.toString();
  }
}
