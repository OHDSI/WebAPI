package org.ohdsi.webapi.user.importer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;

@Entity
@Table(name = "sec_role_group")
public class RoleGroupEntity {

  @Id
  @GenericGenerator(
    name = "sec_role_group_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
      @Parameter(name = "sequence_name", value = "sec_role_group_seq"),
      @Parameter(name = "increment_size", value = "1")
    }
  )
  @GeneratedValue(generator = "sec_role_group_generator")
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

  @ManyToOne()
  @JoinColumn(name = "job_id")
  private UserImportJob userImportJob;

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

  public UserImportJob getUserImportJob() {
    return userImportJob;
  }

  public void setUserImportJob(UserImportJob userImportJob) {
    this.userImportJob = userImportJob;
  }
}
