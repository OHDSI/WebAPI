package org.ohdsi.webapi.shiro.subject;

import org.apache.shiro.mgt.DefaultSubjectFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SubjectFactory;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.subject.WebSubjectContext;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class WebDelegatingRunAsSubjectFactory extends DefaultSubjectFactory implements SubjectFactory {

  public WebDelegatingRunAsSubjectFactory() {
    super();
  }

  @Override
  public Subject createSubject(SubjectContext context) {
    if (!(context instanceof WebSubjectContext)) {
      return super.createSubject(context);
    }
    WebSubjectContext wsc = (WebSubjectContext) context;
    SecurityManager securityManager = wsc.resolveSecurityManager();
    Session session = wsc.resolveSession();
    boolean sessionEnabled = wsc.isSessionCreationEnabled();
    PrincipalCollection principals = wsc.resolvePrincipals();
    boolean authenticated = wsc.resolveAuthenticated();
    String host = wsc.resolveHost();
    ServletRequest request = wsc.resolveServletRequest();
    ServletResponse response = wsc.resolveServletResponse();

    return new WebDelegatingRunAsSubject(principals, authenticated, host, session, sessionEnabled,
            request, response, securityManager);
  }
}
