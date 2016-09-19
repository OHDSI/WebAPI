package org.ohdsi.webapi.shiro;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.web.util.WebUtils;

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
      HttpServletRequest httpRequest = WebUtils.toHttp(request);
      String path = httpRequest.getServletPath() + httpRequest.getPathInfo();
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
