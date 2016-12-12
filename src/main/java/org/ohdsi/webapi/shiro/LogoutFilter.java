package org.ohdsi.webapi.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.AdviceFilter;

/**
 *
 * @author gennadiy.anisimov
 */
public class LogoutFilter extends AdviceFilter {

  private final Log log = LogFactory.getLog(getClass());
  
  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    Subject subject = SecurityUtils.getSubject();
    try {
        subject.logout();
    } catch (SessionException ise) {
        log.debug("Encountered session exception during logout.  This can generally safely be ignored.", ise);
    }

    return false;
  }
}
