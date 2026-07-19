package org.ohdsi.webapi.annotation.result;

import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.ohdsi.webapi.annotation.annotation.AnnotationService;
import org.ohdsi.webapi.annotation.answer.Answer;
import org.ohdsi.webapi.annotation.answer.AnswerService;
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

  @Autowired
  private AnswerService answerService;

  private int cohortId;
  private String cohortName;
  private String dataSourceKey;
  private String cohortSampleName;
  private String questionSetName;
  private int patientId;
  private String questionText;
  private String answerText;
  private String answerValue;
  private Boolean caseStatus;

  public SuperResultDto(Result result){
    this.answerValue = result.getValue();
  }

  public SuperResultDto(Result result, Study study, Source source){
    Question myQuestion = questionService.getQuestionByQuestionId(result.getQuestionId());
    this.caseStatus = myQuestion.getCaseQuestion();
    this.questionText = myQuestion.getText();
    Answer tempAnswer = answerService.getAnswerById(result.getAnswerId());
    this.answerText = tempAnswer.getText();
    Annotation tempanno = annotationService.getAnnotationsByAnnotationId(result.getAnnotation());
    this.answerValue = result.getValue();
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
    this.answerValue = result.getValue();
    this.patientId = tempanno.getSubjectId();
    this.cohortName= study.getCohortDefinition().getName();
    this.cohortId = study.getCohortDefinition().getId();
    this.dataSourceKey = source.getSourceKey();
    this.cohortSampleName = study.getCohortSample().getName();
    this.questionSetName = study.getQuestionSet().getName();
    Answer tempAnswer = answerService.getAnswerById(result.getAnswerId());
    this.answerText = tempAnswer.getText();
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

  public String getAnswerText() {return answerText;}

  public void setAnswerText(String answerText) {this.answerText = answerText;}

  public String getAnswerValue() {
    return answerValue;
  }

  public void setAnswerValue(String answerValue) {
    this.answerValue = answerValue;
  }

  public Boolean getCaseStatus() {
    return caseStatus;
  }

  public void setCaseStatus(Boolean caseStatus) {
    this.caseStatus = caseStatus;
  }
}