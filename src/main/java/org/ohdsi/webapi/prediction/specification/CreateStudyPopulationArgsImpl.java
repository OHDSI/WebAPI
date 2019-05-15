package org.ohdsi.webapi.prediction.specification;

import org.ohdsi.analysis.prediction.design.CreateStudyPopulationArgs;
import org.ohdsi.webapi.RLangClassImpl;

/**
 * Create a parameter object for the function createStudyPopulation 
 */
public class CreateStudyPopulationArgsImpl extends RLangClassImpl implements CreateStudyPopulationArgs {
  private Boolean binary = true;
  private Boolean includeAllOutcomes = true;
  private Boolean firstExposureOnly = false;
  private Integer washoutPeriod = 0;
  private Boolean removeSubjectsWithPriorOutcome = false;
  private Integer priorOutcomeLookback = 99999;
  private Boolean requireTimeAtRisk = true;
  private Integer minTimeAtRisk = 365;
  private Integer riskWindowStart = 0;
  private Boolean addExposureDaysToStart = false;
  private Integer riskWindowEnd = 365;
  private Boolean addExposureDaysToEnd = true;
  private String popAttrClass = "populationSettings";

  /**
   * Forces the outcomeCount to be 0 or 1 (use for binary prediction problems) 
   * @return binary
   **/
  @Override
  public Boolean getBinary() {
    return binary;
  }

    /**
     *
     * @param binary
     */
    public void setBinary(Boolean binary) {
    this.binary = binary;
  }

  /**
   * (binary) indicating whether to include people with outcomes who are not observed for the whole at risk period 
   * @return includeAllOutcomes
   **/
  @Override
  public Boolean getIncludeAllOutcomes() {
    return includeAllOutcomes;
  }

    /**
     *
     * @param includeAllOutcomes
     */
    public void setIncludeAllOutcomes(Boolean includeAllOutcomes) {
    this.includeAllOutcomes = includeAllOutcomes;
  }

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
   * Should subjects without time at risk be removed? 
   * @return requireTimeAtRisk
   **/
  @Override
  public Boolean getRequireTimeAtRisk() {
    return requireTimeAtRisk;
  }

    /**
     *
     * @param requireTimeAtRisk
     */
    public void setRequireTimeAtRisk(Boolean requireTimeAtRisk) {
    this.requireTimeAtRisk = requireTimeAtRisk;
  }

  /**
   * The miminum time at risk in days 
   * @return minTimeAtRisk
   **/
  @Override
  public Integer getMinTimeAtRisk() {
    return minTimeAtRisk;
  }

    /**
     *
     * @param minTimeAtRisk
     */
    public void setMinTimeAtRisk(Integer minTimeAtRisk) {
    this.minTimeAtRisk = minTimeAtRisk;
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
   * Get popAttrClass
   * @return popAttrClass
   **/
  @Override
  public String getAttrClass() {
    return popAttrClass;
  }

    /**
     *
     * @param attrClass
     */
    @Override
  public void setAttrClass(String attrClass) {
    this.popAttrClass = attrClass;
  }
}
