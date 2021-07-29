package org.ohdsi.webapi.estimation.comparativecohortanalysis.specification;

import org.ohdsi.webapi.RLangClassImpl;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.CaliperScaleEnum;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.MatchOnPsArgs;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author asena5
 */
public class MatchOnPsArgsImpl extends RLangClassImpl implements MatchOnPsArgs {
  private Float caliper = 0.2f;
  private CaliperScaleEnum caliperScale = CaliperScaleEnum.STANDARDIZED_LOGIT;
  private Integer maxRatio = 1;
  private List<String> stratificationColumns = null;

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
     * @param stratificationColumnsItem
     * @return
     */
    public MatchOnPsArgsImpl addStratificationColumnsItem(String stratificationColumnsItem) {
    if (this.stratificationColumns == null) {
      this.stratificationColumns = new ArrayList<>();
    }
    this.stratificationColumns.add(stratificationColumnsItem);
    return this;
  }

  /**
   * Names or numbers of one or more columns in the data data.frame on which subjects should be stratified prior to matching. No personswill be matched with persons outside of the strata identified by thevalues in these columns. 
   * @return stratificationColumns
   **/
  @Override
  public List<String> getStratificationColumns() {
    return stratificationColumns;
  }

    /**
     *
     * @param stratificationColumns
     */
    public void setStratificationColumns(List<String> stratificationColumns) {
    this.stratificationColumns = stratificationColumns;
  }
}
