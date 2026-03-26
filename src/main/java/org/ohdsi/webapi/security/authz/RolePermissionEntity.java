package org.ohdsi.webapi.security.authz;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.GenerationType;

/**
 *
 * @author gennadiy.anisimov
 */
@Entity(name = "RolePermission")
@Table(name = "SEC_ROLE_PERMISSION")
public class RolePermissionEntity implements Serializable {
  private static final long serialVersionUID = 6257846375334314942L;

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "sec_role_permission_seq", sequenceName = "sec_role_permission_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sec_role_permission_seq")
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
