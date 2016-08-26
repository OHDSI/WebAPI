package org.ohdsi.webapi.shiro.Entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author gennadiy.anisimov
 */
@Entity(name = "UserRoleEntity")
@Table(name = "SEC_USER_ROLE")
public class UserRoleEntity implements Serializable {

  private static final long serialVersionUID = 6257846375334314942L;

  private Long id;
  private String status;
  private UserEntity user;
  private RoleEntity role;

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "SEC_USER_ROLE_SEQUENCE_GENERATOR", sequenceName = "SEC_USER_ROLE_SEQUENCE", allocationSize = 1, initialValue = 1000)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEC_USER_ROLE_SEQUENCE_GENERATOR")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column(name = "STATUS")
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @ManyToOne
  @JoinColumn(name="USER_ID", nullable=false)
  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  @ManyToOne
  @JoinColumn(name="ROLE_ID", nullable=false)
  public RoleEntity getRole() {
    return role;
  }

  public void setRole(RoleEntity role) {
    this.role = role;
  }
}
