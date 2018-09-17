package org.ohdsi.webapi.shiro.filters;

import com.odysseusinc.logging.event.FailedLogoffEvent;
import com.odysseusinc.logging.event.SuccessLogoffEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.springframework.context.ApplicationEventPublisher;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 *
 * @author gennadiy.anisimov
 */
public class LogoutFilter extends AdviceFilter {

  private ApplicationEventPublisher eventPublisher;

  private final Log log = LogFactory.getLog(getClass());

  public LogoutFilter(ApplicationEventPublisher eventPublisher){
    this.eventPublisher = eventPublisher;
  }
  
  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
    Subject subject = SecurityUtils.getSubject();
    try {
        subject.logout();
        eventPublisher.publishEvent(new SuccessLogoffEvent(this));
    } catch (SessionException ise) {
        log.debug("Encountered session exception during logout. This can be generally safely ignored.", ise);
        eventPublisher.publishEvent(new FailedLogoffEvent(this));
    }

    return false;
  }
}
