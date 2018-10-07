package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CohortMethodAnalysis
 */
public class CohortMethodAnalysis extends Analysis {
  @JsonProperty("targetType")
  private String targetType = null;

  @JsonProperty("comparatorType")
  private String comparatorType = null;

  @JsonProperty("getDbCohortMethodDataArgs")
  private GetDbCohortMethodDataArgs getDbCohortMethodDataArgs = null;

  @JsonProperty("createStudyPopArgs")
  private CreateStudyPopulationArgs createStudyPopArgs = null;

  @JsonProperty("createPs")
  private Boolean createPs = false;

  @JsonProperty("createPsArgs")
  private CreatePsArgs createPsArgs = null;

  @JsonProperty("trimByPs")
  private Boolean trimByPs = false;

  @JsonProperty("trimByPsArgs")
  private TrimByPsArgs trimByPsArgs = null;

  @JsonProperty("trimByPsToEquipoise")
  private Boolean trimByPsToEquipoise = false;

  @JsonProperty("trimByPsToEquipoiseArgs")
  private TrimByPsToEquipoiseArgs trimByPsToEquipoiseArgs = null;

  @JsonProperty("matchOnPs")
  private Boolean matchOnPs = false;

  @JsonProperty("matchOnPsArgs")
  private MatchOnPsArgs matchOnPsArgs = null;

  @JsonProperty("matchOnPsAndCovariates")
  private Boolean matchOnPsAndCovariates = false;

  @JsonProperty("matchOnPsAndCovariatesArgs")
  private MatchOnPsAndCovariatesArgs matchOnPsAndCovariatesArgs = null;

  @JsonProperty("stratifyByPs")
  private Boolean stratifyByPs = false;

  @JsonProperty("stratifyByPsArgs")
  private StratifyByPsArgs stratifyByPsArgs = null;

  @JsonProperty("stratifyByPsAndCovariates")
  private Boolean stratifyByPsAndCovariates = false;

  @JsonProperty("stratifyByPsAndCovariatesArgs")
  private StratifyByPsAndCovariatesArgs stratifyByPsAndCovariatesArgs = null;

  @JsonProperty("fitOutcomeModel")
  private Boolean fitOutcomeModel = false;

  @JsonProperty("fitOutcomeModelArgs")
  private FitOutcomeModelArgs fitOutcomeModelArgs = null;

  @JsonProperty("attr_class")
  private String attrClass = "cmAnalysis";

  public CohortMethodAnalysis targetType(String targetType) {
    this.targetType = targetType;
    return this;
  }

  /**
   * If more than one target is provided for each drugComparatorOutcome, this field should be used to select the specific target to use in this analysis. 
   * @return targetType
   **/
  @JsonProperty("targetType")
  public String getTargetType() {
    return targetType;
  }

  public void setTargetType(String targetType) {
    this.targetType = targetType;
  }

  public CohortMethodAnalysis comparatorType(String comparatorType) {
    this.comparatorType = comparatorType;
    return this;
  }

  /**
   * If more than one comparator is provided for each drugComparatorOutcome, this field should be used to select the specific comparator to use in this analysis. 
   * @return comparatorType
   **/
  @JsonProperty("comparatorType")
  public String getComparatorType() {
    return comparatorType;
  }

  public void setComparatorType(String comparatorType) {
    this.comparatorType = comparatorType;
  }

  public CohortMethodAnalysis getDbCohortMethodDataArgs(GetDbCohortMethodDataArgs getDbCohortMethodDataArgs) {
    this.getDbCohortMethodDataArgs = getDbCohortMethodDataArgs;
    return this;
  }

  /**
   * Get getDbCohortMethodDataArgs
   * @return getDbCohortMethodDataArgs
   **/
  @JsonProperty("getDbCohortMethodDataArgs")
  public GetDbCohortMethodDataArgs getGetDbCohortMethodDataArgs() {
    return getDbCohortMethodDataArgs;
  }

  public void setGetDbCohortMethodDataArgs(GetDbCohortMethodDataArgs getDbCohortMethodDataArgs) {
    this.getDbCohortMethodDataArgs = getDbCohortMethodDataArgs;
  }

