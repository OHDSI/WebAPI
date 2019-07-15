package org.ohdsi.webapi.shiro.filters;

import org.apache.shiro.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseNoCacheFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);

        httpResponse.setHeader("Cache-control", "no-store");
        httpResponse.setHeader("Pragma", "no-cache");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
