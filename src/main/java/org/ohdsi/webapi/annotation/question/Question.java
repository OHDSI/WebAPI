package org.ohdsi.webapi.annotation.question;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.ohdsi.webapi.annotation.answer.Answer;
import org.ohdsi.webapi.annotation.set.QuestionSet;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = "Question")
@Table(name = "annotation_question")
public class Question {

  @Id
  @GenericGenerator(
          name="question_generator",
          strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
          parameters = {
                  @org.hibernate.annotations.Parameter(name="sequence_name",value="annotation_question_seq"),
                  @org.hibernate.annotations.Parameter(name="increment_size",value="1")
          }
  )
  @GeneratedValue(generator = "question_generator")
  @Column(name = "question_id")
  private int id;

  @Column(name = "question_name")
  private String text;

  @Column(name = "question_type")
  private String type;

  @Column(name = "help_text")
  private String helpText;

  @Column(name = "case_question")
  private Boolean caseQuestion = false;

  @Column(name = "required")
  private Boolean required = true;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "set_id")
  private QuestionSet set;

  @OneToMany(
    fetch = FetchType.EAGER,
    mappedBy = "question",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  private List<Answer> answers = new ArrayList<>();

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
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * @param text the text to set
   */
  public void setText(String text) {
    this.text = text;
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
   * @return the helpText
   */
  public String getHelpText() {
    return helpText;
  }

  /**
   * @param helpText the helpText to set
   */
  public void setHelpText(String helpText) {
    this.helpText = helpText;
  }

  /**
   * @return the set
   */
  public QuestionSet getSet() {
    return set;
  }

  /**
   * @param set the set to set
   */
  public void setSet(QuestionSet set) {
    this.set = set;
  }

	/**
	 * @return the answers
	 */
	public List<Answer> getAnswers() {
			return new ArrayList<Answer>(answers);
	}

	/**
	 * @param answers the answers to set
	 */
	protected void setAnswers(List<Answer> answers) {
			this.answers = answers;
	}

  public void addToAnswers(Answer answer) {
      answer.setQuestion(this);
      this.answers.add(answer);
  }

  /**
   * @return  caseQuestion
   */
  public Boolean getCaseQuestion() {
    return caseQuestion;
  }

  /**
   * @param caseQuestion caseQuestion to set
   */
  public void setCaseQuestion(Boolean caseQuestion) {
    this.caseQuestion = caseQuestion;
  }

  /**
   * @return required
   */
  public Boolean getRequired() {
    return required;
  }

  /**
   * @param required required to set
   */
  public void setRequired(Boolean required) {
    this.required = required;
  }
}
