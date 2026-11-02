package org.ohdsi.webapi.security.spring;

import java.util.function.Supplier;

import org.aopalliance.intercept.MethodInvocation;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.springframework.expression.EvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class WebApiMethodSecurityExpressionHandler
    extends DefaultMethodSecurityExpressionHandler {

  private final AuthorizationService authorizationService;

  public WebApiMethodSecurityExpressionHandler(
      AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  @Override
  protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
      Authentication authentication,
      MethodInvocation invocation) {
    WebApiSecurityExpressionRoot root = new WebApiSecurityExpressionRoot(authentication,
        authorizationService);

    root.setPermissionEvaluator(getPermissionEvaluator());
    root.setRoleHierarchy(getRoleHierarchy());
    root.setTrustResolver(getTrustResolver());

    return root;
  }

  @Override
  public EvaluationContext createEvaluationContext(Supplier<Authentication> authentication,
      MethodInvocation invocation) {
    return createEvaluationContext(authentication.get(), invocation);
  }
}
