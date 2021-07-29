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
package org.ohdsi.webapi.evidence;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author fdefalco
 */

@Entity(name = "ConceptCohortMapping")
@Table(name="cohort_concept_map")
public class ConceptCohortMapping implements Serializable {
  
  @Id
  @Column(name = "cohort_definition_id")
  private Integer cohortDefinitionId;

  @Column(name = "cohort_definition_name")
  private String cohortDefinitionName;

  @Column(name = "concept_id")
  private Integer conceptId;

  public Integer getCohortDefinitionId() {
    return cohortDefinitionId;
  }

  public void setCohortDefinitionId(Integer cohortDefinitionId) {
    this.cohortDefinitionId = cohortDefinitionId;
  }

  public String getCohortDefinitionName() {
    return cohortDefinitionName;
  }

  public void setCohortDefinitionName(String cohortDefinitionName) {
    this.cohortDefinitionName = cohortDefinitionName;
  }

  public Integer getConceptId() {
    return conceptId;
  }

  public void setConceptId(Integer conceptId) {
    this.conceptId = conceptId;
  }
  
  
}
