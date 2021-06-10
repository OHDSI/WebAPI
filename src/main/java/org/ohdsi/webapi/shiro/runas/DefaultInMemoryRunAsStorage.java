package org.ohdsi.webapi.shiro.runas;

import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultInMemoryRunAsStorage implements RunAsStorage {

  private final Map<Object, List<PrincipalCollection>> principalsMap = new ConcurrentHashMap<>();

  @Override
  public void pushPrincipals(Object principal, PrincipalCollection principals) {

    if (Objects.isNull(principals) || principals.isEmpty()) {
      throw new IllegalArgumentException("Specified Subject principals cannot be null or empty for 'run as' functionality.");
    }
    List<PrincipalCollection> stack = getRunAsPrincipalStack(principal);
    if (Objects.isNull(stack)) {
      stack = new CopyOnWriteArrayList<>();
      principalsMap.put(principal, stack);
    }
    stack.add(0, principals);
  }

  @Override
  public PrincipalCollection popPrincipals(Object principal) {

    PrincipalCollection popped = null;

    List<PrincipalCollection> stack = getRunAsPrincipalStack(principal);
    if (!Objects.isNull(stack) && !stack.isEmpty()) {
      popped = stack.remove(0);
      if (stack.isEmpty()) {
        removeRunAsStack(principal);
      }
    }

    return popped;
  }

  @Override
  public List<PrincipalCollection> getRunAsPrincipalStack(Object principal) {

    if (Objects.isNull(principal)) {
      throw new IllegalArgumentException("Token should not be null value");
    }
    return principalsMap.get(principal);
  }

  @Override
  public void removeRunAsStack(Object principal) {

    principalsMap.remove(principal);
  }
}
