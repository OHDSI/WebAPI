package org.ohdsi.webapi.annotation.result;

public class SuperResultDto {

  private int cohortId;
  private String cohortName;
  private int dataSourceId;
  private int cohortSampleId;
  private String cohortSampleName;
  private int questionSetId;
  private String questionSetName;
  private int patientId;
  private String questionText;
  private String value;
  private Boolean caseStatus;

  public SuperResultDto(Result result){
    this.value = result.getValue();


  }

  //***** GETTERS/SETTERS *****

  public int getCohortId() {
    return cohortId;
  }

  public void setCohortId(int cohortId) {
    this.cohortId = cohortId;
  }

  public String getCohortName() {
    return cohortName;
  }

  public void setCohortName(String cohortName) {
    this.cohortName = cohortName;
  }

  public int getDataSourceId() {
    return dataSourceId;
  }

  public void setDataSourceId(int dataSourceId) {
    this.dataSourceId = dataSourceId;
  }

  public int getCohortSampleId() {
    return cohortSampleId;
  }

  public void setCohortSampleId(int cohortSampleId) {
    this.cohortSampleId = cohortSampleId;
  }

  public String getCohortSampleName() {
    return cohortSampleName;
  }

  public void setCohortSampleName(String cohortSampleName) {
    this.cohortSampleName = cohortSampleName;
  }

  public int getQuestionSetId() {
    return questionSetId;
  }

  public void setQuestionSetId(int questionSetId) {
    this.questionSetId = questionSetId;
  }

  public String getQuestionSetName() {return questionSetName;}

  public void setQuestionSetName(String questionSetName) {
    this.questionSetName = questionSetName;
  }

  public int getPatientId() {
    return patientId;
  }

  public void setPatientId(int patientId) {
    this.patientId = patientId;
  }

  public String getQuestionText() {
    return questionText;
  }

  public void setQuestionText(String questionText) {
    this.questionText = questionText;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Boolean getCaseStatus() {
    return caseStatus;
  }

  public void setCaseStatus(Boolean caseStatus) {
    this.caseStatus = caseStatus;
  }
}