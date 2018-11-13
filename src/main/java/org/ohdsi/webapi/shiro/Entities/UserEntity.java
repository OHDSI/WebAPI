package org.ohdsi.webapi.shiro.Entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by GMalikov on 24.08.2015.
 */

@Entity(name = "UserEntity")
@Table(name = "SEC_USER")
public class UserEntity implements Serializable{

  private static final long serialVersionUID = -2697485161468660016L;

  private Long id;
  private String login;
  private String password;
  private String salt;
  private String name;
  private Set<UserRoleEntity> userRoles = new LinkedHashSet<>();
  private Date lastViewedNotificationsTime;

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "SEC_USER_SEQUENCE_GENERATOR", sequenceName = "SEC_USER_SEQUENCE", allocationSize = 1, initialValue = 1000)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEC_USER_SEQUENCE_GENERATOR")
  public Long getId() {
      return id;
  }

  public void setId(Long id) {
      this.id = id;
  }

  @Column(name = "LOGIN")
  public String getLogin() {
      return login;
  }

  public void setLogin(String login) {
      this.login = login;
  }

  @Column(name = "NAME")
  public String getName() {
      return name;
  }

  public void setName(String name) {
      this.name = name;
  }

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  public Set<UserRoleEntity> getUserRoles() {
    return userRoles;
  }

  public void setUserRoles(Set<UserRoleEntity> userRoles) {
    this.userRoles = userRoles;
  }

  @Column(name = "last_viewed_notifications_time")
  public Date getLastViewedNotificationsTime() {
    return lastViewedNotificationsTime;
  }

  public void setLastViewedNotificationsTime(Date lastViewedNotificationsTime) {
    this.lastViewedNotificationsTime = lastViewedNotificationsTime;
  }
}
