package org.ohdsi.webapi.annotation.result;

import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.ohdsi.webapi.annotation.annotation.AnnotationService;
import org.ohdsi.webapi.annotation.question.Question;
import org.ohdsi.webapi.annotation.question.QuestionService;
import org.ohdsi.webapi.annotation.study.Study;
import org.ohdsi.webapi.source.Source;
import org.springframework.beans.factory.annotation.Autowired;

//this table is for human viewing- don't expect to ever review this again
public class SuperResultDto {

  @Autowired
  private AnnotationService annotationService;

  @Autowired
  private QuestionService questionService;

  private int cohortId;
  private String cohortName;
  private String dataSourceKey;
//  can be removed/changed to dataSourceKey
  private String cohortSampleName;
  private String questionSetName;
  private int patientId;
  private String questionText;
  private String value;
  private Boolean caseStatus;

  public SuperResultDto(Result result){
    this.value = result.getValue();
  }

  public SuperResultDto(Result result, Study study, Source source){
    Question myQuestion = questionService.getQuestionByQuestionId(result.getQuestionId());
    this.caseStatus = myQuestion.getCaseQuestion();
    this.questionText = myQuestion.getText();
    Annotation tempanno = annotationService.getAnnotationsByAnnotationId(result.getAnnotation());
    this.value = result.getValue();
    this.patientId = tempanno.getSubjectId();
    this.cohortName= study.getCohortDefinition().getName();
    this.cohortId = study.getCohortDefinition().getId();
    this.dataSourceKey = source.getSourceKey();
    this.cohortSampleName = study.getCohortSample().getName();
    this.questionSetName = study.getQuestionSet().getName();
  }

  public SuperResultDto(Result result, Study study, Source source,Question myQuestion,Annotation tempanno){
    this.caseStatus = myQuestion.getCaseQuestion();
    this.questionText = myQuestion.getText();
    this.value = result.getValue();
    this.patientId = tempanno.getSubjectId();
    this.cohortName= study.getCohortDefinition().getName();
    this.cohortId = study.getCohortDefinition().getId();
    this.dataSourceKey = source.getSourceKey();
    this.cohortSampleName = study.getCohortSample().getName();
    this.questionSetName = study.getQuestionSet().getName();
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

  public String getDataSourceKey() {
    return dataSourceKey;
  }

  public void setDataSourceKey(String dataSourceKey) {
    this.dataSourceKey = dataSourceKey;
  }

  public String getCohortSampleName() {
    return cohortSampleName;
  }

  public void setCohortSampleName(String cohortSampleName) {
    this.cohortSampleName = cohortSampleName;
  }

  public String getQuestionSetName() {return questionSetName;}

  public void setQuestionSetName(String questionSetName) { this.questionSetName = questionSetName; }

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