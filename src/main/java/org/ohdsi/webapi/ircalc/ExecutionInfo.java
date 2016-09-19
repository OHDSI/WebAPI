/*
 * Copyright 2016 Observational Health Data Sciences and Informatics [OHDSI.org].
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
package org.ohdsi.webapi.ircalc;

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
@Entity(name = "IRAnalysisGenerationInfo")
@Table(name="ir_execution")
public class ExecutionInfo implements Serializable {
 private static final long serialVersionUID = 1L;

  @EmbeddedId
  private ExecutionInfoId id;
  
  @JsonIgnore
  @ManyToOne
  @MapsId("analysisId")
  @JoinColumn(name="analysis_id", referencedColumnName="id")
  private IncidenceRateAnalysis analysis;

  @JsonIgnore
  @ManyToOne
  @MapsId("sourceId")
  @JoinColumn(name="source_id", referencedColumnName="source_id")
  private Source source;
  
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd, HH:mm")
  @Column(name="start_time")
  private Date startTime;  
  
  @Column(name="execution_duration")
  private Integer executionDuration;    
  
  @Column(name="status")
  private GenerationStatus status;    
  
  @Column(name="is_valid")
  private boolean isValid;
  
  @Column(name="message")
  private String message;
  
  public  ExecutionInfo()
  {    
  }

  public  ExecutionInfo(IncidenceRateAnalysis analysis, Source source)
  {    
    this.id = new ExecutionInfoId(analysis.getId(), source.getSourceId());
    this.source = source;
    this.analysis = analysis;
  }

  public ExecutionInfoId getId() {
    return id;
  }

  public void setId(ExecutionInfoId id) {
    this.id = id;
  }

  public Date getStartTime() {
    return startTime;
  }

  public ExecutionInfo setStartTime(Date startTime) {
    this.startTime = startTime;
    return this;
  }

  public Integer getExecutionDuration() {
    return executionDuration;
  }

  public ExecutionInfo setExecutionDuration(Integer executionDuration) {
    this.executionDuration = executionDuration;
    return this;
  }

  public GenerationStatus getStatus() {
    return status;
  }

  public ExecutionInfo setStatus(GenerationStatus status) {
    this.status = status;
    return this;
  }

  public boolean getIsValid() {
    return isValid;
  }

  public ExecutionInfo setIsValid(boolean isValid) {
    this.isValid = isValid;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public ExecutionInfo setMessage(String message) {
    this.message = message;
    return this;
  }

  public IncidenceRateAnalysis getAnalysis() {
    return analysis;
  }

  public Source getSource() {
    return source;
  }
}
