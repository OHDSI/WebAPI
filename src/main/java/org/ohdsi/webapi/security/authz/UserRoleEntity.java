package org.ohdsi.webapi.security.authz;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.GenerationType;
import org.ohdsi.webapi.security.authc.UserOrigin;

/**
 *
 * @author gennadiy.anisimov
 */
@Entity(name = "UserRole")
@Table(name = "SEC_USER_ROLE")
public class UserRoleEntity implements Serializable {

  private static final long serialVersionUID = 6257846375334314942L;

  private Long id;
  private UserEntity user;
  private RoleEntity role;
  private UserOrigin origin = UserOrigin.SYSTEM;

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "sec_user_role_seq", sequenceName = "sec_user_role_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sec_user_role_seq")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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
