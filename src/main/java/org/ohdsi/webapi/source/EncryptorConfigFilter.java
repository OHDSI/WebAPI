package org.ohdsi.webapi.source;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class EncryptorConfigFilter implements Filter {

    @Autowired
    private Environment env;
    @Value("${jasypt.encryptor.enabled}")
    private boolean encryptorEnabled;
    private String encryptorPassword;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (!request.getMethod().equals(HttpMethod.OPTIONS.name())
                && !request.getRequestURI().contains("source/encryptor")
                && encryptorEnabled && !isEncryptorReady()) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.addHeader("X-Atlas-Encryptor", "require_pasword");
            response.addHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE);
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        encryptorPassword = env.getProperty("jasypt.encryptor.password");
    }

    private boolean isEncryptorReady(){
        return StringUtils.isNotEmpty(encryptorPassword);
    }

    @Override
    public void destroy() {
    }
}
