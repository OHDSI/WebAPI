package org.ohdsi.webapi.user.importer.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name="user_import_id")
  private UserImportJob userImport;

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

  public UserImportJob getUserImport() {
    return userImport;
  }

  public String getJobName() {
return jobName;
  }

  public String getExitCode() {
    return exitCode;
  }
}
