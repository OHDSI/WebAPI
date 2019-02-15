package org.ohdsi.webapi.estimation.comparativecohortanalysis.specification;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.TargetComparatorOutcomes;
import org.ohdsi.webapi.estimation.specification.TargetOutcomeImpl;

/**
 *
 * @author asena5
 */
public class TargetComparatorOutcomesImpl extends TargetOutcomeImpl implements TargetComparatorOutcomes {
  private Long comparatorId = null;
  private List<Long> excludedCovariateConceptIds = null;
  private List<Long> includedCovariateConceptIds = null;

  /**
   * Comparator cohort id
   * @return comparatorId
   **/
  @NotNull
  @Override  
  public Long getComparatorId() {
    return comparatorId;
  }

    /**
     *
     * @param comparatorId
     */
    public void setComparatorId(Long comparatorId) {
    this.comparatorId = comparatorId;
  }

    /**
     *
     * @param excludedCovariateConceptIdsItem
     * @return
     */
    public TargetComparatorOutcomesImpl addExcludedCovariateConceptIdsItem(Long excludedCovariateConceptIdsItem) {
    if (this.excludedCovariateConceptIds == null) {
      this.excludedCovariateConceptIds = new ArrayList<>();
    }
    this.excludedCovariateConceptIds.add(excludedCovariateConceptIdsItem);
    return this;
  }

  /**
   * A list of concept IDs that cannot be used to construct covariates. This argument is to be used only for exclusionconcepts that are specific to the drug-comparator combination.
   * @return excludedCovariateConceptIds
   **/
  @Override  
  public List<Long> getExcludedCovariateConceptIds() {
    return excludedCovariateConceptIds;
  }

    /**
     *
     * @param excludedCovariateConceptIds
     */
    public void setExcludedCovariateConceptIds(List<Long> excludedCovariateConceptIds) {
    this.excludedCovariateConceptIds = excludedCovariateConceptIds;
  }

    /**
     *
     * @param includedCovariateConceptIdsItem
     * @return
     */
    public TargetComparatorOutcomesImpl addIncludedCovariateConceptIdsItem(Long includedCovariateConceptIdsItem) {
    if (this.includedCovariateConceptIds == null) {
      this.includedCovariateConceptIds = new ArrayList<>();
    }
    this.includedCovariateConceptIds.add(includedCovariateConceptIdsItem);
    return this;
  }

  /**
   * A list of concept IDs that must be used to construct covariates. This argument is to be used only for inclusion concepts that are specific to the drug-comparator combination.
   * @return includedCovariateConceptIds
   **/
  @Override  
  public List<Long> getIncludedCovariateConceptIds() {
    return includedCovariateConceptIds;
  }

    /**
     *
     * @param includedCovariateConceptIds
     */
    public void setIncludedCovariateConceptIds(List<Long> includedCovariateConceptIds) {
    this.includedCovariateConceptIds = includedCovariateConceptIds;
  }
}
