package org.ohdsi.webapi.annotation.result;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import org.ohdsi.webapi.annotation.annotation.Annotation;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = "Result")
@Table(name = "annotation_result")
public class Result {

  @Id
  @GeneratedValue
  @Column(name = "result_id")
  private Long id;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "annotation_id")
  private Annotation annotation;

  @Column(name = "question_id")
  private Long questionId;

  @Column(name = "answer_id")
  private Long answerId;

//need charity's help here! these can all be retrieved from the annotation_id
  @JsonIgnore
  @Column(name = "set_id")
  private Long setId;

  @Column(name = "sample_name")
  private String sampleName;

  @JsonIgnore
  @Column(name = "subject_id")
  private Long subjectId;

  @Column(name = "value")
  private String value;

  @Column(name = "type")
  private String type;

  //***** GETTERS/SETTERS ******

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

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
   * @return the subjectId
   */
  public Long getSubjectId() {
    return subjectId;
  }

  /**
   * @param subjectId the subjectId to set
   */
  public void setSubjectId(Long subjectId) {
    this.subjectId = subjectId;
  }

  /**
   * @return the answerId
   */
  public Long getAnswerId() {
    return answerId;
  }

  /**
   * @param answerId the answerId to set
   */
  public void setAnswerId(Long answerId) {
    this.answerId = answerId;
  }

  /**
   * @return the setId
   */
  public Long getSetId() {
    return setId;
  }

  /**
   * @param setId the setId to set
   */
  public void setSetId(Long setId) {
    this.setId = setId;
  }

  /**
   * @return the questionId
   */
  public Long getQuestionId() {
    return questionId;
  }

  /**
   * @param questionId the questionId to set
   */
  public void setQuestionId(Long questionId) {
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
  /**
   * @return the type
   */
  public String getSampleName() {
    return sampleName;
  }

  /**
   * @param type the type to set
   */
  public void setSampleName(String name) {
    this.sampleName = name;
  }
}
