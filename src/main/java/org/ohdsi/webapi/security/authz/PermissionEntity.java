package org.ohdsi.webapi.security.authz;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.GenerationType;

/**
 * Created by GMalikov on 24.08.2015.
 */

@Entity(name = "Permission")
@Table(name = "SEC_PERMISSION")
public class PermissionEntity implements Serializable {

  private static final long serialVersionUID = 1810877985769153135L;

    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = "sec_permission_seq", sequenceName = "sec_permission_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sec_permission_seq")
    private Long id;

  @Column(name = "VALUE")
  private String value;

  @Column(name = "DESCRIPTION")
  private String description;

  @OneToMany(mappedBy = "permission", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private Set<RolePermissionEntity> rolePermissions = new LinkedHashSet<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Set<RolePermissionEntity> getRolePermissions() {
    return rolePermissions;
  }

  public void setRolePermissions(Set<RolePermissionEntity> rolePermissions) {
    this.rolePermissions = rolePermissions;
  }
}
