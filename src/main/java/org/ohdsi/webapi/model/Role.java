package org.ohdsi.webapi.model;

import org.eclipse.collections.impl.block.factory.Comparators;
import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;

import java.util.Comparator;

public class Role implements Comparable<Role> {
  public Long id;
  public String role;
  public boolean defaultImported;

  public Role() {}

  public Role(String role) {
    this.role = role;
  }

  public Role(RoleEntity roleEntity) {
    this.id = roleEntity.getId();
    this.role = roleEntity.getName();
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
