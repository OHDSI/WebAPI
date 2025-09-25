package org.ohdsi.webapi.annotation.navigation;
import java.util.List;
public class Navigation {

  private int prevSubjectId;
  private int nextSubjectId;
  private int nextUnannotatedSubjectId;
  private int numProfileSamples;
  private int numAnnotations;

  //***** GETTERS/SETTERS ******

  /**
   * @return the prevSubjectId
   */
  public int getPrevSubjectId() {
    return prevSubjectId;
  }

  /**
   * @param prevSubjectId the prevSubjectId to set
   */
  public void setPrevSubjectId(int prevSubjectId) {
    this.prevSubjectId = prevSubjectId;
  }

  /**
   * @return the nextSubjectId
   */
  public int getNextSubjectId() {
    return nextSubjectId;
  }

  /**
   * @param nextSubjectId the nextSubjectId to set
   */
  public void setNextSubjectId(int nextSubjectId) {
    this.nextSubjectId = nextSubjectId;
  }

  /**
   * @return the nextUnannotatedSubjectId
   */
  public int getNextUnannotatedSubjectId() {
    return nextUnannotatedSubjectId;
  }

  /**
   * @param nextUnannotatedSubjectId the nextUnannotatedSubjectId to set
   */
  public void setNextUnannotatedSubjectId(int nextUnannotatedSubjectId) {
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
