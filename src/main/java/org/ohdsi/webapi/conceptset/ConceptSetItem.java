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
package org.ohdsi.webapi.conceptset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author fdefalco
 */

@Entity(name = "ConceptSetItem")
@Table(name="concept_set_item")
public class ConceptSetItem implements Serializable{
  
  @Id
  @GeneratedValue  
  @Column(name="concept_set_item_id")    
  private int id;
  
  @Column(name="concept_set_id")
  private int conceptSetId;
  
  @Column(name="concept_id")
  private long conceptId;  
  
  @Column(name="is_excluded")
  private int isExcluded;
  
  @Column(name="include_descendants")
  private int includeDescendants;
  
  @Column(name="include_mapped")
  private int includeMapped;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getConceptSetId() {
    return conceptSetId;
  }

  public void setConceptSetId(int conceptSetId) {
    this.conceptSetId = conceptSetId;
  }

  public long getConceptId() {
    return conceptId;
  }

  public void setConceptId(long conceptId) {
    this.conceptId = conceptId;
  }

  public int getIsExcluded() {
    return isExcluded;
  }

  public void setIsExcluded(int isExcluded) {
    this.isExcluded = isExcluded;
  }

  public int getIncludeDescendants() {
    return includeDescendants;
  }

  public void setIncludeDescendants(int includeDescendants) {
    this.includeDescendants = includeDescendants;
  }

  public int getIncludeMapped() {
    return includeMapped;
  }

  public void setIncludeMapped(int includeMapped) {
    this.includeMapped = includeMapped;
  }
}
