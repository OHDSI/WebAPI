package org.ohdsi.webapi.user.importer;

import org.ohdsi.webapi.user.importer.model.LdapProviderType;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;

import javax.persistence.*;

@Entity
@Table(name = "sec_role_group")
public class RoleGroupMappingEntity {

  @Id
  @SequenceGenerator(name = "sec_role_group_seq", sequenceName = "sec_role_group_seq", allocationSize = 1)
  @GeneratedValue(generator = "sec_role_group_seq", strategy = GenerationType.SEQUENCE)
  @Column(name = "id")
  private int id;

  @Column(name = "provider")
  @Enumerated(EnumType.STRING)
  private LdapProviderType provider;

  @Column(name = "group_dn")
  private String groupDn;

  @Column(name = "group_name")
  private String groupName;

  @ManyToOne
  @JoinColumn(name = "role_id")
  private RoleEntity role;

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

  public String getGroupDn() {
    return groupDn;
  }

  public void setGroupDn(String groupDn) {
    this.groupDn = groupDn;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public RoleEntity getRole() {
    return role;
  }

  public void setRole(RoleEntity role) {
    this.role = role;
  }
}
