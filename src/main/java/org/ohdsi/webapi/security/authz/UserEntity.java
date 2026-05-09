package org.ohdsi.webapi.security.authz;

import org.ohdsi.webapi.security.authc.UserOrigin;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by GMalikov on 24.08.2015.
 */

@Entity(name = "User")
@Table(name = "SEC_USER")
public class UserEntity implements Serializable{

  private static final long serialVersionUID = -2697485161468660016L;

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "sec_user_seq", sequenceName = "sec_user_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sec_user_seq")
  private Long id;

  @Column(name = "LOGIN")
  private String login;

  @Column(name = "NAME")
  private String name;

  @Column(name = "origin", nullable = false)
  @Enumerated(EnumType.STRING)
  private UserOrigin origin = UserOrigin.SYSTEM;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private Set<UserRoleEntity> userRoles = new LinkedHashSet<>();

  @Column(name = "last_viewed_notifications_time")
  private Date lastViewedNotificationsTime;

  public Long getId() {
      return id;
  }

  public void setId(Long id) {
      this.id = id;
  }

  public String getLogin() {
      return login;
  }

  public void setLogin(String login) {
      this.login = login;
  }

  public String getName() {
      return name;
  }

  public void setName(String name) {
      this.name = name;
  }

  public Set<UserRoleEntity> getUserRoles() {
    return userRoles;
  }

  public void setUserRoles(Set<UserRoleEntity> userRoles) {
    this.userRoles = userRoles;
  }

  public Date getLastViewedNotificationsTime() {
    return lastViewedNotificationsTime;
  }

  public void setLastViewedNotificationsTime(Date lastViewedNotificationsTime) {
    this.lastViewedNotificationsTime = lastViewedNotificationsTime;
  }

  public UserOrigin getOrigin() {
    return origin;
  }

  public void setOrigin(UserOrigin origin) {
    this.origin = origin;
  }
}
