package org.ohdsi.webapi.shiro.filters;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pac4j.core.config.Config;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.profile.CommonProfile;

import io.buji.pac4j.token.Pac4jToken;

/**
 *
 */
public class CasHandleFilter implements Filter {
    
    
    private final Log logger = LogFactory.getLog(CasHandleFilter.class);
    
    public static final String CONST_CAS_AUTHN = "webapi.shiro.cas";
    
    private CasClient casClient;
    
    private Config config;
    
    private TicketValidator ticketValidator;
    
    private String casCallbackUrl;
    
    private String casticket;
    
    /**
     * 
     */
    public CasHandleFilter(CasClient casClient, Config config, TicketValidator ticketValidator, String casCallbackUrl,
        String casticket) {
        super();
        this.casClient = casClient;
        this.config = config;
        this.ticketValidator = ticketValidator;
        this.casCallbackUrl = casCallbackUrl;
        this.casticket = casticket;
    }
    
    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {        
    }
    
    /**
     * (non-Jsdoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        
        final ShiroHttpServletRequest request = (ShiroHttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        if (session != null) {
            session.setAttribute(CONST_CAS_AUTHN, "true");
            
            if (!SecurityUtils.getSubject().isAuthenticated()) {
                String ticket = (String) request.getParameter(this.casticket);
                
                if (ticket != null) {
                    String service = casCallbackUrl;
                    
                    //logger.info("service here...:::::" + service);
                    try {
                        
                        final Assertion assertion = this.ticketValidator.validate(ticket, service);
                        final AttributePrincipal principal = assertion.getPrincipal();
                        final CasProfile casProfile = new CasProfile();
                        casProfile.setId(principal.getName());
                        casProfile.addAttributes(principal.getAttributes());
                        
                        Subject currentUser = SecurityUtils.getSubject();
                        LinkedHashMap<String, CommonProfile> pMap = new LinkedHashMap<String, CommonProfile>();
                        pMap.put(principal.getName(), casProfile);
                        Pac4jToken ct = new Pac4jToken(pMap, currentUser.isRemembered());
                        currentUser.login(ct);
                        
                    } catch (TicketValidationException e) {
                        logger.error(e);
                        e.printStackTrace();
                    }
                }
                
            }
        }
        chain.doFilter(request, response);
    }
    
    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub
    }
}
