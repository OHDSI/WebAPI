/*
 * Copyright 2015 Observational Health Data Sciences and Informatics [OHDSI.org].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.cohortdefinition;

import org.ohdsi.webapi.GenerationStatus;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Entity(name = "CohortGenerationInfo")
@Table(name="cohort_generation_info")
public class CohortGenerationInfo implements Serializable {
  private static final long serialVersionUID = 1L;

  public  CohortGenerationInfo()
  {    
  }

  public  CohortGenerationInfo(CohortDefinition definition, Integer sourceId)
  {    
    this.id = new CohortGenerationInfoId(definition.getId(), sourceId);
    this.cohortDefinition = definition;
  }

  @EmbeddedId
  private CohortGenerationInfoId id;
  
  @ManyToOne
  @MapsId("cohortDefinitionId")
  @JoinColumn(name="id", referencedColumnName="id")
  private CohortDefinition cohortDefinition;
  
  @Column(name="start_time")
  private Date startTime;  
  
  @Column(name="execution_duration")
  private Integer executionDuration;    
  
  @Column(name="status")
  private GenerationStatus status;    
  
  @Column(name="is_valid")
  private boolean isValid;
	
  @Column(name="include_features", nullable = true)
  private boolean includeFeatures = false;
	
  @Column(name="fail_message")
  private String failMessage;
	
  @Column(name="person_count")
  private Long personCount;
	
  @Column(name="record_count")
  private Long recordCount;

  public CohortGenerationInfoId getId() {
    return id;
  }
  
  public Date getStartTime() {
    return startTime;
  }

  public CohortGenerationInfo setStartTime(Date startTime) {
    this.startTime = startTime;
    return this;
  }

  public Integer getExecutionDuration() {
    return executionDuration;
  }

  public CohortGenerationInfo setExecutionDuration(Integer executionDuration) {
    this.executionDuration = executionDuration;
    return this;
  }

  public GenerationStatus getStatus() {
    return status;
  }

  public CohortGenerationInfo setStatus(GenerationStatus status) {
    this.status = status;
    return this;
  }

  public boolean isIsValid() {
    return isValid;
  }

  public CohortGenerationInfo setIsValid(boolean isValid) {
    this.isValid = isValid;
    return this;
  }

	public boolean isIncludeFeatures() {
		return includeFeatures;
	}

	public CohortGenerationInfo setIncludeFeatures(boolean includeFeatures) {
		this.includeFeatures = includeFeatures;
		return this;
	}

	public String getFailMessage() {
		return failMessage;
	}

	public CohortGenerationInfo setFailMessage(String failMessage) {
		this.failMessage = failMessage;
		return this;
	}

	public Long getPersonCount() {
		return personCount;
	}

	public CohortGenerationInfo setPersonCount(Long personCount) {
		this.personCount = personCount;
		return this;
	}

	public Long getRecordCount() {
		return recordCount;
	}

	public CohortGenerationInfo setRecordCount(Long recordCount) {
		this.recordCount = recordCount;
		return this;
	}
	
}
