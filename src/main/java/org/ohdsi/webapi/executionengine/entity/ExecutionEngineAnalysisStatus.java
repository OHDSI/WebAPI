package org.ohdsi.webapi.executionengine.entity;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

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

    @ManyToOne(targetEntity = ExecutionEngineGenerationEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "job_execution_id")
    private ExecutionEngineGenerationEntity executionEngineGeneration;

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

    public ExecutionEngineGenerationEntity getExecutionEngineGeneration() {

        return executionEngineGeneration;
    }

    public void setExecutionEngineGeneration(ExecutionEngineGenerationEntity executionEngineGeneration) {

        this.executionEngineGeneration = executionEngineGeneration;
    }

    public List<AnalysisResultFile> getResultFiles() {
      return resultFiles;
    }

    public void setResultFiles(List<AnalysisResultFile> resultFiles) {
      this.resultFiles = resultFiles;
    }

}
