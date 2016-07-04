/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.evidence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author asena5
 */
@Entity(name = "CohortStudyMapping")
@Table(name="cohort_study")
public class CohortStudyMapping {
  @Id
  @Column(name = "cohort_study_id")
  private Integer cohortStudyId;

  @Column(name = "cohort_definition_id")
  private Integer cohortDefinitionId;

  @Column(name = "study_type")
  private Integer studyType;

  @Column(name = "study_name")
  private String studyName;

  @Column(name = "study_URL")
  private String studyUrl;

  public Integer getCohortStudyId() {
    return cohortStudyId;
  }

  public void setCohortStudyId(Integer cohortStudyId) {
    this.cohortStudyId = cohortDefinitionId;
  }

  public Integer getCohortDefinitionId() {
    return cohortDefinitionId;
  }

  public void setCohortDefinitionId(Integer cohortDefinitionId) {
    this.cohortDefinitionId = cohortDefinitionId;
  }

  public Integer getStudyType() {
    return studyType;
  }

  public void setStudyType(Integer studyType) {
    this.studyType = studyType;
  }    
  
  public String getStudyName() {
    return studyName;
  }

  public void setStudyName(String studyName) {
    this.studyName = studyName;
  }    
  
  public String getStudyUrl() {
    return studyUrl;
  }

  public void setStudyUrl(String studyUrl) {
    this.studyUrl = studyUrl;
  }    
}
