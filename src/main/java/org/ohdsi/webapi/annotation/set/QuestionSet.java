package org.ohdsi.webapi.annotation.set;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.ohdsi.webapi.annotation.question.Question;

@Entity(name = "QuestionSet")
@Table(name = "annotation_set")
public class QuestionSet {

  @Id
  @GenericGenerator(
          name="set_generator",
          strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
          parameters = {
                  @org.hibernate.annotations.Parameter(name="sequence_name",value="annotation_question_set_sequence"),
                  @org.hibernate.annotations.Parameter(name="increment_size",value="1")
          }
  )
  @GeneratedValue(generator = "set_generator")
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
  @OrderBy("id")
  private Set<Question> questions = new LinkedHashSet();

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
      return new LinkedHashSet<Question>(questions);
  }

  /**
   * @param questions the questions to set
   */
  protected void setQuestions(LinkedHashSet<Question> questions) {
      this.questions = questions;
  }

  public void addToQuestions(Question question) {
      question.setSet(this);
      this.questions.add(question);
  }

}
