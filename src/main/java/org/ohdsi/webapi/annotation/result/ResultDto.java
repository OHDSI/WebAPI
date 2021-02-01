package org.ohdsi.webapi.annotation.result;

public class ResultDto {

  private Long answerId;
  private Long questionId;
  private String value;
  private String type;
  private String sampleName;

  //***** GETTERS/SETTERS ******

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
