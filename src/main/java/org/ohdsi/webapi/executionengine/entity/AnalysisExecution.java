package org.ohdsi.webapi.executionengine.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "analysis_execution")
public class AnalysisExecution {

    public enum Status {
        PENDING, STARTED, RUNNING, COMPLETED, FAILED
    };

    @Id
    @GenericGenerator(
        name = "analysis_execution_pk_sequence",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "analysis_execution_id_seq"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "analysis_execution_pk_sequence")
    @Column(name = "id")
    private Integer id;
    @Column(name = "analysis_id")
    private Integer analysisId;
    @Column(name = "analysis_type")
    @Enumerated(EnumType.STRING)
    private AnalysisExecutionType analysisType;
    @Column(name = "duration")
    private Integer duration;
    @Column(name = "executed")
    private Date executed;
    @Column(name = "sec_user_id")
    private Integer userId;
    @Column(name = "executionStatus")
    @Enumerated(EnumType.STRING)
    private Status executionStatus;
    @Column(name = "update_password")
    private String updatePassword;
    @Column(name = "source_id")
    private Integer sourceId;

    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public Integer getAnalysisId() {

        return analysisId;
    }

    public void setAnalysisId(Integer analysisId) {

        this.analysisId = analysisId;
    }

    public AnalysisExecutionType getAnalysisType() {

        return analysisType;
    }

    public void setAnalysisType(AnalysisExecutionType analysisType) {

        this.analysisType = analysisType;
    }

    public Integer getDuration() {

        return duration;
    }

    public void setDuration(Integer duration) {

        this.duration = duration;
    }

    public Date getExecuted() {

        return executed;
    }

    public void setExecuted(Date executed) {

        this.executed = executed;
    }

    public Integer getUserId() {

        return userId;
    }

    public void setUserId(Integer userId) {

        this.userId = userId;
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

    public Integer getSourceId() {

        return sourceId;
    }

    public void setSourceId(Integer sourceId) {

        this.sourceId = sourceId;
    }
}
