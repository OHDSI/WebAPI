package org.ohdsi.webapi.user.importer.model;

import com.odysseusinc.scheduler.model.ArachneJob;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.util.List;

@Entity
@Table(name = "user_import_job")
@SequenceGenerator(name = "arachne_job_generator", sequenceName = "user_import_job_seq", allocationSize = 1)
@NamedEntityGraph(name = "jobWithMapping",
  attributeNodes = @NamedAttributeNode("roleGroupMapping"))
public class UserImportJob extends ArachneJob {

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_import_job_weekdays", joinColumns = @JoinColumn(name = "user_import_job_id"))
  @Column(name = "day_of_week")
  @Enumerated(EnumType.STRING)
  private List<DayOfWeek> weekDays;

  @Column(name = "provider_type")
  @Enumerated(EnumType.STRING)
  private LdapProviderType providerType;

  @OneToMany(mappedBy = "userImportJob")
  private List<RoleGroupEntity> roleGroupMapping;

  @Column(name = "preserve_roles")
  private Boolean preserveRoles;

  @Override
  public List<DayOfWeek> getWeekDays() {

    return weekDays;
  }

  @Override
  public void setWeekDays(List<DayOfWeek> weekDays) {
    this.weekDays = weekDays;
  }

  public LdapProviderType getProviderType() {
    return providerType;
  }

  public void setProviderType(LdapProviderType providerType) {
    this.providerType = providerType;
  }

  public List<RoleGroupEntity> getRoleGroupMapping() {
    return roleGroupMapping;
  }

  public void setRoleGroupMapping(List<RoleGroupEntity> roleGroupMapping) {
    this.roleGroupMapping = roleGroupMapping;
  }

  public Boolean getPreserveRoles() {
    return preserveRoles;
  }

  public void setPreserveRoles(Boolean preserveRoles) {
    this.preserveRoles = preserveRoles;
  }
}
