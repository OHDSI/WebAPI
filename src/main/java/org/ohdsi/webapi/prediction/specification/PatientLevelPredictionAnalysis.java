package org.ohdsi.webapi.prediction.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.ohdsi.webapi.featureextraction.specification.CovariateSettings;

@JsonIgnoreProperties(ignoreUnknown=true)
public class PatientLevelPredictionAnalysis {
  @JsonProperty("id")
  private Integer id = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("version")
  private String version = null;

  @JsonProperty("organizationName")
  private String organizationName = null;

  @JsonProperty("packageName")
  private String packageName = null;

  /**
   * The base skeleton R package 
   */
  public enum SkeletonTypeEnum {
    PATIENTLEVELPREDICTIONSTUDY("PatientLevelPredictionStudy");

    private String value;

    SkeletonTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SkeletonTypeEnum fromValue(String text) {
      for (SkeletonTypeEnum b : SkeletonTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("skeletonType")
  private SkeletonTypeEnum skeletonType = SkeletonTypeEnum.PATIENTLEVELPREDICTIONSTUDY;

  @JsonProperty("skeletonVersion")
  private String skeletonVersion = "v0.0.1";

  @JsonProperty("createdBy")
  private String createdBy = null;

  @JsonProperty("createdDate")
  private String createdDate = null;

  @JsonProperty("modifiedBy")
  private String modifiedBy = null;

  @JsonProperty("modifiedDate")
  private String modifiedDate = null;

  @JsonProperty("cohortDefinitions")
  private List<PredictionCohortDefinition> cohortDefinitions = null;

  @JsonProperty("conceptSets")
  private List<PredictionConceptSet> conceptSets = null;

  @JsonProperty("conceptSetCrossReference")
  private List<ConceptSetCrossReference> conceptSetCrossReference = null;

  @JsonProperty("targetIds")
  private List<BigDecimal> targetIds = null;

  @JsonProperty("outcomeIds")
  private List<BigDecimal> outcomeIds = null;

  @JsonProperty("covariateSettings")
  private List<CovariateSettings> covariateSettings = null;

  @JsonProperty("populationSettings")
  private List<CreateStudyPopulationArgs> populationSettings = null;

  @JsonProperty("modelSettings")
  private List<Object> modelSettings = null;

  @JsonProperty("getPlpDataArgs")
  private GetDbPLPDataArgs getPlpDataArgs = null;

  @JsonProperty("runPlpArgs")
  private RunPlpArgs runPlpArgs = null;

  public PatientLevelPredictionAnalysis id(Integer id) {
    this.id = id;
    return this;
  }

  /**
   * Identifier for the PLP specification
   * @return id
   **/
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PatientLevelPredictionAnalysis name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Name for the PLP specification
   * @return name
   **/
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PatientLevelPredictionAnalysis description(String description) {
    this.description = description;
    return this;
  }

  /**
   * The description of the study
   * @return description
   **/
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public PatientLevelPredictionAnalysis version(String version) {
    this.version = version;
    return this;
  }

  /**
   * Version number of the specification for use by the hydration package
   * @return version
   **/
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public PatientLevelPredictionAnalysis organizationName(String organizationName) {
    this.organizationName = organizationName;
    return this;
  }
  /**
   * The organization that produced the specification
   * @return organizationName
   **/
  @JsonProperty("organizationName")
  public String getOrganizationName() {
    return organizationName;
  }

  public void setOrganizationName(String organizationName) {
    this.organizationName = organizationName;
  }

  public PatientLevelPredictionAnalysis packageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  /**
   * The name of the R Package for execution
   * @return packageName
   **/
  @JsonProperty("packageName")
  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public PatientLevelPredictionAnalysis skeletonType(SkeletonTypeEnum skeletonType) {
    this.skeletonType = skeletonType;
    return this;
  }

  /**
   * The base skeleton R package 
   * @return skeletonType
   **/
  @JsonProperty("skeletonType")
  public SkeletonTypeEnum getSkeletonType() {
    return skeletonType;
  }

  public void setSkeletonType(SkeletonTypeEnum skeletonType) {
    this.skeletonType = skeletonType;
  }

  public PatientLevelPredictionAnalysis skeletonVersion(String skeletonVersion) {
    this.skeletonVersion = skeletonVersion;
    return this;
  }

  /**
   * The cooresponding skelecton version to use
   * @return skeletonVersion
   **/
  @JsonProperty("skeletonVersion")
  public String getSkeletonVersion() {
    return skeletonVersion;
  }

  public void setSkeletonVersion(String skeletonVersion) {
    this.skeletonVersion = skeletonVersion;
  }

  public PatientLevelPredictionAnalysis createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  /**
   * The person who created the analysis 
   * @return createdBy
   **/
  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public PatientLevelPredictionAnalysis createdDate(String createdDate) {
    this.createdDate = createdDate;
    return this;
  }

  /**
   * The date and time the estimation was first saved 
   * @return createdDate
   **/
  @JsonProperty("createdDate")
  public String getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(String createdDate) {
    this.createdDate = createdDate;
  }

  public PatientLevelPredictionAnalysis modifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
    return this;
  }

  /**
   * The last person to modify the analysis 
   * @return modifiedBy
   **/
  @JsonProperty("modifiedBy")
  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public PatientLevelPredictionAnalysis modifiedDate(String modifiedDate) {
    this.modifiedDate = modifiedDate;
    return this;
  }

  /**
   * The date and time the estimation was last saved 
   * @return modifiedDate
   **/
  @JsonProperty("modifiedDate")
  public String getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate(String modifiedDate) {
    this.modifiedDate = modifiedDate;
  }
  
  public PatientLevelPredictionAnalysis cohortDefinitions(List<PredictionCohortDefinition> cohortDefinitions) {
    this.cohortDefinitions = cohortDefinitions;
    return this;
  }

  public PatientLevelPredictionAnalysis addCohortDefinitionsItem(PredictionCohortDefinition cohortDefinitionsItem) {
    if (this.cohortDefinitions == null) {
      this.cohortDefinitions = new ArrayList<PredictionCohortDefinition>();
    }
    this.cohortDefinitions.add(cohortDefinitionsItem);
    return this;
  }

  /**
   * Get cohortDefinitions
   * @return cohortDefinitions
   **/
  @JsonProperty("cohortDefinitions")
  public List<PredictionCohortDefinition> getCohortDefinitions() {
    return cohortDefinitions;
  }

  public void setCohortDefinitions(List<PredictionCohortDefinition> cohortDefinitions) {
    this.cohortDefinitions = cohortDefinitions;
  }
  
  public PatientLevelPredictionAnalysis conceptSets(List<PredictionConceptSet> conceptSets) {
    this.conceptSets = conceptSets;
    return this;
  }

  public PatientLevelPredictionAnalysis addConceptSetsItem(PredictionConceptSet conceptSetsItem) {
    if (this.conceptSets == null) {
      this.conceptSets = new ArrayList<PredictionConceptSet>();
    }
    this.conceptSets.add(conceptSetsItem);
    return this;
  }

  /**
   * Get conceptSets
   * @return conceptSets
   **/
  @JsonProperty("conceptSets")
  public List<PredictionConceptSet> getConceptSets() {
    return conceptSets;
  }

  public void setConceptSets(List<PredictionConceptSet> conceptSets) {
    this.conceptSets = conceptSets;
  }
  
  /**
   * Get conceptSetCrossReference
   * @return conceptSetCrossReference
   **/
  @JsonProperty("conceptSetCrossReference")
  public List<ConceptSetCrossReference> getConceptSetCrossReference() {
    return conceptSetCrossReference;
  }

  public void setConceptSetCrossReference(List<ConceptSetCrossReference> conceptSetCrossReference) {
    this.conceptSetCrossReference = conceptSetCrossReference;
  }

  public PatientLevelPredictionAnalysis targetIds(List<BigDecimal> targetIds) {
    this.targetIds = targetIds;
    return this;
  }

  public PatientLevelPredictionAnalysis addTargetIdsItem(BigDecimal targetIdsItem) {
    if (this.targetIds == null) {
      this.targetIds = new ArrayList<BigDecimal>();
    }
    this.targetIds.add(targetIdsItem);
    return this;
  }

  /**
   * A list of the cohort IDs from the cohortDefinition collection for use as targets in the prediction 
   * @return targetIds
   **/
  @JsonProperty("targetIds")
  public List<BigDecimal> getTargetIds() {
    return targetIds;
  }

  public void setTargetIds(List<BigDecimal> targetIds) {
    this.targetIds = targetIds;
  }


  public PatientLevelPredictionAnalysis outcomeIds(List<BigDecimal> outcomeIds) {
    this.outcomeIds = outcomeIds;
    return this;
  }

  public PatientLevelPredictionAnalysis addOutcomeIdsItem(BigDecimal outcomeIdsItem) {
    if (this.outcomeIds == null) {
      this.outcomeIds = new ArrayList<BigDecimal>();
    }
    this.outcomeIds.add(outcomeIdsItem);
    return this;
  }

  /**
   * A list of the cohort IDs from the cohortDefinition collection for use as outcomes in the prediction 
   * @return outcomeIds
   **/
  @JsonProperty("outcomeIds")
  public List<BigDecimal> getOutcomeIds() {
    return outcomeIds;
  }

  public void setOutcomeIds(List<BigDecimal> outcomeIds) {
    this.outcomeIds = outcomeIds;
  }

  public PatientLevelPredictionAnalysis covariateSettings(List<CovariateSettings> covariateSettings) {
    this.covariateSettings = covariateSettings;
    return this;
  }

  public PatientLevelPredictionAnalysis addCovariateSettingsItem(CovariateSettings covariateSettingsItem) {
    if (this.covariateSettings == null) {
      this.covariateSettings = new ArrayList<CovariateSettings>();
    }
    this.covariateSettings.add(covariateSettingsItem);
    return this;
  }

  /**
   * Get covariateSettings
   * @return covariateSettings
   **/
  @JsonProperty("covariateSettings")
  public List<CovariateSettings> getCovariateSettings() {
    return covariateSettings;
  }

  public void setCovariateSettings(List<CovariateSettings> covariateSettings) {
    this.covariateSettings = covariateSettings;
  }

  public PatientLevelPredictionAnalysis populationSettings(List<CreateStudyPopulationArgs> populationSettings) {
    this.populationSettings = populationSettings;
    return this;
  }

  public PatientLevelPredictionAnalysis addPopulationSettingsItem(CreateStudyPopulationArgs populationSettingsItem) {
    if (this.populationSettings == null) {
      this.populationSettings = new ArrayList<CreateStudyPopulationArgs>();
    }
    this.populationSettings.add(populationSettingsItem);
    return this;
  }

  /**
   * Get populationSettings
   * @return populationSettings
   **/
  @JsonProperty("populationSettings")
  public List<CreateStudyPopulationArgs> getPopulationSettings() {
    return populationSettings;
  }

  public void setPopulationSettings(List<CreateStudyPopulationArgs> populationSettings) {
    this.populationSettings = populationSettings;
  }

  public PatientLevelPredictionAnalysis modelSettings(List<Object> modelSettings) {
    this.modelSettings = modelSettings;
    return this;
  }

  public PatientLevelPredictionAnalysis addModelSettingsItem(Object modelSettingsItem) {
    if (this.modelSettings == null) {
      this.modelSettings = new ArrayList<Object>();
    }
    this.modelSettings.add(modelSettingsItem);
    return this;
  }

  /**
   * Get modelSettings
   * @return modelSettings
   **/
  @JsonProperty("modelSettings")
  public List<Object> getModelSettings() {
    return modelSettings;
  }

  public void setModelSettings(List<Object> modelSettings) {
    this.modelSettings = modelSettings;
  }

  public PatientLevelPredictionAnalysis getPlpDataArgs(GetDbPLPDataArgs getPlpDataArgs) {
    this.getPlpDataArgs = getPlpDataArgs;
    return this;
  }

  /**
   * Get getPlpDataArgs
   * @return getPlpDataArgs
   **/
  @JsonProperty("getPlpDataArgs")
  public GetDbPLPDataArgs getGetPlpDataArgs() {
    return getPlpDataArgs;
  }

  public void setGetPlpDataArgs(GetDbPLPDataArgs getPlpDataArgs) {
    this.getPlpDataArgs = getPlpDataArgs;
  }

  public PatientLevelPredictionAnalysis runPlpArgs(RunPlpArgs runPlpArgs) {
    this.runPlpArgs = runPlpArgs;
    return this;
  }

  /**
   * Get runPlpArgs
   * @return runPlpArgs
   **/
  @JsonProperty("runPlpArgs")
  public RunPlpArgs getRunPlpArgs() {
    return runPlpArgs;
  }

  public void setRunPlpArgs(RunPlpArgs runPlpArgs) {
    this.runPlpArgs = runPlpArgs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatientLevelPredictionAnalysis patientLevelPredictionAnalysis = (PatientLevelPredictionAnalysis) o;
    return Objects.equals(this.id, patientLevelPredictionAnalysis.id) &&
        Objects.equals(this.name, patientLevelPredictionAnalysis.name) &&
        Objects.equals(this.description, patientLevelPredictionAnalysis.description) &&
        Objects.equals(this.version, patientLevelPredictionAnalysis.version) &&
        Objects.equals(this.organizationName, patientLevelPredictionAnalysis.organizationName) &&
        Objects.equals(this.packageName, patientLevelPredictionAnalysis.packageName) &&
        Objects.equals(this.skeletonType, patientLevelPredictionAnalysis.skeletonType) &&
        Objects.equals(this.skeletonVersion, patientLevelPredictionAnalysis.skeletonVersion) &&
        Objects.equals(this.createdBy, patientLevelPredictionAnalysis.createdBy) &&
        Objects.equals(this.createdDate, patientLevelPredictionAnalysis.createdDate) &&
        Objects.equals(this.modifiedBy, patientLevelPredictionAnalysis.modifiedBy) &&
        Objects.equals(this.modifiedDate, patientLevelPredictionAnalysis.modifiedDate) &&
        Objects.equals(this.cohortDefinitions, patientLevelPredictionAnalysis.cohortDefinitions) &&
        Objects.equals(this.conceptSets, patientLevelPredictionAnalysis.conceptSets) &&
        Objects.equals(this.conceptSetCrossReference, patientLevelPredictionAnalysis.conceptSetCrossReference) &&
        Objects.equals(this.outcomeIds, patientLevelPredictionAnalysis.outcomeIds) &&
        Objects.equals(this.covariateSettings, patientLevelPredictionAnalysis.covariateSettings) &&
        Objects.equals(this.populationSettings, patientLevelPredictionAnalysis.populationSettings) &&
        Objects.equals(this.modelSettings, patientLevelPredictionAnalysis.modelSettings) &&
        Objects.equals(this.getPlpDataArgs, patientLevelPredictionAnalysis.getPlpDataArgs) &&
        Objects.equals(this.runPlpArgs, patientLevelPredictionAnalysis.runPlpArgs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, version, organizationName, packageName, skeletonType, skeletonVersion, createdBy, createdDate, modifiedBy, modifiedDate, cohortDefinitions, conceptSets, conceptSetCrossReference, targetIds, outcomeIds, covariateSettings, populationSettings, modelSettings, getPlpDataArgs, runPlpArgs);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PatientLevelPredictionAnalysis {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    organizationName: ").append(toIndentedString(organizationName)).append("\n");
    sb.append("    packageName: ").append(toIndentedString(packageName)).append("\n");
    sb.append("    skeletonType: ").append(toIndentedString(skeletonType)).append("\n");
    sb.append("    skeletonVersion: ").append(toIndentedString(skeletonVersion)).append("\n");
    sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
    sb.append("    createdDate: ").append(toIndentedString(createdDate)).append("\n");
    sb.append("    modifiedBy: ").append(toIndentedString(modifiedBy)).append("\n");
    sb.append("    modifiedDate: ").append(toIndentedString(modifiedDate)).append("\n");
    sb.append("    cohortDefinitions: ").append(toIndentedString(cohortDefinitions)).append("\n");
    sb.append("    conceptSets: ").append(toIndentedString(conceptSets)).append("\n");
    sb.append("    conceptSetCrossReference: ").append(toIndentedString(conceptSetCrossReference)).append("\n");
    sb.append("    targetIds: ").append(toIndentedString(targetIds)).append("\n");
    sb.append("    outcomeIds: ").append(toIndentedString(outcomeIds)).append("\n");
    sb.append("    covariateSettings: ").append(toIndentedString(covariateSettings)).append("\n");
    sb.append("    populationSettings: ").append(toIndentedString(populationSettings)).append("\n");
    sb.append("    modelSettings: ").append(toIndentedString(modelSettings)).append("\n");
    sb.append("    getPlpDataArgs: ").append(toIndentedString(getPlpDataArgs)).append("\n");
    sb.append("    runPlpArgs: ").append(toIndentedString(runPlpArgs)).append("\n");
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
