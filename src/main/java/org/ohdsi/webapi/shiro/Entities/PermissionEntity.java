package org.ohdsi.webapi.shiro.Entities;

import java.io.Serializable;
import java.util.HashSet;
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
  private Set<RolePermissionEntity> rolePermissions = new HashSet<>(0);


  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy=GenerationType.SEQUENCE)
  @SequenceGenerator(
          name="sec_permission_sequence", 
          sequenceName="sec_permission_sequence"
  )
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

  @OneToMany(mappedBy = "permission", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
  public Set<RolePermissionEntity> getRolePermissions() {
    return rolePermissions;
  }

  public void setRolePermissions(Set<RolePermissionEntity> rolePermissions) {
    this.rolePermissions = rolePermissions;
  }
}
