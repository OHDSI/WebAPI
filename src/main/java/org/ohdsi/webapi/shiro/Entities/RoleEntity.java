package org.ohdsi.webapi.shiro.Entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * Created by GMalikov on 24.08.2015.
 */

@Entity(name = "RoleEntity")
@Table(name = "SEC_ROLE")
public class RoleEntity implements Serializable{

  private static final long serialVersionUID = 6257846375334314942L;

  private Long id;
  private String name;
  private Set<RolePermissionEntity> rolePermissions = new HashSet<>(0);
  private Set<UserRoleEntity> userRoles = new HashSet<>(0);

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy=GenerationType.SEQUENCE)
  @SequenceGenerator(
          name="sec_role_sequence", 
          sequenceName="sec_role_sequence"
  )
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column(name = "NAME")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  @OneToMany(mappedBy = "role", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
  public Set<RolePermissionEntity> getRolePermissions() {
    return rolePermissions;
  }

  public void setRolePermissions(Set<RolePermissionEntity> rolePermissions) {
    this.rolePermissions = rolePermissions;
  }

  @OneToMany(mappedBy = "role", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
  public Set<UserRoleEntity> getUserRoles() {
    return userRoles;
  }

  public void setUserRoles(Set<UserRoleEntity> userRoles) {
    this.userRoles = userRoles;
  }
}
