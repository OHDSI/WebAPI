package org.ohdsi.webapi.executionengine.entity;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.source.Source;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "ee_analysis_status")
public class ExecutionEngineAnalysisStatus {

    public enum Status {
        PENDING, STARTED, RUNNING, COMPLETED, FAILED
    };

    @Id
    @GenericGenerator(
        name = "analysis_execution_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "analysis_execution_sequence"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "analysis_execution_generator")
    @Column(name = "id")
    private Integer id;
    @Column(name = "executionStatus")
    @Enumerated(EnumType.STRING)
    private Status executionStatus;
    @Column(name = "update_password")
    private String updatePassword;
    @ManyToOne(targetEntity = Source.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;
    @Column(name = "job_execution_id")
    private Long jobExecutionId;
    @OneToMany(mappedBy = "execution", targetEntity = AnalysisResultFile.class, fetch = FetchType.LAZY)
    private List<AnalysisResultFile> resultFiles;

    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public Status getExecutionStatus() {

        return executionStatus;
    }

    public void setExecutionStatus(Status executionStatus) {

        this.executionStatus = executionStatus;
    }

    public String getUpdatePassword() {

        return updatePassword;
    }

    public void setUpdatePassword(String updatePassword) {

        this.updatePassword = updatePassword;
    }

    public Source getSource() {
      return source;
    }

    public void setSource(Source source) {
      this.source = source;
    }

    public Long getJobExecutionId() {
          return jobExecutionId;
      }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    public List<AnalysisResultFile> getResultFiles() {
      return resultFiles;
    }

    public void setResultFiles(List<AnalysisResultFile> resultFiles) {
      this.resultFiles = resultFiles;
    }
}
