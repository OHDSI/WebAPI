/**
 * This file was generated by the Jeddict
 */ 

package org.ohdsi.webapi.cohortanalysis;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cknoll1
 */

@Entity(name="CohortAnalysisGenerationInfo")
@Table(name="cohort_analysis_gen_info")
@IdClass(CohortAnalysisGenerationInfoPK.class)
public class CohortAnalysisGenerationInfo { 

    @Column(name="source_id")
    @Id
    private Integer sourceId;

		@Id
		@ManyToOne(targetEntity = CohortDefinition.class)
    @JoinColumn(name="cohort_id",referencedColumnName="id")
    private CohortDefinition cohortDefinition;

    @Column(name="last_execution")
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastExecution;

    @Column(name="execution_duration")
    @Basic
    private Integer executionDuration;

    @Column(name="fail_message")
    @Basic
    private String failMessage;

    @Column(name = "progress")
    private Integer progress;

		@Column(name = "analysis_id")
		@ElementCollection(fetch = FetchType.LAZY)
		@CollectionTable(name = "cohort_analysis_list_xref", joinColumns = {
			@JoinColumn(name = "cohort_id", referencedColumnName = "cohort_id")
			,@JoinColumn(name = "source_id", referencedColumnName = "source_id")})
	  private Set<Integer> analysisIds = new HashSet<>();

    public Integer getSourceId() {
        return this.sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }


    public Date getLastExecution() {
        return this.lastExecution;
    }

    public void setLastExecution(Date lastExecution) {
        this.lastExecution = lastExecution;
    }


    public Integer getExecutionDuration() {
        return this.executionDuration;
    }

    public void setExecutionDuration(Integer executionDuration) {
        this.executionDuration = executionDuration;
    }


    public String getFailMessage() {
        return this.failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }


    public Set<Integer> getAnalysisIds() {
        return this.analysisIds;
    }

    public void setAnalysisIds(Set<Integer> analysisIds) {
        this.analysisIds = analysisIds;
    }


    public CohortDefinition getCohortDefinition() {
        return this.cohortDefinition;
    }

    public void setCohortDefinition(CohortDefinition cohortDefinition) {
        this.cohortDefinition = cohortDefinition;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }
}
