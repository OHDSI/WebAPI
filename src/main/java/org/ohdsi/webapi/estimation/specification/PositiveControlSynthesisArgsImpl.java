package org.ohdsi.webapi.estimation.specification;

import java.util.ArrayList;
import java.util.List;
import org.ohdsi.analysis.estimation.design.PositiveControlSynthesisModelTypeEnum;
import org.ohdsi.analysis.estimation.design.PositiveControlSynthesisArgs;
import org.ohdsi.webapi.cyclops.specification.ControlImpl;
import org.ohdsi.webapi.featureextraction.specification.CovariateSettingsImpl;
import org.ohdsi.webapi.cyclops.specification.PriorImpl;

/**
 *
 * @author asena5
 */
public class PositiveControlSynthesisArgsImpl implements PositiveControlSynthesisArgs {
  private PositiveControlSynthesisModelTypeEnum modelType = PositiveControlSynthesisModelTypeEnum.SURVIVAL;
  private Integer minOutcomeCountForModel = 100;
  private Integer minOutcomeCountForInjection = 25;
  private CovariateSettingsImpl covariateSettings = null;
  private PriorImpl prior = null;
  private ControlImpl control = null;
  private Boolean firstExposureOnly = false;
  private Integer washoutPeriod = 183;
  private Integer riskWindowStart = 0;
  private Integer riskWindowEnd = 0;
  private Boolean addExposureDaysToEnd = true;
  private Boolean firstOutcomeOnly = false;
  private Boolean removePeopleWithPriorOutcomes = false;
  private Integer maxSubjectsForModel = 100000;
  private List<Float> effectSizes = null;
  private Float precision = 0.01f;
  private Integer outputIdOffset = 1000;
  /**
   * Can be either \&quot;poisson\&quot; or \&quot;survival\&quot; 
   * @return modelType
   **/
  @Override
  public PositiveControlSynthesisModelTypeEnum getModelType() {
    return modelType;
  }

    /**
     *
     * @param modelType
     */
    public void setModelType(PositiveControlSynthesisModelTypeEnum modelType) {
    this.modelType = modelType;
  }

  /**
   * Minimum number of outcome events required to build a model. 
   * @return minOutcomeCountForModel
   **/
  @Override
  public Integer getMinOutcomeCountForModel() {
    return minOutcomeCountForModel;
  }

    /**
     *
     * @param minOutcomeCountForModel
     */
    public void setMinOutcomeCountForModel(Integer minOutcomeCountForModel) {
    this.minOutcomeCountForModel = minOutcomeCountForModel;
  }

  /**
   * Minimum number of outcome events required to inject a signal. 
   * @return minOutcomeCountForInjection
   **/
  @Override
  public Integer getMinOutcomeCountForInjection() {
    return minOutcomeCountForInjection;
  }

    /**
     *
     * @param minOutcomeCountForInjection
     */
    public void setMinOutcomeCountForInjection(Integer minOutcomeCountForInjection) {
    this.minOutcomeCountForInjection = minOutcomeCountForInjection;
  }

  /**
   * Get covariateSettings
   * @return covariateSettings
   **/
  @Override
  public CovariateSettingsImpl getCovariateSettings() {
    return covariateSettings;
  }

    /**
     *
     * @param covariateSettings
     */
    public void setCovariateSettings(CovariateSettingsImpl covariateSettings) {
    this.covariateSettings = covariateSettings;
  }

  /**
   * Get prior
   * @return prior
   **/
  @Override
  public PriorImpl getPrior() {
    return prior;
  }

    /**
     *
     * @param prior
     */
    public void setPrior(PriorImpl prior) {
    this.prior = prior;
  }

  /**
   * Get control
   * @return control
   **/
  @Override
  public ControlImpl getControl() {
    return control;
  }

    /**
     *
     * @param control
     */
    public void setControl(ControlImpl control) {
    this.control = control;
  }

  /**
   * Should signals be injected only for the first exposure? (ie. assuming an acute effect) 
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
   * Number of days at the start of observation for which no signals will be injected, but will be used to determine whether exposure or outcome is the first one, and for extracting covariates to build the outcome model. 
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
   * The start of the risk window relative to the start of the exposure (in days). When 0, risk is assumed to start on the first day of exposure. 
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
   * The end of the risk window relative to the start of the exposure. Note that typically the length of exposure is added to this number (when the addExposureDaysToEnd parameter is set to TRUE). 
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
   * Should length of exposure be added to the risk window? 
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
   * Should only the first outcome per person be considered when modeling the outcome? 
   * @return firstOutcomeOnly
   **/
  @Override
  public Boolean getFirstOutcomeOnly() {
    return firstOutcomeOnly;
  }

    /**
     *
     * @param firstOutcomeOnly
     */
    public void setFirstOutcomeOnly(Boolean firstOutcomeOnly) {
    this.firstOutcomeOnly = firstOutcomeOnly;
  }

  /**
   * Remove people with prior outcomes? 
   * @return removePeopleWithPriorOutcomes
   **/
  @Override
  public Boolean getRemovePeopleWithPriorOutcomes() {
    return removePeopleWithPriorOutcomes;
  }

    /**
     *
     * @param removePeopleWithPriorOutcomes
     */
    public void setRemovePeopleWithPriorOutcomes(Boolean removePeopleWithPriorOutcomes) {
    this.removePeopleWithPriorOutcomes = removePeopleWithPriorOutcomes;
  }

  /**
   * Maximum number of people used to fit an outcome model. 
   * @return maxSubjectsForModel
   **/
  @Override
  public Integer getMaxSubjectsForModel() {
    return maxSubjectsForModel;
  }

    /**
     *
     * @param maxSubjectsForModel
     */
    public void setMaxSubjectsForModel(Integer maxSubjectsForModel) {
    this.maxSubjectsForModel = maxSubjectsForModel;
  }

    /**
     *
     * @param effectSizesItem
     * @return
     */
    public PositiveControlSynthesisArgsImpl addEffectSizesItem(Float effectSizesItem) {
    if (this.effectSizes == null) {
      this.effectSizes = new ArrayList<>();
    }
    this.effectSizes.add(effectSizesItem);
    return this;
  }

  /**
   * A numeric vector of effect sizes that should be inserted. 
   * @return effectSizes
   **/
  @Override
  public List<Float> getEffectSizes() {
    return effectSizes;
  }

    /**
     *
     * @param effectSizes
     */
    public void setEffectSizes(List<Float> effectSizes) {
    this.effectSizes = effectSizes;
  }

  /**
   * The allowed ratio between target and injected signal size. 
   * @return precision
   **/
  @Override
  public Float getPrecision() {
    return precision;
  }

    /**
     *
     * @param precision
     */
    public void setPrecision(Float precision) {
    this.precision = precision;
  }

  /**
   * What should be the first new outcome ID that is to be created? 
   * @return outputIdOffset
   **/
  @Override
  public Integer getOutputIdOffset() {
    return outputIdOffset;
  }

    /**
     *
     * @param outputIdOffset
     */
    public void setOutputIdOffset(Integer outputIdOffset) {
    this.outputIdOffset = outputIdOffset;
  }    
}
