package org.ohdsi.webapi.estimation.comparativecohortanalysis.specification;

import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.TrimByPsArgs;
import org.ohdsi.webapi.RLangClassImpl;

/**
 *
 * @author asena5
 */
public class TrimByPsArgsImpl extends RLangClassImpl implements TrimByPsArgs {
  private Float trimFraction = 0.05f;

  /**
   * This fraction will be removed from each target group. In the target group, persons with the highest propensity scores will be removed, in the comparator group person with the lowest scores will be removed. 
   * @return trimFraction
   **/
  @Override
  public Float getTrimFraction() {
    return trimFraction;
  }

    /**
     *
     * @param trimFraction
     */
    public void setTrimFraction(Float trimFraction) {
    this.trimFraction = trimFraction;
  }
}