  public CohortMethodAnalysis createStudyPopArgs(CreateStudyPopulationArgs createStudyPopArgs) {
    this.createStudyPopArgs = createStudyPopArgs;
    return this;
  }

  /**
   * Get createStudyPopArgs
   * @return createStudyPopArgs
   **/
  @JsonProperty("createStudyPopArgs")
  public CreateStudyPopulationArgs getCreateStudyPopArgs() {
    return createStudyPopArgs;
  }

  public void setCreateStudyPopArgs(CreateStudyPopulationArgs createStudyPopArgs) {
    this.createStudyPopArgs = createStudyPopArgs;
  }

  public CohortMethodAnalysis createPs(Boolean createPs) {
    this.createPs = createPs;
    return this;
  }

  /**
   * Should the createPs function be used in this analysis? 
   * @return createPs
   **/
  @JsonProperty("createPs")
  public Boolean isisCreatePs() {
    return createPs;
  }

  public void setCreatePs(Boolean createPs) {
    this.createPs = createPs;
  }

  public CohortMethodAnalysis createPsArgs(CreatePsArgs createPsArgs) {
    this.createPsArgs = createPsArgs;
    return this;
  }

  /**
   * Get createPsArgs
   * @return createPsArgs
   **/
  @JsonProperty("createPsArgs")
  public CreatePsArgs getCreatePsArgs() {
    return createPsArgs;
  }

  public void setCreatePsArgs(CreatePsArgs createPsArgs) {
    this.createPsArgs = createPsArgs;
  }

  public CohortMethodAnalysis trimByPs(Boolean trimByPs) {
    this.trimByPs = trimByPs;
    return this;
  }

  /**
   * Should the trimByPs function be used in this analysis? 
   * @return trimByPs
   **/
  @JsonProperty("trimByPs")
  public Boolean isisTrimByPs() {
    return trimByPs;
  }

  public void setTrimByPs(Boolean trimByPs) {
    this.trimByPs = trimByPs;
  }

  public CohortMethodAnalysis trimByPsArgs(TrimByPsArgs trimByPsArgs) {
    this.trimByPsArgs = trimByPsArgs;
    return this;
  }

  /**
   * Get trimByPsArgs
   * @return trimByPsArgs
   **/
  @JsonProperty("trimByPsArgs")
  public TrimByPsArgs getTrimByPsArgs() {
    return trimByPsArgs;
  }

  public void setTrimByPsArgs(TrimByPsArgs trimByPsArgs) {
    this.trimByPsArgs = trimByPsArgs;
  }

  public CohortMethodAnalysis trimByPsToEquipoise(Boolean trimByPsToEquipoise) {
    this.trimByPsToEquipoise = trimByPsToEquipoise;
    return this;
  }

  /**
   * Should the trimByPsToEquipoise function be used in this analysis? 
   * @return trimByPsToEquipoise
   **/
  @JsonProperty("trimByPsToEquipoise")
  public Boolean isisTrimByPsToEquipoise() {
    return trimByPsToEquipoise;
  }

  public void setTrimByPsToEquipoise(Boolean trimByPsToEquipoise) {
    this.trimByPsToEquipoise = trimByPsToEquipoise;
  }

  public CohortMethodAnalysis trimByPsToEquipoiseArgs(TrimByPsToEquipoiseArgs trimByPsToEquipoiseArgs) {
    this.trimByPsToEquipoiseArgs = trimByPsToEquipoiseArgs;
    return this;
  }

  /**
   * Get trimByPsToEquipoiseArgs
   * @return trimByPsToEquipoiseArgs
   **/
  @JsonProperty("trimByPsToEquipoiseArgs")
  public TrimByPsToEquipoiseArgs getTrimByPsToEquipoiseArgs() {
    return trimByPsToEquipoiseArgs;
  }

  public void setTrimByPsToEquipoiseArgs(TrimByPsToEquipoiseArgs trimByPsToEquipoiseArgs) {
    this.trimByPsToEquipoiseArgs = trimByPsToEquipoiseArgs;
  }

  public CohortMethodAnalysis matchOnPs(Boolean matchOnPs) {
    this.matchOnPs = matchOnPs;
    return this;
  }

