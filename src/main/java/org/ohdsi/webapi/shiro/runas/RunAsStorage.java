package org.ohdsi.webapi.shiro.runas;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.List;

public interface RunAsStorage {

  void pushPrincipals(Object principal, PrincipalCollection principals);

  PrincipalCollection popPrincipals(Object principal);

  List<PrincipalCollection> getRunAsPrincipalStack(Object principal);

  void removeRunAsStack(Object principal);
}
