package org.ohdsi.webapi.user.importer.model;

import com.odysseusinc.scheduler.model.ArachneJob;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.util.List;

@Entity
@Table(name = "user_import_job")
@SequenceGenerator(name = "arachne_job_generator", sequenceName = "user_import_job_seq", allocationSize = 1)
public class UserImportJob extends ArachneJob {

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_import_job_weekdays", joinColumns = @JoinColumn(name = "user_import_job_id"))
  @Column(name = "day_of_week")
  @Enumerated(EnumType.STRING)
  private List<DayOfWeek> weekDays;

  @Column(name = "provider_type")
  @Enumerated(EnumType.STRING)
  private LdapProviderType providerType;

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
}
