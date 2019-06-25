package org.ohdsi.webapi.user.importer.model;

import org.ohdsi.webapi.user.importer.repository.LdapProviderTypeConverter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user_import_job_history")
public class UserImportJobHistoryItem {

  @Id
  @Column
  private Long id;

  @Column(name = "start_time")
  @Temporal(TemporalType.TIMESTAMP)
  private Date startTime;

  @Column(name = "end_time")
  @Temporal(TemporalType.TIMESTAMP)
  private Date endTime;

  @Column
  private String status;

  @Column(name = "exit_code")
  private String exitCode;

  @Column(name = "exit_message")
  private String exitMessage;

  @Column(name = "author")
  private String author;

  @Column(name = "provider_type")
  @Convert(converter = LdapProviderTypeConverter.class)
  private LdapProviderType providerType;

  @Column(name = "job_name")
  private String jobName;

  public Long getId() {
    return id;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public String getStatus() {
    return status;
  }

  public String getExitMessage() {
    return exitMessage;
  }

  public String getAuthor() {
    return author;
  }

  public LdapProviderType getProviderType() {
    return providerType;
  }

  public String getJobName() {
    return jobName;
  }

  public String getExitCode() {
    return exitCode;
  }
}
