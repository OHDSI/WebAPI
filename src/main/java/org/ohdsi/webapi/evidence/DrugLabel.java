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
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author fdefalco
 */
@Entity(name = "DrugLabel")
@Table(name="drug_labels")
public class DrugLabel implements Serializable {

  @Id
  @Column(name="drug_label_id")
  private int drugLabelId;
  
  @Column(name="setid")  
  private String setid;
   
  @Column(name="search_name")  
  private String searchName;
  
  @Column(name="ingredient_concept_name")
  private String ingredientConceptName;
  
  @Column(name="ingredient_concept_id")
  private int ingredientConceptId;
  
  @Column(name="cohort_id")
  private Integer cohortId;
  
  @Column(name="image_url")
  private String imageUrl;
  
  @Column(name="date")
  private Timestamp date;

  public int getDrugLabelId() {
    return drugLabelId;
  }

  public void setDrugLabelId(int drugLabelId) {
    this.drugLabelId = drugLabelId;
  }

  public String getSetid() {
    return setid;
  }

  public void setSetid(String setid) {
    this.setid = setid;
  }

  public String getSearchName() {
    return searchName;
  }

  public void setSearchName(String searchName) {
    this.searchName = searchName;
  }

  public String getIngredientConceptName() {
    return ingredientConceptName;
  }

  public void setIngredientConceptName(String ingredientConceptName) {
    this.ingredientConceptName = ingredientConceptName;
  }

  public int getIngredientConceptId() {
    return ingredientConceptId;
  }

  public void setIngredientConceptId(int ingredientConceptId) {
    this.ingredientConceptId = ingredientConceptId;
  }

  public Integer getCohortId() {
    return cohortId;
  }

  public void setCohortId(Integer cohortId) {
    this.cohortId = cohortId;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public Timestamp getDate() {
    return date;
  }

  public void setDate(Timestamp date) {
    this.date = date;
  }
}

