package org.ohdsi.webapi.annotation.set;

public class QuestionSampleDto {
  private int QuestionSetId;
  private int CohortSampleId;
  private String QuestionSetName;
  private String CohortSampleName;

  public QuestionSampleDto(int questionSetId, int cohortSampleId, String questionSetName, String cohortSampleName) {
    this.QuestionSetId = questionSetId;
    this.CohortSampleId = cohortSampleId;
    this.QuestionSetName = questionSetName;
    this.CohortSampleName = cohortSampleName;
  }

  //***** GETTERS/SETTERS ******

  public int getQuestionSetId() {
    return QuestionSetId;
  }

  public void setQuestionSetId(int questionSetId) {
    this.QuestionSetId = questionSetId;
  }

  public int getCohortSampleId() {
    return CohortSampleId;
  }

  public void setCohortSampleId(int cohortSampleId) {
    this.CohortSampleId = cohortSampleId;
  }

  public String getQuestionSetName() {
    return QuestionSetName;
  }

  public void setQuestionSetName(String questionSetName) {
    this.QuestionSetName = questionSetName;
  }

  public String getCohortSampleName() {
    return CohortSampleName;
  }

  public void setCohortSampleName(String cohortSampleName) {
    this.CohortSampleName = cohortSampleName;
  }

}