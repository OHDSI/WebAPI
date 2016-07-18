package org.ohdsi.webapi.shiro;

import java.security.Principal;
import java.util.Set;
import javax.security.auth.Subject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author gennadiy.anisimov
 */
public class UpdateAccessTokenFilter extends AdviceFilter {

  private final String SUBJECT_ATTRIBUTE = "MY_SUBJECT";
  
  private final SimpleAuthorizer authorizer;
  
  public UpdateAccessTokenFilter(SimpleAuthorizer authorizer) {
    this.authorizer = authorizer;
  }
  
  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    Subject subject = (Subject) request.getAttribute(SUBJECT_ATTRIBUTE);
    
    if (subject == null)
      return false;
    
    Set<Principal> principals = subject.getPrincipals();
    if (principals.isEmpty())
      return false;
    
    Principal principal = principals.iterator().next();
    String user = principal.getName();
    
    this.authorizer.registerUser(user);
    String jwt = TokenManager.createJsonWebToken(user);
    
    HttpServletResponse httpResponse = WebUtils.toHttp(response);
    httpResponse.setHeader("Bearer", jwt);
    httpResponse.setStatus(HttpServletResponse.SC_OK);
    
    return false;
  }
}