  /**
   * Should the matchOnPsAndCovariates function be used in this analysis? 
   * @return matchOnPs
   **/
  @JsonProperty("matchOnPs")
  public Boolean isisMatchOnPs() {
    return matchOnPs;
  }

  public void setMatchOnPs(Boolean matchOnPs) {
    this.matchOnPs = matchOnPs;
  }

  public CohortMethodAnalysis matchOnPsArgs(MatchOnPsArgs matchOnPsArgs) {
    this.matchOnPsArgs = matchOnPsArgs;
    return this;
  }

  /**
   * Get matchOnPsArgs
   * @return matchOnPsArgs
   **/
  @JsonProperty("matchOnPsArgs")
  public MatchOnPsArgs getMatchOnPsArgs() {
    return matchOnPsArgs;
  }

  public void setMatchOnPsArgs(MatchOnPsArgs matchOnPsArgs) {
    this.matchOnPsArgs = matchOnPsArgs;
  }

  public CohortMethodAnalysis matchOnPsAndCovariates(Boolean matchOnPsAndCovariates) {
    this.matchOnPsAndCovariates = matchOnPsAndCovariates;
    return this;
  }

  /**
   * Should the matchOnPsAndCovariates function be used in this analysis? 
   * @return matchOnPsAndCovariates
   **/
  @JsonProperty("matchOnPsAndCovariates")
  public Boolean isisMatchOnPsAndCovariates() {
    return matchOnPsAndCovariates;
  }

  public void setMatchOnPsAndCovariates(Boolean matchOnPsAndCovariates) {
    this.matchOnPsAndCovariates = matchOnPsAndCovariates;
  }

  public CohortMethodAnalysis matchOnPsAndCovariatesArgs(MatchOnPsAndCovariatesArgs matchOnPsAndCovariatesArgs) {
    this.matchOnPsAndCovariatesArgs = matchOnPsAndCovariatesArgs;
    return this;
  }

  /**
   * Get matchOnPsAndCovariatesArgs
   * @return matchOnPsAndCovariatesArgs
   **/
  @JsonProperty("matchOnPsAndCovariatesArgs")
  public MatchOnPsAndCovariatesArgs getMatchOnPsAndCovariatesArgs() {
    return matchOnPsAndCovariatesArgs;
  }

  public void setMatchOnPsAndCovariatesArgs(MatchOnPsAndCovariatesArgs matchOnPsAndCovariatesArgs) {
    this.matchOnPsAndCovariatesArgs = matchOnPsAndCovariatesArgs;
  }

  public CohortMethodAnalysis stratifyByPs(Boolean stratifyByPs) {
    this.stratifyByPs = stratifyByPs;
    return this;
  }

  /**
   * Should the stratifyByPs function be used in this analysis? 
   * @return stratifyByPs
   **/
  @JsonProperty("stratifyByPs")
  public Boolean isisStratifyByPs() {
    return stratifyByPs;
  }

  public void setStratifyByPs(Boolean stratifyByPs) {
    this.stratifyByPs = stratifyByPs;
  }

  public CohortMethodAnalysis stratifyByPsArgs(StratifyByPsArgs stratifyByPsArgs) {
    this.stratifyByPsArgs = stratifyByPsArgs;
    return this;
  }

  /**
   * Get stratifyByPsArgs
   * @return stratifyByPsArgs
   **/
  @JsonProperty("stratifyByPsArgs")
  public StratifyByPsArgs getStratifyByPsArgs() {
    return stratifyByPsArgs;
  }

  public void setStratifyByPsArgs(StratifyByPsArgs stratifyByPsArgs) {
    this.stratifyByPsArgs = stratifyByPsArgs;
  }

  public CohortMethodAnalysis stratifyByPsAndCovariates(Boolean stratifyByPsAndCovariates) {
    this.stratifyByPsAndCovariates = stratifyByPsAndCovariates;
    return this;
  }

  /**
   * Should the stratifyByPsAndCovariates function be used in this analysis? 
   * @return stratifyByPsAndCovariates
   **/
  @JsonProperty("stratifyByPsAndCovariates")
  public Boolean isisStratifyByPsAndCovariates() {
    return stratifyByPsAndCovariates;
  }

