package org.ohdsi.webapi.shiro.subject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.subject.support.WebDelegatingSubject;
import org.ohdsi.webapi.shiro.runas.DefaultInMemoryRunAsStorage;
import org.ohdsi.webapi.shiro.runas.RunAsStorage;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WebDelegatingRunAsSubject extends WebDelegatingSubject {

  private RunAsStorage runAsStorage;

  public WebDelegatingRunAsSubject(PrincipalCollection principals,
                                   boolean authenticated,
                                   String host,
                                   Session session,
                                   ServletRequest request,
                                   ServletResponse response,
                                   SecurityManager securityManager,
                                   RunAsStorage runAsStorage) {

    super(principals, authenticated, host, session, request, response, securityManager);
    this.runAsStorage = runAsStorage;
  }

  public WebDelegatingRunAsSubject(PrincipalCollection principals,
                                   boolean authenticated,
                                   String host,
                                   Session session,
                                   boolean sessionEnabled,
                                   ServletRequest request,
                                   ServletResponse response,
                                   SecurityManager securityManager) {
    super(principals, authenticated, host, session, sessionEnabled, request, response, securityManager);
    this.runAsStorage = new DefaultInMemoryRunAsStorage();
  }


  public WebDelegatingRunAsSubject(PrincipalCollection principals,
                                   boolean authenticated,
                                   String host,
                                   Session session,
                                   boolean sessionEnabled,
                                   ServletRequest request,
                                   ServletResponse response,
                                   SecurityManager securityManager,
                                   RunAsStorage runAsStorage) {

    super(principals, authenticated, host, session, sessionEnabled, request, response, securityManager);
    this.runAsStorage = runAsStorage;
  }

  @Override
  public PrincipalCollection getPrincipals() {

    List<PrincipalCollection> runAsPrincipals = getRunAsPrincipalStack();
    return CollectionUtils.isEmpty(runAsPrincipals) ? this.principals : runAsPrincipals.get(0);
  }

  @Override
  public void logout() {

    releaseRunAs();
    super.logout();
  }

  @Override
  public void runAs(PrincipalCollection principals) {

    if (!hasPrincipals()) {
      String msg = "This subject does not yet have an identity.  Assuming the identity of another " +
              "Subject is only allowed for Subjects with an existing identity.  Try logging this subject in " +
              "first, or using the " + Subject.Builder.class.getName() + " to build ad hoc Subject instances " +
              "with identities as necessary.";
      throw new IllegalStateException(msg);
    }
    if (supportsRunAs()) {
      runAsStorage.pushPrincipals(this.principals.getPrimaryPrincipal(), principals);
    }
  }

  protected boolean supportsRunAs() {

    return Objects.nonNull(runAsStorage) && Objects.nonNull(this.principals);
  }

  @Override
  public boolean isRunAs() {

    List<PrincipalCollection> stack = getRunAsPrincipalStack();
    return !CollectionUtils.isEmpty(stack);
  }

  @Override
  public PrincipalCollection getPreviousPrincipals() {

    PrincipalCollection previousPrincipals = null;
    if (supportsRunAs()) {
      final List<PrincipalCollection> stack = runAsStorage.getRunAsPrincipalStack(this.principals.getPrimaryPrincipal());
      int stackSize = stack != null ? stack.size() : 0;
      if (stackSize > 0) {
        if (stackSize == 1) {
          previousPrincipals = this.principals;
        } else {
          previousPrincipals = stack.get(1);
        }
      }
    }
    return previousPrincipals;
  }

  @Override
  public PrincipalCollection releaseRunAs() {

    if (supportsRunAs()) {
      return runAsStorage.popPrincipals(this.principals.getPrimaryPrincipal());
    } else {
      return null;
    }
  }

  protected List<PrincipalCollection> getRunAsPrincipalStack() {

    if (supportsRunAs()) {
      return runAsStorage.getRunAsPrincipalStack(this.principals.getPrimaryPrincipal());
    } else {
      return Collections.emptyList();
    }
  }
}
