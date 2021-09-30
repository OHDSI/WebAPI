/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.conceptset;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import org.ohdsi.webapi.GenerationStatus;

/**
 *
 * @author Anthony Sena <https://github.com/ohdsi>
 */

@Entity(name = "ConceptSetGenerationInfo")
@Table(name = "concept_set_generation_info")
@IdClass(ConceptSetGenerationInfoKey.class)
public class ConceptSetGenerationInfo implements Serializable {

    private static long serialVersionUID = 1L;

    public ConceptSetGenerationInfo() {
    }

    public ConceptSetGenerationInfo(ConceptSet conceptSet, Integer sourceId, Integer generationType) {

    }

    @Id
    @Column(name = "concept_set_id")
    private Integer conceptSetId;

    @Id
    @Column(name = "source_id")
    private Integer sourceId;
    
    @Column(name = "generation_type")
    private ConceptSetGenerationType generationType;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "execution_duration")
    private Integer executionDuration;

    @Column(name = "status")
    private GenerationStatus status;

    @Column(name = "is_valid")
    private boolean isValid;

    @Column(name = "is_canceled")
    private boolean isCanceled;
		
		@Lob
		@Type(type = "org.hibernate.type.TextType")  
		private String params;
		

    /**
     * @return the conceptSetId
     */
    public Integer getConceptSetId() {
        return conceptSetId;
    }

    /**
     * @return the executionDuration
     */
    public Integer getExecutionDuration() {
        return executionDuration;
    }

    /**
     * @return the sourceId
     */
    public Integer getSourceId() {
        return sourceId;
    }

    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @return the status
     */
    public GenerationStatus getStatus() {
        return status;
    }

    /**
     * @return the isValid
     */
    public boolean isIsValid() {
        return isValid;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }

    /**
     * @param conceptSetId the conceptSetId to set
     */
    public void setConceptSetId(Integer conceptSetId) {
        this.conceptSetId = conceptSetId;
    }

    /**
     * @param executionDuration the executionDuration to set
     */
    public void setExecutionDuration(Integer executionDuration) {
        this.executionDuration = executionDuration;
    }

    /**
     * @param isValid the isValid to set
     */
    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * @param sourceId the sourceId to set
     */
    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(GenerationStatus status) {
        this.status = status;
    }

    /**
     * @return the generationType
     */
    public ConceptSetGenerationType getGenerationType() {
        return generationType;
    }

    /**
     * @param generationType the generationType to set
     */
    public void setGenerationType(ConceptSetGenerationType generationType) {
        this.generationType = generationType;
    }

	/**
	 * @return the params
	 */
	public String getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(String params) {
		this.params = params;
	}
}
