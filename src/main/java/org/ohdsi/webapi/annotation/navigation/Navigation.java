package org.ohdsi.webapi.annotation.navigation;
import java.util.List;
public class Navigation {

  private Long prevSubjectId;
  private Long nextSubjectId;
  private Long nextUnannotatedSubjectId;
  private int numProfileSamples;
  private int numAnnotations;

  //***** GETTERS/SETTERS ******

  /**
   * @return the prevSubjectId
   */
  public Long getPrevSubjectId() {
    return prevSubjectId;
  }

  /**
   * @param prevSubjectId the prevSubjectId to set
   */
  public void setPrevSubjectId(Long prevSubjectId) {
    this.prevSubjectId = prevSubjectId;
  }

  /**
   * @return the nextSubjectId
   */
  public Long getNextSubjectId() {
    return nextSubjectId;
  }

  /**
   * @param nextSubjectId the nextSubjectId to set
   */
  public void setNextSubjectId(Long nextSubjectId) {
    this.nextSubjectId = nextSubjectId;
  }

  /**
   * @return the nextUnannotatedSubjectId
   */
  public Long getNextUnannotatedSubjectId() {
    return nextUnannotatedSubjectId;
  }

  /**
   * @param nextUnannotatedSubjectId the nextUnannotatedSubjectId to set
   */
  public void setNextUnannotatedSubjectId(Long nextUnannotatedSubjectId) {
    this.nextUnannotatedSubjectId = nextUnannotatedSubjectId;
  }

  /**
   * @return the numProfileSamples
   */
  public int getNumProfileSamples() {
    return numProfileSamples;
  }

  /**
   * @param numProfileSamples the numProfileSamples to set
   */
  public void setNumProfileSamples(int numProfileSamples) {
    this.numProfileSamples = numProfileSamples;
  }

  /**
   * @return the numAnnotations
   */
  public int getNumAnnotations() {
    return numAnnotations;
  }

  /**
   * @param numAnnotations the numAnnotations to set
   */
  public void setNumAnnotations(int numAnnotations) {
    this.numAnnotations = numAnnotations;
  }

}
