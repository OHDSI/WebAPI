package org.ohdsi.webapi.estimation.comparativecohortanalysis.specification;

import org.ohdsi.webapi.RLangClassImpl;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.CaliperScaleEnum;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.MatchOnPsAndCovariatesArgs;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author asena5
 */
public class MatchOnPsAndCovariatesArgsImpl extends RLangClassImpl implements MatchOnPsAndCovariatesArgs {
  private Float caliper = 0.2f;
  private CaliperScaleEnum caliperScale = CaliperScaleEnum.STANDARDIZED_LOGIT;
  private Integer maxRatio = 1;
  private List<Integer> covariateIds = null;

  /**
   * The caliper for matching. A caliper is the distance which is acceptable for any match. Observations which are outside of the caliper are dropped. A caliper of 0 means no caliper is used. 
   * @return caliper
   **/
  @Override
  public Float getCaliper() {
    return caliper;
  }

    /**
     *
     * @param caliper
     */
    public void setCaliper(Float caliper) {
    this.caliper = caliper;
  }

  /**
   * The scale on which the caliper is defined. Three scales are supported are &#x27;propensity score&#x27;, &#x27;standardized&#x27;, or &#x27;standardized logit&#x27;. On the standardized scale, the caliper is interpreted in standard deviations of the propensity score distribution. &#x27;standardized logit&#x27; is similar, except that the propensity score is transformed to the logit scale because the PS is more likely to be normally distributed on that scale(Austin, 2011). 
   * @return caliperScale
   **/
  @Override
  public CaliperScaleEnum getCaliperScale() {
    return caliperScale;
  }

    /**
     *
     * @param caliperScale
     */
    public void setCaliperScale(CaliperScaleEnum caliperScale) {
    this.caliperScale = caliperScale;
  }

  /**
   * The maximum number of persons int the comparator arm to be matched to each person in the target arm. A maxRatio of 0 means no maximum - all comparators will be assigned to a target person. 
   * @return maxRatio
   **/
  @Override
  public Integer getMaxRatio() {
    return maxRatio;
  }

    /**
     *
     * @param maxRatio
     */
    public void setMaxRatio(Integer maxRatio) {
    this.maxRatio = maxRatio;
  }

    /**
     *
     * @param covariateIdsItem
     * @return
     */
    public MatchOnPsAndCovariatesArgsImpl addCovariateIdsItem(Integer covariateIdsItem) {
    if (this.covariateIds == null) {
      this.covariateIds = new ArrayList<>();
    }
    this.covariateIds.add(covariateIdsItem);
    return this;
  }

  /**
   * One or more covariate IDs in the cohortMethodData object on which subjects should be also matched. 
   * @return covariateIds
   **/
  @Override
  public List<Integer> getCovariateIds() {
    return covariateIds;
  }

    /**
     *
     * @param covariateIds
     */
    public void setCovariateIds(List<Integer> covariateIds) {
    this.covariateIds = covariateIds;
  }
}
