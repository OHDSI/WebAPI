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
 * @author asena5
 */
@Entity(name = "ConceptOfInterestMapping")
@Table(name="concept_of_interest")
public class ConceptOfInterestMapping implements Serializable {
  @Id
  @Column(name = "id")
  private Integer id;

  @Column(name = "concept_id")
  private Integer conceptId;

  @Column(name = "concept_of_interest_id")
  private Integer conceptOfInterestId;
  
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getConceptId() {
    return conceptId;
  }

  public void setConceptId(Integer conceptId) {
    this.conceptId = conceptId;
  }
  
  public Integer getConceptOfInterestId() {
    return conceptOfInterestId;
  }

  public void setConceptOfInterestId(Integer conceptOfInterestId) {
    this.conceptOfInterestId = conceptOfInterestId;
  }
}
