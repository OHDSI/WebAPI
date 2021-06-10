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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.source.Source;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Entity(name = "StudyGenerationInfo")
@Table(name="feas_study_generation_info")
public class StudyGenerationInfo implements Serializable {
 private static final long serialVersionUID = 1L;

  @EmbeddedId
  private StudyGenerationInfoId id;
  
  @JsonIgnore
  @ManyToOne
  @MapsId("studyId")
  @JoinColumn(name="study_id", referencedColumnName="id")
  private FeasibilityStudy study;

  @JsonIgnore
  @ManyToOne
  @MapsId("sourceId")
  @JoinColumn(name="source_id", referencedColumnName="source_id")
  private Source source;
  
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
  @Column(name="start_time")
  private Date startTime;  
  
  @Column(name="execution_duration")
  private Integer executionDuration;    
  
  @Column(name="status")
  private GenerationStatus status;    
  
  @Column(name="is_valid")
  private boolean isValid;

  @Column(name = "is_canceled")
  private boolean isCanceled;
  
  public  StudyGenerationInfo()
  {    
  }

  public  StudyGenerationInfo(FeasibilityStudy study, Source source)
  {    
    this.id = new StudyGenerationInfoId(study.getId(), source.getSourceId());
    this.source = source;
    this.study = study;
  }

  public StudyGenerationInfoId getId() {
    return id;
  }

  public void setId(StudyGenerationInfoId id) {
    this.id = id;
  }

  public Date getStartTime() {
    return startTime;
  }

  public StudyGenerationInfo setStartTime(Date startTime) {
    this.startTime = startTime;
    return this;
  }

  public Integer getExecutionDuration() {
    return executionDuration;
  }

  public StudyGenerationInfo setExecutionDuration(Integer executionDuration) {
    this.executionDuration = executionDuration;
    return this;
  }

  public GenerationStatus getStatus() {
    return status;
  }

  public StudyGenerationInfo setStatus(GenerationStatus status) {
    this.status = status;
    return this;
  }

  public boolean isIsValid() {
    return isValid;
  }

  public StudyGenerationInfo setIsValid(boolean isValid) {
    this.isValid = isValid;
    return this;
  }

  public boolean isCanceled() {
    return isCanceled;
  }

  public void setCanceled(boolean canceled) {
    isCanceled = canceled;
  }

  public FeasibilityStudy getStudy() {
    return study;
  }

  public Source getSource() {
    return source;
  }
}
