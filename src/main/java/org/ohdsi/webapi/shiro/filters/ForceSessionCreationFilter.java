package org.ohdsi.webapi.shiro.filters;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.web.servlet.AdviceFilter;

/**
 *
 * @author gennadiy.anisimov
 */
public class ForceSessionCreationFilter extends AdviceFilter {

  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    request.setAttribute(DefaultSubjectContext.SESSION_CREATION_ENABLED, Boolean.TRUE);
    Session session = SecurityUtils.getSubject().getSession(true);
    if (session == null) {
      throw new Exception("Can't create web session");
    }

    return true;
  }
}
