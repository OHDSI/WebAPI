package org.ohdsi.webapi.estimation.comparativecohortanalysis.specification;

import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.StratifyByPsAndCovariatesArgs;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.BaseSelectionEnum;
import org.ohdsi.webapi.RLangClassImpl;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 *
 * @author asena5
 */
public class StratifyByPsAndCovariatesArgsImpl extends RLangClassImpl implements StratifyByPsAndCovariatesArgs {
  private Integer numberOfStrata = 5;
  private BaseSelectionEnum baseSelection = BaseSelectionEnum.ALL;
  private List<Integer> covariateIds = null;
  
  /**
   * Into how many strata should the propensity score be divided? The boundaries of the strata are automatically defined to contain equal numbers of target persons. 
   * @return numberOfStrata
   **/
  @Override
  @NotNull
  public Integer getNumberOfStrata() {
    return numberOfStrata;
  }

    /**
     *
     * @param numberOfStrata
     */
    public void setNumberOfStrata(Integer numberOfStrata) {
    this.numberOfStrata = numberOfStrata;
  }

  /**
   * What is the base selection of subjects where the strata bounds are to be determined? Strata are defined as equally-sized strata inside this selection. Possible values are \&quot;all\&quot;, \&quot;target\&quot;, and \&quot;comparator\&quot;. 
   * @return baseSelection
   **/
  @Override
  public BaseSelectionEnum getBaseSelection() {
    return baseSelection;
  }

    /**
     *
     * @param baseSelection
     */
    public void setBaseSelection(BaseSelectionEnum baseSelection) {
    this.baseSelection = baseSelection;
  }

    /**
     *
     * @param covariateIdsItem
     * @return
     */
    public StratifyByPsAndCovariatesArgsImpl addCovariateIdsItem(Integer covariateIdsItem) {
    if (this.covariateIds == null) {
      this.covariateIds = new ArrayList<>();
    }
    this.covariateIds.add(covariateIdsItem);
    return this;
  }

  /**
   * One or more covariate IDs in the cohortMethodData object on which subjects should also be stratified. 
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
