package org.ohdsi.webapi.annotation.result;

import org.ohdsi.webapi.annotation.annotation.Annotation;

public class Result {

  private Annotation annotation;
  private int questionId;
  private int answerId;
  private String value;
  private String type;

  //***** GETTERS/SETTERS ******

  /**
   * @return the annotation
   */
  public Annotation getAnnotation() {
    return annotation;
  }

  /**
   * @param annotation the annotation to set
   */
  public void setAnnotation(Annotation annotation) {
    this.annotation = annotation;
  }

  /**
   * @return the answerId
   */
  public int getAnswerId() {
    return answerId;
  }

  /**
   * @param answerId the answerId to set
   */
  public void setAnswerId(int answerId) {
    this.answerId = answerId;
  }

  /**
   * @return the questionId
   */
  public int getQuestionId() {
    return questionId;
  }

  /**
   * @param questionId the questionId to set
   */
  public void setQuestionId(int questionId) {
    this.questionId = questionId;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }
}
