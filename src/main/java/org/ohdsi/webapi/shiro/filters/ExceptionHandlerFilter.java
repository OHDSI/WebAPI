package org.ohdsi.webapi.shiro.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Component
@Order(1)
public class ExceptionHandlerFilter implements Filter {
    private final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Throwable e) {
            LOGGER.error("Error during filtering", e);
            // Throw new exception without information of original exception;
            RuntimeException ex = new RuntimeException("An exception ocurred: " + e.getClass().getName());
            // Clean stacktrace, but keep message
            ex.setStackTrace(new StackTraceElement[0]);
            // Throw new exception without information of original exception;
            throw ex;
        }
    }

    @Override
    public void destroy() {
    }
}