  public void setStratifyByPsAndCovariates(Boolean stratifyByPsAndCovariates) {
    this.stratifyByPsAndCovariates = stratifyByPsAndCovariates;
  }

  public CohortMethodAnalysis stratifyByPsAndCovariatesArgs(StratifyByPsAndCovariatesArgs stratifyByPsAndCovariatesArgs) {
    this.stratifyByPsAndCovariatesArgs = stratifyByPsAndCovariatesArgs;
    return this;
  }

  /**
   * Get stratifyByPsAndCovariatesArgs
   * @return stratifyByPsAndCovariatesArgs
   **/
  @JsonProperty("stratifyByPsAndCovariatesArgs")
  public StratifyByPsAndCovariatesArgs getStratifyByPsAndCovariatesArgs() {
    return stratifyByPsAndCovariatesArgs;
  }

  public void setStratifyByPsAndCovariatesArgs(StratifyByPsAndCovariatesArgs stratifyByPsAndCovariatesArgs) {
    this.stratifyByPsAndCovariatesArgs = stratifyByPsAndCovariatesArgs;
  }

  public CohortMethodAnalysis fitOutcomeModel(Boolean fitOutcomeModel) {
    this.fitOutcomeModel = fitOutcomeModel;
    return this;
  }

  /**
   * Should the fitOutcomeModel function be used in this analysis? 
   * @return fitOutcomeModel
   **/
  @JsonProperty("fitOutcomeModel")
  public Boolean isisFitOutcomeModel() {
    return fitOutcomeModel;
  }

  public void setFitOutcomeModel(Boolean fitOutcomeModel) {
    this.fitOutcomeModel = fitOutcomeModel;
  }

  public CohortMethodAnalysis fitOutcomeModelArgs(FitOutcomeModelArgs fitOutcomeModelArgs) {
    this.fitOutcomeModelArgs = fitOutcomeModelArgs;
    return this;
  }

  /**
   * Get fitOutcomeModelArgs
   * @return fitOutcomeModelArgs
   **/
  @JsonProperty("fitOutcomeModelArgs")
  public FitOutcomeModelArgs getFitOutcomeModelArgs() {
    return fitOutcomeModelArgs;
  }

  public void setFitOutcomeModelArgs(FitOutcomeModelArgs fitOutcomeModelArgs) {
    this.fitOutcomeModelArgs = fitOutcomeModelArgs;
  }

  public CohortMethodAnalysis attrClass(String attrClass) {
    this.attrClass = attrClass;
    return this;
  }

  /**
   * Get attrClass
   * @return attrClass
   **/
  @JsonProperty("attr_class")
  public String getAttrClass() {
    return attrClass;
  }

