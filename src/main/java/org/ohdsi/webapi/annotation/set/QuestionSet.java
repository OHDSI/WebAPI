package org.ohdsi.webapi.annotation.set;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Id;
import javax.persistence.Table;
import org.ohdsi.webapi.annotation.question.Question;

@Entity(name = "QuestionSet")
@Table(name = "annotation_set")
public class QuestionSet {

  @Id
  @GeneratedValue
  @Column(name = "set_id")
  private int id;

  @Column(name = "cohort_definition_id")
  private int cohortId;

  private String name;

  @OneToMany(
    fetch = FetchType.EAGER,
    mappedBy = "set",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  private Set<Question> questions = new HashSet();

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
   * @return the cohortId
   */
  public int getCohortId() {
    return cohortId;
  }

  /**
   * @param cohortId the cohortId to set
   */
  public void setCohortId(int cohortId) {
    this.cohortId = cohortId;
  }


  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the questions
   */
  public Set<Question> getQuestions() {
      return new HashSet<Question>(questions);
  }

  /**
   * @param questions the questions to set
   */
  protected void setQuestions(Set<Question> questions) {
      this.questions = questions;
  }

  public void addToQuestions(Question question) {
      question.setSet(this);
      this.questions.add(question);
  }

}
