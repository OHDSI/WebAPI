package org.ohdsi.webapi.annotation.answer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.annotation.question.Question;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity(name = "Answer")
@Table(name = "annotation_answer")
public class Answer {

  @Id
  @GenericGenerator(
          name="answer_generator",
          strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
          parameters = {
                  @Parameter(name="sequence_name",value="annotation_answer_seq"),
                  @Parameter(name="increment_size",value="1")
          }
  )
  @GeneratedValue(generator = "answer_generator")
  @Column(name = "answer_id")
  private int id;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id")
  private Question question;

  @Column(name = "text")
  private String text;

  @Column(name = "value")
  private String value;

  @Column(name = "help_text")
  private String helpText;

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
   * @return the question
   */
  public Question getQuestion() {
    return question;
  }

  /**
   * @param question the question to set
   */
  public void setQuestion(Question question) {
    this.question = question;
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
}
