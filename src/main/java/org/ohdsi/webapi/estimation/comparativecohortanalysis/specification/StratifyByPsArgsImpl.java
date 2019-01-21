package org.ohdsi.webapi.estimation.comparativecohortanalysis.specification;

import org.ohdsi.webapi.RLangClassImpl;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.BaseSelectionEnum;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.StratifyByPsArgs;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 *
 * @author asena5
 */
public class StratifyByPsArgsImpl extends RLangClassImpl implements StratifyByPsArgs {
  private Integer numberOfStrata = 5;
  private BaseSelectionEnum baseSelection = BaseSelectionEnum.ALL;
  private List<String> stratificationColumns = null;

  /**
   * How many strata? The boundaries of the strata are automatically defined to contain equal numbers of target persons. 
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
     * @param stratificationColumnsItem
     * @return
     */
    public StratifyByPsArgsImpl addStratificationColumnsItem(String stratificationColumnsItem) {
    if (this.stratificationColumns == null) {
      this.stratificationColumns = new ArrayList<>();
    }
    this.stratificationColumns.add(stratificationColumnsItem);
    return this;
  }

  /**
   * Names of one or more columns in the data data.frame on which subjects should also be stratified in addition to stratification on propensity score. 
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
