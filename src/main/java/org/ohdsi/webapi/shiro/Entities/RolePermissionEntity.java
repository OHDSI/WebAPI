package org.ohdsi.webapi.shiro.Entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 *
 * @author gennadiy.anisimov
 */
@Entity(name = "RolePermissionEntity")
@Table(name = "SEC_ROLE_PERMISSION")
public class RolePermissionEntity implements Serializable {
  private static final long serialVersionUID = 6257846375334314942L;

  @Id
  @Column(name = "ID")
  @GenericGenerator(
    name = "sec_role_permission_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
      @Parameter(name = "sequence_name", value = "sec_role_permission_sequence"),
      @Parameter(name = "initial_value", value = "1000"),
      @Parameter(name = "increment_size", value = "1")
    }
  )
  @GeneratedValue(generator = "sec_role_permission_generator")
  private Long id;

  @Column(name = "STATUS")
  private String status;

  @ManyToOne
  @JoinColumn(name="ROLE_ID", nullable=false)
  private RoleEntity role;

  @ManyToOne
  @JoinColumn(name="PERMISSION_ID", nullable=false)
  private PermissionEntity permission;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public RoleEntity getRole() {
    return role;
  }

  public void setRole(RoleEntity role) {
    this.role = role;
  }

  public PermissionEntity getPermission() {
    return permission;
  }

  public void setPermission(PermissionEntity permission) {
    this.permission = permission;
  }

}
