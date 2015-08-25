package org.ohdsi.webapi.shiro.Entities;

import javax.persistence.*;
import java.io.Serializable;
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

//    private Set<RoleEntity> roles;

    @Id
    @Column(name = "ID")
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

//    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, targetEntity = RoleEntity.class, fetch = FetchType.EAGER)
//    @JoinTable(name = "SEC_USER_ROLE",
//        joinColumns = {@JoinColumn(name = "USER_ID", nullable = false)},
//        inverseJoinColumns = {@JoinColumn(name = "ROLE_ID", nullable = true)})
//    public Set<RoleEntity> getRoles() {
//        return roles;
//    }
//
//    public void setRoles(Set<RoleEntity> roles) {
//        this.roles = roles;
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity)) return false;

        UserEntity that = (UserEntity) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
