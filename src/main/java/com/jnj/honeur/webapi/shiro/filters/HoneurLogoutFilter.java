package com.jnj.honeur.webapi.shiro.filters;

import com.jnj.honeur.webapi.shiro.HoneurTokenManager;
import com.odysseusinc.logging.event.FailedLogoutEvent;
import com.odysseusinc.logging.event.SuccessLogoutEvent;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;

public class HoneurLogoutFilter extends AdviceFilter {

    private ApplicationEventPublisher eventPublisher;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public HoneurLogoutFilter(final ApplicationEventPublisher eventPublisher){
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String jwt = HoneurTokenManager.extractToken(request);

        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        Optional<Cookie> fingerprintCookie = Arrays.stream(httpServletRequest.getCookies())
                .filter(cookie -> cookie.getName().equals("userFingerprint")).findFirst();

        if(fingerprintCookie.isPresent()) {
            String fingerprint = fingerprintCookie.get().getValue();
            String principal = HoneurTokenManager.getSubject(jwt, fingerprint);

            if (HoneurTokenManager.invalidate(jwt, fingerprint)) {
                httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }

            Subject subject = SecurityUtils.getSubject();
            if(subject != null) {
                try {
                    subject.logout();
                    eventPublisher.publishEvent(new SuccessLogoutEvent(this, principal));
                } catch (SessionException ise) {
                    LOGGER.warn("Encountered session exception during logout. This can be generally safely ignored", ise);
                    eventPublisher.publishEvent(new FailedLogoutEvent(this, principal));
                }
            }
        }

        return false;
    }
}
