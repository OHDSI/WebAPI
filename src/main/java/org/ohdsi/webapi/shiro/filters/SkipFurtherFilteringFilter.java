package org.ohdsi.webapi.shiro.filters;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.ohdsi.webapi.shiro.ServletBridge;

/**
 *
 * @author gennadiy.anisimov
 */
public abstract class SkipFurtherFilteringFilter implements Filter {

  @Override
  public void init(FilterConfig fc) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (shouldSkip(request, response)) {
      HttpServletRequest httpRequest = ServletBridge.toHttp(request);
      // getPathInfo() can return null if there's no extra path info
      String pathInfo = httpRequest.getPathInfo();
      String path = httpRequest.getServletPath() + (pathInfo != null ? pathInfo : "");
      RequestDispatcher requestDispatcher = request.getRequestDispatcher(path);
      requestDispatcher.forward(request, response);
    }
    else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {
  }

  protected abstract boolean shouldSkip(ServletRequest request, ServletResponse response);
}
