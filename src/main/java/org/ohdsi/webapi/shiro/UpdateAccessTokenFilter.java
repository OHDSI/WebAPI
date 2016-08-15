package org.ohdsi.webapi.shiro;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
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
  private final int tokenExpirationIntervalInSeconds;
  private final Set<String> defaultRoles;
  
  public UpdateAccessTokenFilter(
          SimpleAuthorizer authorizer,
          Set<String> defaultRoles,
          int tokenExpirationIntervalInSeconds) {
    this.authorizer = authorizer;
    this.tokenExpirationIntervalInSeconds = tokenExpirationIntervalInSeconds;
    this.defaultRoles = defaultRoles;
  }
  
  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    HttpServletResponse httpResponse = WebUtils.toHttp(response);
    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
    
    Object subjectObject = request.getAttribute(SUBJECT_ATTRIBUTE);
    if (subjectObject == null)
      return false;
    
    String user;    
    if (subjectObject instanceof Subject) {
      Subject subject = (Subject) subjectObject;

      Set<Principal> principals = subject.getPrincipals();
      if (principals.isEmpty())
        return false;

      Principal principal = principals.iterator().next();
      user = principal.getName();      
    } else if (subjectObject instanceof String) {
      user = (String) subjectObject;
    } else {
      return false;
    }
    
    if (user == null || user.isEmpty())
      return false;
    
    this.authorizer.registerUser(user, defaultRoles);
    
    Date expiration = this.getExpiration();
    String jwt = TokenManager.createJsonWebToken(user, expiration);
    
    httpResponse.setHeader("Bearer", jwt);
    httpResponse.setStatus(HttpServletResponse.SC_OK);
    
    return false;
  }
  
  private Date getExpiration() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, this.tokenExpirationIntervalInSeconds);
    return calendar.getTime();    
  }
}
