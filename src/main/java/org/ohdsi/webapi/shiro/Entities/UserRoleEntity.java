package org.ohdsi.webapi.shiro.Entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Entity(name = "UserRoleEntity")
@Table(name = "SEC_USER_ROLE")
public class UserRoleEntity implements Serializable {

  private static final long serialVersionUID = 6257846375334314942L;

  private Long id;
  private String status;
  private UserEntity user;
  private RoleEntity role;
  private UserOrigin origin = UserOrigin.SYSTEM;

  @Id
  @Column(name = "ID")
  @GenericGenerator(
    name = "sec_user_role_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
      @Parameter(name = "sequence_name", value = "sec_user_role_sequence"),
      @Parameter(name = "initial_value", value = "1000"),
      @Parameter(name = "increment_size", value = "1")
    }
  )
  @GeneratedValue(generator = "sec_user_role_generator")
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

  @Column(name = "origin", nullable = false)
  @Enumerated(EnumType.STRING)
  public UserOrigin getOrigin() {
    return origin;
  }

  public void setOrigin(UserOrigin origin) {
    this.origin = origin;
  }
}
