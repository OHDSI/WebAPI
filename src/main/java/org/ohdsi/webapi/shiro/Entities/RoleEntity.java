package org.ohdsi.webapi.shiro.Entities;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * Created by GMalikov on 24.08.2015.
 */

@Entity(name = "RoleEntity")
@Table(name = "SEC_ROLE")
public class RoleEntity implements Serializable{

  private static final long serialVersionUID = 6257846375334314942L;

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "SEC_ROLE_SEQUENCE_GENERATOR", sequenceName = "SEC_ROLE_SEQUENCE", allocationSize = 1, initialValue = 1000)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEC_ROLE_SEQUENCE_GENERATOR")
  private Long id;

  @Column(name = "NAME")
  private String name;

  @OneToMany(mappedBy = "role", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private Set<RolePermissionEntity> rolePermissions = new LinkedHashSet<>();

  @OneToMany(mappedBy = "role", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private Set<UserRoleEntity> userRoles = new LinkedHashSet<>();

  @Column(name = "system_role")
  private Boolean systemRole;
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<RolePermissionEntity> getRolePermissions() {
    return rolePermissions;
  }

  public void setRolePermissions(Set<RolePermissionEntity> rolePermissions) {
    this.rolePermissions = rolePermissions;
  }

  public Set<UserRoleEntity> getUserRoles() {
    return userRoles;
  }

  public void setUserRoles(Set<UserRoleEntity> userRoles) {
    this.userRoles = userRoles;
  }

  public Boolean isSystemRole() {
    return systemRole;
  }

  public void setSystemRole(Boolean system) {
    systemRole = system;
  }
}
