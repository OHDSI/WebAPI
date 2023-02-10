package org.ohdsi.webapi.shiro.Entities;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
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
  private UserOrigin origin = UserOrigin.SYSTEM;
  private Set<UserRoleEntity> userRoles = new LinkedHashSet<>();
  private Date lastViewedNotificationsTime;

  @Id
  @Column(name = "ID")
  @GenericGenerator(
    name = "sec_user_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
      @Parameter(name = "sequence_name", value = "sec_user_sequence"),
      @Parameter(name = "initial_value", value = "1000"),
      @Parameter(name = "increment_size", value = "1")
    }
  )
  @GeneratedValue(generator = "sec_user_generator")
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

  @Column(name = "origin", nullable = false)
  @Enumerated(EnumType.STRING)
  public UserOrigin getOrigin() {
    return origin;
  }

  public void setOrigin(UserOrigin origin) {
    this.origin = origin;
  }
}
