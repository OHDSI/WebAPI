package org.ohdsi.webapi.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.ohdsi.webapi.shiro.management.AtlasSecurity;
import waffle.shiro.negotiate.NegotiateAuthenticationFilter;

public class NegotiateAuthenticationPropagationFilter extends NegotiateAuthenticationFilter {

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {

        request.setAttribute(AtlasSecurity.AUTH_FILTER_ATTRIBUTE, this.getClass().getName());
        return super.onLoginSuccess(token, subject, request, response);
    }
}
