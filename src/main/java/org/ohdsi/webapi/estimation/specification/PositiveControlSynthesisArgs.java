package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.ohdsi.webapi.cyclops.specification.Control;
import org.ohdsi.webapi.featureextraction.specification.CovariateSettings;
import org.ohdsi.webapi.cyclops.specification.Prior;
import java.util.ArrayList;
import java.util.List;

public class PositiveControlSynthesisArgs {
  /**
   * Can be either \&quot;poisson\&quot; or \&quot;survival\&quot; 
   */
  public enum ModelTypeEnum {
    POISSON("poisson"),
    
    SURVIVAL("survival");

    private String value;

    ModelTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ModelTypeEnum fromValue(String text) {
      for (ModelTypeEnum b : ModelTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("modelType")
  private ModelTypeEnum modelType = ModelTypeEnum.SURVIVAL;

  @JsonProperty("minOutcomeCountForModel")
  private Integer minOutcomeCountForModel = 100;

  @JsonProperty("minOutcomeCountForInjection")
  private Integer minOutcomeCountForInjection = 25;

  @JsonProperty("covariateSettings")
  private CovariateSettings covariateSettings = null;

  @JsonProperty("prior")
  private Prior prior = null;

  @JsonProperty("control")
  private Control control = null;

  @JsonProperty("firstExposureOnly")
  private Boolean firstExposureOnly = false;

  @JsonProperty("washoutPeriod")
  private Integer washoutPeriod = 183;

  @JsonProperty("riskWindowStart")
  private Integer riskWindowStart = 0;

  @JsonProperty("riskWindowEnd")
  private Integer riskWindowEnd = 0;

  @JsonProperty("addExposureDaysToEnd")
  private Boolean addExposureDaysToEnd = true;

  @JsonProperty("firstOutcomeOnly")
  private Boolean firstOutcomeOnly = false;

  @JsonProperty("removePeopleWithPriorOutcomes")
  private Boolean removePeopleWithPriorOutcomes = false;

  @JsonProperty("maxSubjectsForModel")
  private Integer maxSubjectsForModel = 100000;

  @JsonProperty("effectSizes")
  private List<Float> effectSizes = null;

  @JsonProperty("precision")
  private Float precision = 0.01f;

  @JsonProperty("outputIdOffset")
  private Integer outputIdOffset = 1000;

  public PositiveControlSynthesisArgs modelType(ModelTypeEnum modelType) {
    this.modelType = modelType;
    return this;
  }

  /**
   * Can be either \&quot;poisson\&quot; or \&quot;survival\&quot; 
   * @return modelType
   **/
  @JsonProperty("modelType")
  public ModelTypeEnum getModelType() {
    return modelType;
  }

  public void setModelType(ModelTypeEnum modelType) {
    this.modelType = modelType;
  }

  public PositiveControlSynthesisArgs minOutcomeCountForModel(Integer minOutcomeCountForModel) {
    this.minOutcomeCountForModel = minOutcomeCountForModel;
    return this;
  }

  /**
   * Minimum number of outcome events required to build a model. 
   * @return minOutcomeCountForModel
   **/
  @JsonProperty("minOutcomeCountForModel")
  public Integer getMinOutcomeCountForModel() {
    return minOutcomeCountForModel;
  }

  public void setMinOutcomeCountForModel(Integer minOutcomeCountForModel) {
    this.minOutcomeCountForModel = minOutcomeCountForModel;
  }

  public PositiveControlSynthesisArgs minOutcomeCountForInjection(Integer minOutcomeCountForInjection) {
    this.minOutcomeCountForInjection = minOutcomeCountForInjection;
    return this;
  }

  /**
   * Minimum number of outcome events required to inject a signal. 
   * @return minOutcomeCountForInjection
   **/
  @JsonProperty("minOutcomeCountForInjection")
  public Integer getMinOutcomeCountForInjection() {
    return minOutcomeCountForInjection;
  }

  public void setMinOutcomeCountForInjection(Integer minOutcomeCountForInjection) {
    this.minOutcomeCountForInjection = minOutcomeCountForInjection;
  }

  public PositiveControlSynthesisArgs covariateSettings(CovariateSettings covariateSettings) {
    this.covariateSettings = covariateSettings;
    return this;
  }

  /**
   * Get covariateSettings
   * @return covariateSettings
   **/
  @JsonProperty("covariateSettings")
  public CovariateSettings getCovariateSettings() {
    return covariateSettings;
  }

  public void setCovariateSettings(CovariateSettings covariateSettings) {
    this.covariateSettings = covariateSettings;
  }

  public PositiveControlSynthesisArgs prior(Prior prior) {
    this.prior = prior;
    return this;
  }

  /**
   * Get prior
   * @return prior
   **/
  @JsonProperty("prior")
  public Prior getPrior() {
    return prior;
  }

  public void setPrior(Prior prior) {
    this.prior = prior;
  }

  public PositiveControlSynthesisArgs control(Control control) {
    this.control = control;
    return this;
  }

  /**
   * Get control
   * @return control
   **/
  @JsonProperty("control")
  public Control getControl() {
    return control;
  }

  public void setControl(Control control) {
    this.control = control;
  }

  public PositiveControlSynthesisArgs firstExposureOnly(Boolean firstExposureOnly) {
    this.firstExposureOnly = firstExposureOnly;
    return this;
  }

  /**
   * Should signals be injected only for the first exposure? (ie. assuming an acute effect) 
   * @return firstExposureOnly
   **/
  @JsonProperty("firstExposureOnly")
  public Boolean isisFirstExposureOnly() {
    return firstExposureOnly;
  }

  public void setFirstExposureOnly(Boolean firstExposureOnly) {
    this.firstExposureOnly = firstExposureOnly;
  }

  public PositiveControlSynthesisArgs washoutPeriod(Integer washoutPeriod) {
    this.washoutPeriod = washoutPeriod;
    return this;
  }

  /**
   * Number of days at the start of observation for which no signals will be injected, but will be used to determine whether exposure or outcome is the first one, and for extracting covariates to build the outcome model. 
   * @return washoutPeriod
   **/
  @JsonProperty("washoutPeriod")
  public Integer getWashoutPeriod() {
    return washoutPeriod;
  }

  public void setWashoutPeriod(Integer washoutPeriod) {
    this.washoutPeriod = washoutPeriod;
  }

  public PositiveControlSynthesisArgs riskWindowStart(Integer riskWindowStart) {
    this.riskWindowStart = riskWindowStart;
    return this;
  }

  /**
   * The start of the risk window relative to the start of the exposure (in days). When 0, risk is assumed to start on the first day of exposure. 
   * @return riskWindowStart
   **/
  @JsonProperty("riskWindowStart")
  public Integer getRiskWindowStart() {
    return riskWindowStart;
  }

  public void setRiskWindowStart(Integer riskWindowStart) {
    this.riskWindowStart = riskWindowStart;
  }

  public PositiveControlSynthesisArgs riskWindowEnd(Integer riskWindowEnd) {
    this.riskWindowEnd = riskWindowEnd;
    return this;
  }

  /**
   * The end of the risk window relative to the start of the exposure. Note that typically the length of exposure is added to this number (when the addExposureDaysToEnd parameter is set to TRUE). 
   * @return riskWindowEnd
   **/
  @JsonProperty("riskWindowEnd")
  public Integer getRiskWindowEnd() {
    return riskWindowEnd;
  }

  public void setRiskWindowEnd(Integer riskWindowEnd) {
    this.riskWindowEnd = riskWindowEnd;
  }

  public PositiveControlSynthesisArgs addExposureDaysToEnd(Boolean addExposureDaysToEnd) {
    this.addExposureDaysToEnd = addExposureDaysToEnd;
    return this;
  }

  /**
   * Should length of exposure be added to the risk window? 
   * @return addExposureDaysToEnd
   **/
  @JsonProperty("addExposureDaysToEnd")
  public Boolean isisAddExposureDaysToEnd() {
    return addExposureDaysToEnd;
  }

  public void setAddExposureDaysToEnd(Boolean addExposureDaysToEnd) {
    this.addExposureDaysToEnd = addExposureDaysToEnd;
  }

  public PositiveControlSynthesisArgs firstOutcomeOnly(Boolean firstOutcomeOnly) {
    this.firstOutcomeOnly = firstOutcomeOnly;
    return this;
  }

  /**
   * Should only the first outcome per person be considered when modeling the outcome? 
   * @return firstOutcomeOnly
   **/
  @JsonProperty("firstOutcomeOnly")
  public Boolean isisFirstOutcomeOnly() {
    return firstOutcomeOnly;
  }

  public void setFirstOutcomeOnly(Boolean firstOutcomeOnly) {
    this.firstOutcomeOnly = firstOutcomeOnly;
  }

  public PositiveControlSynthesisArgs removePeopleWithPriorOutcomes(Boolean removePeopleWithPriorOutcomes) {
    this.removePeopleWithPriorOutcomes = removePeopleWithPriorOutcomes;
    return this;
  }

  /**
   * Remove people with prior outcomes? 
   * @return removePeopleWithPriorOutcomes
   **/
  @JsonProperty("removePeopleWithPriorOutcomes")
  public Boolean isisRemovePeopleWithPriorOutcomes() {
    return removePeopleWithPriorOutcomes;
  }

  public void setRemovePeopleWithPriorOutcomes(Boolean removePeopleWithPriorOutcomes) {
    this.removePeopleWithPriorOutcomes = removePeopleWithPriorOutcomes;
  }

  public PositiveControlSynthesisArgs maxSubjectsForModel(Integer maxSubjectsForModel) {
    this.maxSubjectsForModel = maxSubjectsForModel;
    return this;
  }

  /**
   * Maximum number of people used to fit an outcome model. 
   * @return maxSubjectsForModel
   **/
  @JsonProperty("maxSubjectsForModel")
  public Integer getMaxSubjectsForModel() {
    return maxSubjectsForModel;
  }

  public void setMaxSubjectsForModel(Integer maxSubjectsForModel) {
    this.maxSubjectsForModel = maxSubjectsForModel;
  }

  public PositiveControlSynthesisArgs effectSizes(List<Float> effectSizes) {
    this.effectSizes = effectSizes;
    return this;
  }

  public PositiveControlSynthesisArgs addEffectSizesItem(Float effectSizesItem) {
    if (this.effectSizes == null) {
      this.effectSizes = new ArrayList<Float>();
    }
    this.effectSizes.add(effectSizesItem);
    return this;
  }

  /**
   * A numeric vector of effect sizes that should be inserted. 
   * @return effectSizes
   **/
  @JsonProperty("effectSizes")
  public List<Float> getEffectSizes() {
    return effectSizes;
  }

  public void setEffectSizes(List<Float> effectSizes) {
    this.effectSizes = effectSizes;
  }

  public PositiveControlSynthesisArgs precision(Float precision) {
    this.precision = precision;
    return this;
  }

  /**
   * The allowed ratio between target and injected signal size. 
   * @return precision
   **/
  @JsonProperty("precision")
  public Float getPrecision() {
    return precision;
  }

  public void setPrecision(Float precision) {
    this.precision = precision;
  }

  public PositiveControlSynthesisArgs outputIdOffset(Integer outputIdOffset) {
    this.outputIdOffset = outputIdOffset;
    return this;
  }

  /**
   * What should be the first new outcome ID that is to be created? 
   * @return outputIdOffset
   **/
  @JsonProperty("outputIdOffset")
  public Integer getOutputIdOffset() {
    return outputIdOffset;
  }

  public void setOutputIdOffset(Integer outputIdOffset) {
    this.outputIdOffset = outputIdOffset;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PositiveControlSynthesisArgs positiveControlSynthesisArgs = (PositiveControlSynthesisArgs) o;
    return Objects.equals(this.modelType, positiveControlSynthesisArgs.modelType) &&
        Objects.equals(this.minOutcomeCountForModel, positiveControlSynthesisArgs.minOutcomeCountForModel) &&
        Objects.equals(this.minOutcomeCountForInjection, positiveControlSynthesisArgs.minOutcomeCountForInjection) &&
        Objects.equals(this.covariateSettings, positiveControlSynthesisArgs.covariateSettings) &&
        Objects.equals(this.prior, positiveControlSynthesisArgs.prior) &&
        Objects.equals(this.control, positiveControlSynthesisArgs.control) &&
        Objects.equals(this.firstExposureOnly, positiveControlSynthesisArgs.firstExposureOnly) &&
        Objects.equals(this.washoutPeriod, positiveControlSynthesisArgs.washoutPeriod) &&
        Objects.equals(this.riskWindowStart, positiveControlSynthesisArgs.riskWindowStart) &&
        Objects.equals(this.riskWindowEnd, positiveControlSynthesisArgs.riskWindowEnd) &&
        Objects.equals(this.addExposureDaysToEnd, positiveControlSynthesisArgs.addExposureDaysToEnd) &&
        Objects.equals(this.firstOutcomeOnly, positiveControlSynthesisArgs.firstOutcomeOnly) &&
        Objects.equals(this.removePeopleWithPriorOutcomes, positiveControlSynthesisArgs.removePeopleWithPriorOutcomes) &&
        Objects.equals(this.maxSubjectsForModel, positiveControlSynthesisArgs.maxSubjectsForModel) &&
        Objects.equals(this.effectSizes, positiveControlSynthesisArgs.effectSizes) &&
        Objects.equals(this.precision, positiveControlSynthesisArgs.precision) &&
        Objects.equals(this.outputIdOffset, positiveControlSynthesisArgs.outputIdOffset);
  }

  @Override
  public int hashCode() {
    return Objects.hash(modelType, minOutcomeCountForModel, minOutcomeCountForInjection, covariateSettings, prior, control, firstExposureOnly, washoutPeriod, riskWindowStart, riskWindowEnd, addExposureDaysToEnd, firstOutcomeOnly, removePeopleWithPriorOutcomes, maxSubjectsForModel, effectSizes, precision, outputIdOffset);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PositiveControlSynthesisArgs {\n");
    
    sb.append("    modelType: ").append(toIndentedString(modelType)).append("\n");
    sb.append("    minOutcomeCountForModel: ").append(toIndentedString(minOutcomeCountForModel)).append("\n");
    sb.append("    minOutcomeCountForInjection: ").append(toIndentedString(minOutcomeCountForInjection)).append("\n");
    sb.append("    covariateSettings: ").append(toIndentedString(covariateSettings)).append("\n");
    sb.append("    prior: ").append(toIndentedString(prior)).append("\n");
    sb.append("    control: ").append(toIndentedString(control)).append("\n");
    sb.append("    firstExposureOnly: ").append(toIndentedString(firstExposureOnly)).append("\n");
    sb.append("    washoutPeriod: ").append(toIndentedString(washoutPeriod)).append("\n");
    sb.append("    riskWindowStart: ").append(toIndentedString(riskWindowStart)).append("\n");
    sb.append("    riskWindowEnd: ").append(toIndentedString(riskWindowEnd)).append("\n");
    sb.append("    addExposureDaysToEnd: ").append(toIndentedString(addExposureDaysToEnd)).append("\n");
    sb.append("    firstOutcomeOnly: ").append(toIndentedString(firstOutcomeOnly)).append("\n");
    sb.append("    removePeopleWithPriorOutcomes: ").append(toIndentedString(removePeopleWithPriorOutcomes)).append("\n");
    sb.append("    maxSubjectsForModel: ").append(toIndentedString(maxSubjectsForModel)).append("\n");
    sb.append("    effectSizes: ").append(toIndentedString(effectSizes)).append("\n");
    sb.append("    precision: ").append(toIndentedString(precision)).append("\n");
    sb.append("    outputIdOffset: ").append(toIndentedString(outputIdOffset)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }    
}
