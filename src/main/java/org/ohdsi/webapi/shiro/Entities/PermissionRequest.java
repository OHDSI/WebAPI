package org.ohdsi.webapi.shiro.Entities;

import java.io.Serializable;

/**
 *
 * @author gennadiy.anisimov
 */
public class PermissionRequest implements Serializable{

  private static final long serialVersionUID = -2697485161468660016L;

  private Long id;
  private String role;
  private String permission;
  private String description;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPermission() {
    return permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
