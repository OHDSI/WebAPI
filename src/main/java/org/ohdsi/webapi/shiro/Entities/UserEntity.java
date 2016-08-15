package org.ohdsi.webapi.shiro.Entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

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
  private Set<UserRoleEntity> userRoles = new HashSet<>(0);

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy=GenerationType.SEQUENCE)
  @SequenceGenerator(
          name="sec_user_sequence", 
          sequenceName="sec_user_sequence"
  )
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

  @Column(name = "PASSWORD")
  public String getPassword() {
      return password;
  }

  public void setPassword(String password) {
      this.password = password;
  }

  @Column(name = "SALT")
  public String getSalt() {
      return salt;
  }

  public void setSalt(String salt) {
      this.salt = salt;
  }

  @Column(name = "NAME")
  public String getName() {
      return name;
  }

  public void setName(String name) {
      this.name = name;
  }

  @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
  public Set<UserRoleEntity> getUserRoles() {
    return userRoles;
  }

  public void setUserRoles(Set<UserRoleEntity> userRoles) {
    this.userRoles = userRoles;
  }
}
