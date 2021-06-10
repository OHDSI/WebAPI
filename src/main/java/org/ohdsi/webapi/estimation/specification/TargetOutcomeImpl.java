package org.ohdsi.webapi.estimation.specification;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import org.ohdsi.analysis.estimation.design.TargetOutcome;

/**
 *
 * @author asena5
 */
public class TargetOutcomeImpl implements TargetOutcome {
  private Long targetId = null;
  private List<Long> outcomeIds = new ArrayList<>();

  /**
   * Target cohort id
   * @return targetId
   **/
  @NotNull
  @Override  
  public Long getTargetId() {
    return targetId;
  }

    /**
     *
     * @param targetId
     */
    public void setTargetId(Long targetId) {
    this.targetId = targetId;
  }

    /**
     *
     * @param outcomeIdsItem
     * @return
     */
    public TargetOutcomeImpl addOutcomeIdsItem(Long outcomeIdsItem) {
    this.outcomeIds.add(outcomeIdsItem);
    return this;
  }

  /**
   * The list of outcome cohort ids
   * @return outcomeIds
   **/
  @NotNull
  @Override  
  public List<Long> getOutcomeIds() {
    return outcomeIds;
  }

    /**
     *
     * @param outcomeIds
     */
    public void setOutcomeIds(List<Long> outcomeIds) {
    this.outcomeIds = outcomeIds;
  }
}
