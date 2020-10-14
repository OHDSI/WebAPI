package org.ohdsi.webapi.shiro.filters;

import io.buji.pac4j.token.Pac4jToken;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * CAS authentication callback filter
 */
public class CasHandleFilter extends AtlasAuthFilter {
    
    
    private final Logger logger = LoggerFactory.getLogger(CasHandleFilter.class);
    
    /**
     * attribute name to recognize CAS authentication. Get login UpdateAccessTokenFilter
     * differently.
     */
    public static final String CONST_CAS_AUTHN = "webapi.shiro.cas";
    
    private TicketValidator ticketValidator;
    
    private String casCallbackUrl;
    
    private String casticket;
    
    /**
     * @param ticketValidator TicketValidator for service ticket validation
     * @param casCallbackUrl CAS callback URL
     * @param casticket parameter name for ticket to validate
     */
    public CasHandleFilter(TicketValidator ticketValidator, String casCallbackUrl, String casticket) {
        super();
        this.ticketValidator = ticketValidator;
        this.casCallbackUrl = casCallbackUrl;
        this.casticket = casticket;
    }
    
    /**
     * @see org.apache.shiro.web.filter.authc.AuthenticatingFilter#createToken(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse)
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest,
                                              ServletResponse servletResponse) throws Exception {
        final ShiroHttpServletRequest request = (ShiroHttpServletRequest) servletRequest;
        HttpSession session = request.getSession();
        Pac4jToken ct = null;
        if (session != null) {
            session.setAttribute(CONST_CAS_AUTHN, "true");
            
            if (!SecurityUtils.getSubject().isAuthenticated()) {
                String ticket = request.getParameter(this.casticket);
                
                if (ticket != null) {
                    String service = casCallbackUrl;
                    
                    try {
                        
                        final Assertion assertion = this.ticketValidator.validate(ticket, service);
                        final AttributePrincipal principal = assertion.getPrincipal();
                        final CasProfile casProfile = new CasProfile();
                        casProfile.setId(principal.getName());
                        casProfile.addAttributes(principal.getAttributes());
                        
                        Subject currentUser = SecurityUtils.getSubject();
                        ct = new Pac4jToken(Collections.singletonList(casProfile), currentUser.isRemembered());
                        /*
                         * let AuthenticatingFilter.executeLogin login user
                         */
                        //currentUser.login(ct);
                        
                    } catch (TicketValidationException e) {
                        throw new AuthenticationException(e);
                    }
                }
                
            }
        }
        return ct;
    }
    
    /**
     * @see org.apache.shiro.web.filter.AccessControlFilter#onAccessDenied(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse)
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        boolean loggedIn = executeLogin(request, response);
        
        return loggedIn;
    }
}
