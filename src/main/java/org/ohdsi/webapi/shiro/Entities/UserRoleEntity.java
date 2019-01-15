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
  @GenericGenerator(
    name = "SEC_USER_ROLE_SEQUENCE_GENERATOR",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
      @Parameter(name = "sequence_name", value = "SEC_USER_ROLE_SEQUENCE"),
      @Parameter(name = "initial_value", value = "1000"),
      @Parameter(name = "increment_size", value = "1")
    }
  )
  @GeneratedValue(generator = "SEC_USER_ROLE_SEQUENCE_GENERATOR")
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
