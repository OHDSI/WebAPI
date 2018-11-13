package org.ohdsi.webapi.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.collections.impl.block.factory.Comparators;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;

import java.util.Comparator;

public class Role implements Comparable<Role> {
  @JsonProperty("id")
  public Long id;
  @JsonProperty("role")
  public String role;
  @JsonProperty("defaultImported")
  public boolean defaultImported;
  @JsonProperty("systemRole")
  public boolean systemRole;

  public Role() {}

  public Role(String role) {
    this.role = role;
  }

  public Role(RoleEntity roleEntity) {
    this.id = roleEntity.getId();
    this.role = roleEntity.getName();
    this.systemRole = roleEntity.isSystemRole();
  }

  public Role(RoleEntity roleEntity, boolean defaultImported) {
    this(roleEntity);
    this.defaultImported = defaultImported;
  }

  @Override
  public int compareTo(Role o) {
    Comparator c = Comparators.naturalOrder();
    if (this.id == null && o.id == null)
      return c.compare(this.role, o.role);
    else
      return c.compare(this.id, o.id);
  }

}
