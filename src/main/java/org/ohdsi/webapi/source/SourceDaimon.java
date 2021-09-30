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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author fdefalco
 */
@Entity(name = "SourceDaimon")
@Table(name="source_daimon")
@SQLDelete(sql = "UPDATE {h-schema}source_daimon SET priority = -1 WHERE SOURCE_DAIMON_ID = ?")
//@Where(clause = "priority >= 0")
public class SourceDaimon implements Serializable {
  public enum DaimonType { CDM, Vocabulary, Results, CEM, CEMResults, Temp };
  
  public SourceDaimon() {
  
  }
  
  public SourceDaimon(Source source) {
    this.source = source;
  }
  
  @Id
  @GenericGenerator(
    name = "source_daimon_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
      @Parameter(name = "sequence_name", value = "source_daimon_sequence"),
      @Parameter(name = "increment_size", value = "1")
    }
  )
  @GeneratedValue(generator = "source_daimon_generator")
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
  private Integer priority;

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

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof SourceDaimon)) return false;
        SourceDaimon that = (SourceDaimon) o;
        return Objects.equals(getSource(), that.getSource()) &&
                Objects.equals(getDaimonType(), that.getDaimonType());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getSource(), getDaimonType());
    }

    @Override
    public String toString(){
        return String.format("sourceDaimonId = %d, daimonType = %s, tableQualifier = %s, priority = %d", sourceDaimonId, daimonType, tableQualifier, priority);
    }
}
