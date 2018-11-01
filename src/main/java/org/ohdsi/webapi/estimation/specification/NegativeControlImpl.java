package org.ohdsi.webapi.estimation.specification;

import org.ohdsi.analysis.estimation.design.NegativeControl;
import org.ohdsi.analysis.estimation.design.NegativeControlTypeEnum;

public class NegativeControlImpl implements NegativeControl {
  private Long targetId = null;
  private Long comparatorId = null;
  private Long outcomeId = null;
  private String outcomeName = null;
  private NegativeControlTypeEnum type = NegativeControlTypeEnum.OUTCOME;

  /**
   * The identifier for the target cohort 
   * @return targetId
   **/
  @Override
  public Long getTargetId() {
    return targetId;
  }

  public void setTargetId(Long targetId) {
    this.targetId = targetId;
  }

  /**
   * The identifier for the comparator cohort 
   * @return comparatorId
   **/
  @Override
  public Long getComparatorId() {
    return comparatorId;
  }

  public void setComparatorId(Long comparatorId) {
    this.comparatorId = comparatorId;
  }

  /**
   * The identifier for the negative control cohort 
   * @return outcomeId
   **/
  @Override
  public Long getOutcomeId() {
    return outcomeId;
  }

  public void setOutcomeId(Long outcomeId) {
    this.outcomeId = outcomeId;
  }

  /**
   * The name of the negative control cohort 
   * @return outcomeName
   **/
  @Override
  public String getOutcomeName() {
    return outcomeName;
  }

  public void setOutcomeName(String outcomeName) {
    this.outcomeName = outcomeName;
  }

  /**
   * The type of negative control 
   * @return type
   **/
  @Override
  public NegativeControlTypeEnum getType() {
    return type;
  }

  public void setType(NegativeControlTypeEnum type) {
    this.type = type;
  }
}
