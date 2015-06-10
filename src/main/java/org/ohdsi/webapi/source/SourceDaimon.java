/*
 * Copyright 2015 fdefalco.
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
package org.ohdsi.webapi.source;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author fdefalco
 */
@Entity(name = "SourceDaimon")
@Table(name="source_daimon")
public class SourceDaimon implements Serializable {
  public enum DaimonType { CDM, Vocabulary, Results, Evidence };
  
  public SourceDaimon() {
  
  }
  
  public SourceDaimon(Source source) {
    this.source = source;
  }
  
  @Id
  @GeneratedValue    
  @Column(name="SOURCE_DAIMON_ID")  
  private int sourceDaimonId;
  
  @ManyToOne
  @JsonIgnore
  @JoinColumn(name="SOURCE_ID", referencedColumnName="SOURCE_ID")  
  private Source source;

  @Enumerated(EnumType.ORDINAL)  
  @Column(name="DAIMON_TYPE")  
  private DaimonType daimonType;
  
  @Column(name="TABLE_QUALIFIER")  
  private String tableQualifier;  
  
  @Column(name="PRIORITY")
  private String priority;

  public int getSourceDaimonId() {
    return sourceDaimonId;
  }

  public void setSourceDaimonId(int sourceDaimonId) {
    this.sourceDaimonId = sourceDaimonId;
  }

  public Source getSource() {
    return source;
  }

  public void setSource(Source source) {
    this.source = source;
  }

  public DaimonType getDaimonType() {
    return daimonType;
  }

  public void setDaimonType(DaimonType daimonType) {
    this.daimonType = daimonType;
  }

  public String getTableQualifier() {
    return tableQualifier;
  }

  public void setTableQualifier(String tableQualifier) {
    this.tableQualifier = tableQualifier;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }
  
  
}
