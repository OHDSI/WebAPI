package org.ohdsi.webapi.estimation.comparativecohortanalysis.specification;

import org.ohdsi.webapi.RLangClassImpl;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.CreateStudyPopulationArgs;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.RemoveDuplicateSubjectsEnum;

/**
 *
 * @author asena5
 */
public class CreateStudyPopulationArgsImpl extends RLangClassImpl implements CreateStudyPopulationArgs {
  private Boolean firstExposureOnly = false;
  private Boolean restrictToCommonPeriod = false;
  private Integer washoutPeriod = 0;
  private RemoveDuplicateSubjectsEnum removeDuplicateSubjects = RemoveDuplicateSubjectsEnum.KEEP_ALL;
  private Boolean removeSubjectsWithPriorOutcome = false;
  private Integer priorOutcomeLookback = 99999;
  private Integer minDaysAtRisk = 1;
  private Integer riskWindowStart = 0;
  private Boolean addExposureDaysToStart = false;
  private Integer riskWindowEnd = 0;
  private Boolean addExposureDaysToEnd = true;
  private Boolean censorAtNewRiskWindow = null;
  

  /**
   * Should only the first exposure per subject be included? Note that this is typically done in the createStudyPopulation function 
   * @return firstExposureOnly
   **/
  @Override
  public Boolean getFirstExposureOnly() {
    return firstExposureOnly;
  }

    /**
     *
     * @param firstExposureOnly
     */
    public void setFirstExposureOnly(Boolean firstExposureOnly) {
    this.firstExposureOnly = firstExposureOnly;
  }

  /**
   * Restrict the analysis to the period when both exposures are observed? 
   * @return restrictToCommonPeriod
   **/
  @Override
  public Boolean getRestrictToCommonPeriod() {
    return restrictToCommonPeriod;
  }

    /**
     *
     * @param restrictToCommonPeriod
     */
    public void setRestrictToCommonPeriod(Boolean restrictToCommonPeriod) {
    this.restrictToCommonPeriod = restrictToCommonPeriod;
  }

  /**
   * The minimum required continuous observation time prior to index date for a person to be included in the cohort. 
   * @return washoutPeriod
   **/
  @Override
  public Integer getWashoutPeriod() {
    return washoutPeriod;
  }

    /**
     *
     * @param washoutPeriod
     */
    public void setWashoutPeriod(Integer washoutPeriod) {
    this.washoutPeriod = washoutPeriod;
  }

  /**
   * Remove subjects that are in both the target and comparator cohort? 
   * @return removeDuplicateSubjects
   **/
  @Override
  public RemoveDuplicateSubjectsEnum getRemoveDuplicateSubjects() {
    return removeDuplicateSubjects;
  }

    /**
     *
     * @param removeDuplicateSubjects
     */
    public void setRemoveDuplicateSubjects(RemoveDuplicateSubjectsEnum removeDuplicateSubjects) {
    this.removeDuplicateSubjects = removeDuplicateSubjects;
  }

  /**
   * Remove subjects that have the outcome prior to the risk window start? 
   * @return removeSubjectsWithPriorOutcome
   **/
  @Override
  public Boolean getRemoveSubjectsWithPriorOutcome() {
    return removeSubjectsWithPriorOutcome;
  }

    /**
     *
     * @param removeSubjectsWithPriorOutcome
     */
    public void setRemoveSubjectsWithPriorOutcome(Boolean removeSubjectsWithPriorOutcome) {
    this.removeSubjectsWithPriorOutcome = removeSubjectsWithPriorOutcome;
  }

  /**
   * How many days should we look back when identifying prior outcomes? 
   * @return priorOutcomeLookback
   **/
  @Override
  public Integer getPriorOutcomeLookback() {
    return priorOutcomeLookback;
  }

    /**
     *
     * @param priorOutcomeLookback
     */
    public void setPriorOutcomeLookback(Integer priorOutcomeLookback) {
    this.priorOutcomeLookback = priorOutcomeLookback;
  }

  /**
   * The minimum required number of days at risk. 
   * @return minDaysAtRisk
   **/
  @Override
  public Integer getMinDaysAtRisk() {
    return minDaysAtRisk;
  }

    /**
     *
     * @param minDaysAtRisk
     */
    public void setMinDaysAtRisk(Integer minDaysAtRisk) {
    this.minDaysAtRisk = minDaysAtRisk;
  }

  /**
   * The start of the risk window (in days) relative to the indexdate (+ days of exposure if theaddExposureDaysToStart parameter is specified). 
   * @return riskWindowStart
   **/
  @Override
  public Integer getRiskWindowStart() {
    return riskWindowStart;
  }

    /**
     *
     * @param riskWindowStart
     */
    public void setRiskWindowStart(Integer riskWindowStart) {
    this.riskWindowStart = riskWindowStart;
  }

  /**
   * Add the length of exposure the start of the risk window?
   * @return addExposureDaysToStart
   **/
  @Override
  public Boolean getAddExposureDaysToStart() {
    return addExposureDaysToStart;
  }

    /**
     *
     * @param addExposureDaysToStart
     */
    public void setAddExposureDaysToStart(Boolean addExposureDaysToStart) {
    this.addExposureDaysToStart = addExposureDaysToStart;
  }

  /**
   * The end of the risk window (in days) relative to the index date (+ days of exposure if the addExposureDaysToEnd parameter is specified). 
   * @return riskWindowEnd
   **/
  @Override
  public Integer getRiskWindowEnd() {
    return riskWindowEnd;
  }

    /**
     *
     * @param riskWindowEnd
     */
    public void setRiskWindowEnd(Integer riskWindowEnd) {
    this.riskWindowEnd = riskWindowEnd;
  }

  /**
   * Add the length of exposure the risk window? 
   * @return addExposureDaysToEnd
   **/
  @Override
  public Boolean getAddExposureDaysToEnd() {
    return addExposureDaysToEnd;
  }

    /**
     *
     * @param addExposureDaysToEnd
     */
    public void setAddExposureDaysToEnd(Boolean addExposureDaysToEnd) {
    this.addExposureDaysToEnd = addExposureDaysToEnd;
  }

  /**
   * If a subject is in multiple cohorts, should time-at-risk be censored when the new time-at-risk start to prevent overlap? 
   * @return censorAtNewRiskWindow
   **/
  @Override
  public Boolean getCensorAtNewRiskWindow() {
    return censorAtNewRiskWindow;
  }

    /**
     *
     * @param censorAtNewRiskWindow
     */
    public void setCensorAtNewRiskWindow(Boolean censorAtNewRiskWindow) {
    this.censorAtNewRiskWindow = censorAtNewRiskWindow;
  }
}
