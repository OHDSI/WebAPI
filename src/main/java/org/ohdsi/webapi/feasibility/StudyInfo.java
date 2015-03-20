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
package org.ohdsi.webapi.feasibility;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.GenerationStatus;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Entity(name = "StudyInfo")
@Table(name="feas_study_generation_info")
public class StudyInfo implements Serializable {
 private static final long serialVersionUID = 1L;

  @Id
  private Integer id;
  
  @MapsId
  @OneToOne
  @JoinColumn(name="study_id")
  private FeasibilityStudy study;

  @Column(name="start_time")
  private Date startTime;  
  
  @Column(name="execution_duration")
  private Integer executionDuration;    
  
  @Column(name="status")
  private GenerationStatus status;    
  
  @Column(name="is_valid")
  private boolean isValid;
  
  public StudyInfo(){
    super(); 
  }
  
  public StudyInfo(FeasibilityStudy study)
  {
    this();
    this.study = study;
  }
  
  public Integer getId() {
    return id;
  }

  public StudyInfo setId(Integer id) {
    this.id = id;
    return this;
  }

  public Date getStartTime() {
    return startTime;
  }

  public StudyInfo setStartTime(Date startTime) {
    this.startTime = startTime;
    return this;
  }

  public Integer getExecutionDuration() {
    return executionDuration;
  }

  public StudyInfo setExecutionDuration(Integer executionDuration) {
    this.executionDuration = executionDuration;
    return this;
  }

  public GenerationStatus getStatus() {
    return status;
  }

  public StudyInfo setStatus(GenerationStatus status) {
    this.status = status;
    return this;
  }

  public boolean isIsValid() {
    return isValid;
  }

  public StudyInfo setIsValid(boolean isValid) {
    this.isValid = isValid;
    return this;
  }
}
