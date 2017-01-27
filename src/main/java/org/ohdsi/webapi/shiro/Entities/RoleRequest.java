package org.ohdsi.webapi.shiro.Entities;

import java.io.Serializable;

/**
 *
 * @author gennadiy.anisimov
 */
public class RoleRequest implements Serializable {

  private static final long serialVersionUID = -2697485161468660016L;

  private Long id;
  private String user;
  private String role;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
