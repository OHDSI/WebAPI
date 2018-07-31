package com.jnj.honeur.webapi.cas.filter;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * This filter extends the TGC cookie lifetime with 15 minutes, if the cookie is present on the request
 * and CAS is enabled.
 */
public class CASSessionFilter extends AdviceFilter {

    private static final Log log = LogFactory.getLog(CASSessionFilter.class);

    private boolean casEnabled;
    private String domain;

    public CASSessionFilter(boolean casEnabled, String domain) {
        this.casEnabled = casEnabled;
        this.domain = domain;
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {

        if (!casEnabled) {
            log.debug("No Cas Configuration");
            return true;
        }

        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        HttpServletResponse httpResponse = WebUtils.toHttp(response);

        String cookie = httpRequest.getHeader("Cookie");

        if (cookie != null) {
            Optional<String> tgcCookieOptional =
                    Arrays.stream(cookie.split("; ")).filter(c -> c.contains("TGC="))
                            .findFirst();
            if (tgcCookieOptional.isPresent()) {
                String tgcCookie = tgcCookieOptional.get();
                log.debug("TGC Cookie Lifetime Extended");

                ArrayList<String> setCookieHeader = new ArrayList<>();

                setCookieHeader.add(tgcCookie);
                setCookieHeader.add("Max-Age=900");
                setCookieHeader.add("Path=/");
                setCookieHeader.add("Domain="+domain);
                setCookieHeader.add("Secure");
                setCookieHeader.add("HttpOnly");

                httpResponse.setHeader("Set-Cookie", String.join("; ", setCookieHeader));
            }
        }

        return true;
    }
}
