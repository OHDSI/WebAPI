package org.ohdsi.webapi.annotation.annotation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Id;
import javax.persistence.Table;
import org.ohdsi.webapi.annotation.set.QuestionSet;

@Entity(name = "Annotation")
@Table(
  name = "annotation"
)
public class Annotation {

  @Id
  @GeneratedValue
  @Column(name = "annotation_id")
  private int id;

  @Column(name = "subject_id")
  private int subjectId;

  @Column(name = "cohort_sample_id")
  private int cohortSampleId;

  @ManyToOne
  @JoinColumn(name = "question_set_id")
  private QuestionSet questionSet;

  //***** GETTERS/SETTERS ******

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
	 * @param id the id to set
	 */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return the subjectId
   */
  public int getSubjectId() {
    return subjectId;
  }

  /**
   * @param subjectId the subjectId to set
   */
  public void setSubjectId(int subjectId) {
    this.subjectId = subjectId;
  }

  /**
   * @return the cohortId
   */
  public int getCohortSampleId() {
    return cohortSampleId;
  }

  /**
   * @param cohortSampleId the cohortId to set
   */
  public void setCohortSampleId(int cohortSampleId) {
    this.cohortSampleId = cohortSampleId;
  }

  /**
   * @return the set
   */
  public QuestionSet getQuestionSet() {
    return questionSet;
  }

  /**
   * @param set the set to set
   */
  public void setQuestionSet(QuestionSet questionSet) {
    this.questionSet = questionSet;
  }

}
