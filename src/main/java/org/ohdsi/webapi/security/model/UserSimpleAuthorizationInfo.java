package org.ohdsi.webapi.security.model;

import java.util.List;
import java.util.Map;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;

public class UserSimpleAuthorizationInfo extends SimpleAuthorizationInfo {

  private Long userId;
  private String login;
  private Map<String,List<Permission>> permissionIdx;

  
  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }
  
  public Map<String, List<Permission>> getPermissionIdx() {
    return permissionIdx;
  }

  public void setPermissionIdx(Map<String, List<Permission>> permissionIdx) {
    this.permissionIdx = permissionIdx;
  }

}
