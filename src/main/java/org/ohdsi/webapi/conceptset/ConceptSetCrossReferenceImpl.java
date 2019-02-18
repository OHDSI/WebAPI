package org.ohdsi.webapi.conceptset;

import org.ohdsi.analysis.ConceptSetCrossReference;

public class ConceptSetCrossReferenceImpl implements ConceptSetCrossReference {
  private Integer conceptSetId = null;
  private String targetName = null;
  private Integer targetIndex = 0;
  private String propertyName = null;

  /**
   * The concept set ID
   * @return conceptSetId
   **/
  @Override
  public Integer getConceptSetId() {
    return conceptSetId;
  }

  public void setConceptSetId(Integer conceptSetId) {
    this.conceptSetId = conceptSetId;
  }

  /**
   * The target object name that will utilize the concept set
   * @return targetName
   **/
  @Override
  public String getTargetName() {
    return targetName;
  }

  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }

  /**
   * The index of the target object
   * @return targetIndex
   **/
  @Override
  public Integer getTargetIndex() {
    return targetIndex;
  }

  public void setTargetIndex(Integer targetIndex) {
    this.targetIndex = targetIndex;
  }

  /**
   * The property that will hold the list of concept IDs from  the resolved concept set 
   * @return propertyName
   **/
  @Override
  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }
}