  public void setAttrClass(String attrClass) {
    this.attrClass = attrClass;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CohortMethodAnalysis cohortMethodAnalysis = (CohortMethodAnalysis) o;
    return Objects.equals(this.targetType, cohortMethodAnalysis.targetType) &&
        Objects.equals(this.comparatorType, cohortMethodAnalysis.comparatorType) &&
        Objects.equals(this.getDbCohortMethodDataArgs, cohortMethodAnalysis.getDbCohortMethodDataArgs) &&
        Objects.equals(this.createStudyPopArgs, cohortMethodAnalysis.createStudyPopArgs) &&
        Objects.equals(this.createPs, cohortMethodAnalysis.createPs) &&
        Objects.equals(this.createPsArgs, cohortMethodAnalysis.createPsArgs) &&
        Objects.equals(this.trimByPs, cohortMethodAnalysis.trimByPs) &&
        Objects.equals(this.trimByPsArgs, cohortMethodAnalysis.trimByPsArgs) &&
        Objects.equals(this.trimByPsToEquipoise, cohortMethodAnalysis.trimByPsToEquipoise) &&
        Objects.equals(this.trimByPsToEquipoiseArgs, cohortMethodAnalysis.trimByPsToEquipoiseArgs) &&
        Objects.equals(this.matchOnPs, cohortMethodAnalysis.matchOnPs) &&
        Objects.equals(this.matchOnPsArgs, cohortMethodAnalysis.matchOnPsArgs) &&
        Objects.equals(this.matchOnPsAndCovariates, cohortMethodAnalysis.matchOnPsAndCovariates) &&
        Objects.equals(this.matchOnPsAndCovariatesArgs, cohortMethodAnalysis.matchOnPsAndCovariatesArgs) &&
        Objects.equals(this.stratifyByPs, cohortMethodAnalysis.stratifyByPs) &&
        Objects.equals(this.stratifyByPsArgs, cohortMethodAnalysis.stratifyByPsArgs) &&
        Objects.equals(this.stratifyByPsAndCovariates, cohortMethodAnalysis.stratifyByPsAndCovariates) &&
        Objects.equals(this.stratifyByPsAndCovariatesArgs, cohortMethodAnalysis.stratifyByPsAndCovariatesArgs) &&
        Objects.equals(this.fitOutcomeModel, cohortMethodAnalysis.fitOutcomeModel) &&
        Objects.equals(this.fitOutcomeModelArgs, cohortMethodAnalysis.fitOutcomeModelArgs) &&
        Objects.equals(this.attrClass, cohortMethodAnalysis.attrClass) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetType, comparatorType, getDbCohortMethodDataArgs, createStudyPopArgs, createPs, createPsArgs, trimByPs, trimByPsArgs, trimByPsToEquipoise, trimByPsToEquipoiseArgs, matchOnPs, matchOnPsArgs, matchOnPsAndCovariates, matchOnPsAndCovariatesArgs, stratifyByPs, stratifyByPsArgs, stratifyByPsAndCovariates, stratifyByPsAndCovariatesArgs, fitOutcomeModel, fitOutcomeModelArgs, attrClass, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CohortMethodAnalysis {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    targetType: ").append(toIndentedString(targetType)).append("\n");
    sb.append("    comparatorType: ").append(toIndentedString(comparatorType)).append("\n");
    sb.append("    getDbCohortMethodDataArgs: ").append(toIndentedString(getDbCohortMethodDataArgs)).append("\n");
    sb.append("    createStudyPopArgs: ").append(toIndentedString(createStudyPopArgs)).append("\n");
    sb.append("    createPs: ").append(toIndentedString(createPs)).append("\n");
    sb.append("    createPsArgs: ").append(toIndentedString(createPsArgs)).append("\n");
    sb.append("    trimByPs: ").append(toIndentedString(trimByPs)).append("\n");
    sb.append("    trimByPsArgs: ").append(toIndentedString(trimByPsArgs)).append("\n");
    sb.append("    trimByPsToEquipoise: ").append(toIndentedString(trimByPsToEquipoise)).append("\n");
    sb.append("    trimByPsToEquipoiseArgs: ").append(toIndentedString(trimByPsToEquipoiseArgs)).append("\n");
    sb.append("    matchOnPs: ").append(toIndentedString(matchOnPs)).append("\n");
    sb.append("    matchOnPsArgs: ").append(toIndentedString(matchOnPsArgs)).append("\n");
    sb.append("    matchOnPsAndCovariates: ").append(toIndentedString(matchOnPsAndCovariates)).append("\n");
    sb.append("    matchOnPsAndCovariatesArgs: ").append(toIndentedString(matchOnPsAndCovariatesArgs)).append("\n");
    sb.append("    stratifyByPs: ").append(toIndentedString(stratifyByPs)).append("\n");
    sb.append("    stratifyByPsArgs: ").append(toIndentedString(stratifyByPsArgs)).append("\n");
    sb.append("    stratifyByPsAndCovariates: ").append(toIndentedString(stratifyByPsAndCovariates)).append("\n");
    sb.append("    stratifyByPsAndCovariatesArgs: ").append(toIndentedString(stratifyByPsAndCovariatesArgs)).append("\n");
    sb.append("    fitOutcomeModel: ").append(toIndentedString(fitOutcomeModel)).append("\n");
    sb.append("    fitOutcomeModelArgs: ").append(toIndentedString(fitOutcomeModelArgs)).append("\n");
    sb.append("    attrClass: ").append(toIndentedString(attrClass)).append("\n");
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
