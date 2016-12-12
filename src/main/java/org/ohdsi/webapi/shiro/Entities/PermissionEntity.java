package org.ohdsi.webapi.shiro.Entities;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * Created by GMalikov on 24.08.2015.
 */

@Entity(name = "PermissionEntity")
@Table(name = "SEC_PERMISSION")
public class PermissionEntity implements Serializable {

  private static final long serialVersionUID = 1810877985769153135L;
  private Long id;
  private String value;
  private String description;
  private Set<RolePermissionEntity> rolePermissions = new LinkedHashSet<>();


  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "SEC_PERMISSION_SEQUENCE_GENERATOR", sequenceName = "SEC_PERMISSION_SEQUENCE", allocationSize = 1, initialValue = 1000)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEC_PERMISSION_SEQUENCE_GENERATOR")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column(name = "VALUE")
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Column(name = "DESCRIPTION")
  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @OneToMany(mappedBy = "permission", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  public Set<RolePermissionEntity> getRolePermissions() {
    return rolePermissions;
  }

  public void setRolePermissions(Set<RolePermissionEntity> rolePermissions) {
    this.rolePermissions = rolePermissions;
  }
}
