package org.ohdsi.webapi.user.importer.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_import")
public class UserImport {
    @Id
    @GenericGenerator(
            name = "user_import_generator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "user_import_sequence"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
    @GeneratedValue(generator = "user_import_generator")
    @Column(name = "id")
    private int id;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private LdapProviderType provider;

    @Column(name = "preserveRoles")
    private Boolean preserveRoles;

    @Column(name = "userRoles")
    private String userRoles;

    @Column(name = "roleGroupMapping")
    private String roleGroupMapping;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LdapProviderType getProvider() {
        return provider;
    }

    public void setProvider(LdapProviderType provider) {
        this.provider = provider;
    }

    public Boolean getPreserveRoles() {
        return preserveRoles;
    }

    public void setPreserveRoles(Boolean preserveRoles) {
        this.preserveRoles = preserveRoles;
    }

    public String getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(String userRoles) {
        this.userRoles = userRoles;
    }

    public String getRoleGroupMapping() {
        return roleGroupMapping;
    }

    public void setRoleGroupMapping(String roleGroupMapping) {
        this.roleGroupMapping = roleGroupMapping;
    }
}
