package org.ohdsi.webapi.shiro.Entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by GMalikov on 24.08.2015.
 */

@Entity(name = "RoleEntity")
@Table(name = "SEC_ROLE")
public class RoleEntity implements Serializable{

    private static final long serialVersionUID = 6257846375334314942L;

    private Long id;
    private String name;
    private Set<PermissionEntity> permissions = new HashSet<>(0);
    private Set<UserEntity> users = new HashSet<>(0);

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    @SequenceGenerator(
            name="sec_role_sequence", 
            sequenceName="sec_role_sequence"
    )
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
    public Set<PermissionEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionEntity> permissions) {
        this.permissions = permissions;
    }
    
    @ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    public Set<UserEntity> getUsers() {
      return users;
    }
    
    public void setUsers(Set<UserEntity> users) {
      this.users = users;
    }
}
