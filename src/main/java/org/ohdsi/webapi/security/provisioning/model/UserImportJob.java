package org.ohdsi.webapi.security.provisioning.model;

import org.ohdsi.webapi.arachne.scheduler.model.ArachneJob;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.util.List;

@Entity
@Table(name = "user_import_job")
@GenericGenerator(
  name = "arachne_job_generator",
  strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
  parameters = {
    @Parameter(name = "sequence_name", value = "user_import_job_seq"),
    @Parameter(name = "increment_size", value = "1")
  }
)
@NamedEntityGraph(name = "jobWithMapping",
  attributeNodes = @NamedAttributeNode("groupRoleImportMapping"))
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
  private List<GroupRoleImportEntity> groupRoleImportMapping;

  @Column(name = "preserve_roles")
  private Boolean preserveRoles;

  @Column(name = "user_roles")
  private String userRoles;

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

  public List<GroupRoleImportEntity> getRoleGroupMapping() {
    return groupRoleImportMapping;
  }

  public void setRoleGroupMapping(List<GroupRoleImportEntity> groupRoleImportMapping) {
    this.groupRoleImportMapping = groupRoleImportMapping;
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
}
