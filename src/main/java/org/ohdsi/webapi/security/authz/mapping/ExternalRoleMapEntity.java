package org.ohdsi.webapi.security.authz.mapping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.ohdsi.webapi.security.authc.UserOrigin;
import org.ohdsi.webapi.security.authz.RoleEntity;

/**
 * JPA entity representing a mapping between an external identity (LDAP group, OIDC claim, etc.)
 * and a WebAPI role. Used during authentication to automatically assign roles to users based on
 * their external identities.
 *
 * Example: LDAP group "cn=admins,ou=groups,..." maps to WebAPI role "admin"
 */
@Entity
@Table(name = "sec_external_role_map")
public class ExternalRoleMapEntity {

  @Id
  @Column(name = "id")
  @SequenceGenerator(name = "sec_external_role_map_seq", sequenceName = "sec_external_role_map_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sec_external_role_map_seq")
  private Integer id;

  @Column(name = "origin", nullable = false, length = 32)
  @Enumerated(EnumType.STRING)
  private UserOrigin origin;

  @Column(name = "external_claim", nullable = false, length = 255)
  private String externalClaim;

  @ManyToOne
  @JoinColumn(name = "role_id", nullable = false)
  private RoleEntity role;

  @Column(name = "description", length = 500)
  private String description;

  public ExternalRoleMapEntity() {
  }

  public ExternalRoleMapEntity(UserOrigin origin, String externalClaim, RoleEntity role) {
    this.origin = origin;
    this.externalClaim = externalClaim;
    this.role = role;
  }

  public ExternalRoleMapEntity(UserOrigin origin, String externalClaim, RoleEntity role, String description) {
    this.origin = origin;
    this.externalClaim = externalClaim;
    this.role = role;
    this.description = description;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public UserOrigin getOrigin() {
    return origin;
  }

  public void setOrigin(UserOrigin origin) {
    this.origin = origin;
  }

  public String getExternalClaim() {
    return externalClaim;
  }

  public void setExternalClaim(String externalClaim) {
    this.externalClaim = externalClaim;
  }

  public RoleEntity getRole() {
    return role;
  }

  public void setRole(RoleEntity role) {
    this.role = role;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
