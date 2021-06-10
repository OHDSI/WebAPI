package org.ohdsi.webapi.shiro.Entities;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * Created by GMalikov on 24.08.2015.
 */

@Entity(name = "PermissionEntity")
@Table(name = "SEC_PERMISSION")
public class PermissionEntity implements Serializable {

  private static final long serialVersionUID = 1810877985769153135L;

  @Id
  @Column(name = "ID")
  @GenericGenerator(
      name = "sec_permission_generator",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
          @Parameter(name = "sequence_name", value = "sec_permission_id_seq"),
          @Parameter(name = "initial_value", value = "1000"),
          @Parameter(name = "increment_size", value = "1")
      }
  )
  @GeneratedValue(generator = "sec_permission_generator")
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
