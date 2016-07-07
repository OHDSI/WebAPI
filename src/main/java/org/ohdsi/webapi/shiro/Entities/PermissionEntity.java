package org.ohdsi.webapi.shiro.Entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by GMalikov on 24.08.2015.
 */

@Entity(name = "PermissionEntity")
@Table(name = "SEC_PERMISSION")
public class PermissionEntity implements Serializable {

    private static final long serialVersionUID = 1810877985769153135L;
    private Long id;
    private String value;
    private RoleEntity role;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    @SequenceGenerator(
            name="sec_permission_sequence", 
            sequenceName="sec_permission_sequence"
    )
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "VALUE")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, targetEntity = RoleEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE_ID")
    public RoleEntity getRole() {
        return role;
    }

    public void setRole(RoleEntity role) {
        this.role = role;
    }
}
