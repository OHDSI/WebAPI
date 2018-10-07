package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.List;

public class EstimationAnalysis {
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
    COMPARATIVEEFFECTSTUDY("ComparativeEffectStudy");

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
  private SkeletonTypeEnum skeletonType = SkeletonTypeEnum.COMPARATIVEEFFECTSTUDY;

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
  private List<EstimationCohortDefinition> cohortDefinitions = null;

  @JsonProperty("conceptSets")
  private List<EstimationConceptSet> conceptSets = null;

  @JsonProperty("conceptSetCrossReference")
  private List<ConceptSetCrossReference> conceptSetCrossReference = null;

  @JsonProperty("negativeControls")
  private List<NegativeControl> negativeControls = null;

  @JsonProperty("doPositiveControlSynthesis")
  private Boolean doPositiveControlSynthesis = false;

  @JsonProperty("positiveControlSynthesisArgs")
  private PositiveControlSynthesisArgs positiveControlSynthesisArgs = null;

  @JsonProperty("negativeControlOutcomeCohortDefinition")
  private NegativeControlOutcomeCohortExpression negativeControlOutcomeCohortDefinition = null;

  @JsonProperty("negativeControlExposureCohortDefinition")
  private NegativeControlExposureCohortExpression negativeControlExposureCohortDefinition = null;

  @JsonProperty("estimationAnalysisSettings")
  private EstimationAnalysisSettings estimationAnalysisSettings = null;

  public EstimationAnalysis id(Integer id) {
    this.id = id;
    return this;
  }

  /**
   * Identifier for the estimation specification
   * @return id
   **/
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public EstimationAnalysis name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Name for the estimation specification
   * @return name
   **/
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EstimationAnalysis description(String description) {
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

  public EstimationAnalysis version(String version) {
    this.version = version;
    return this;
  }

  /**
   * Version number of the specification
   * @return version
   **/
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public EstimationAnalysis organizationName(String organizationName) {
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

  public EstimationAnalysis packageName(String packageName) {
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

  public EstimationAnalysis skeletonType(SkeletonTypeEnum skeletonType) {
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

  public EstimationAnalysis skeletonVersion(String skeletonVersion) {
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

  public EstimationAnalysis createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  /**
   * The person who created the specification 
   * @return createdBy
   **/
  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public EstimationAnalysis createdDate(String createdDate) {
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

  public EstimationAnalysis modifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
    return this;
  }

  /**
   * The person who updated the specification 
   * @return modifiedBy
   **/
  @JsonProperty("modifiedBy")
  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public EstimationAnalysis modifiedDate(String modifiedDate) {
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

  public EstimationAnalysis cohortDefinitions(List<EstimationCohortDefinition> cohortDefinitions) {
    this.cohortDefinitions = cohortDefinitions;
    return this;
  }

  public EstimationAnalysis addCohortDefinitionsItem(EstimationCohortDefinition cohortDefinitionsItem) {
    if (this.cohortDefinitions == null) {
      this.cohortDefinitions = new ArrayList<EstimationCohortDefinition>();
    }
    this.cohortDefinitions.add(cohortDefinitionsItem);
    return this;
  }

  /**
   * Get cohortDefinitions
   * @return cohortDefinitions
   **/
  @JsonProperty("cohortDefinitions")
  public List<EstimationCohortDefinition> getCohortDefinitions() {
    return cohortDefinitions;
  }

  public void setCohortDefinitions(List<EstimationCohortDefinition> cohortDefinitions) {
    this.cohortDefinitions = cohortDefinitions;
  }

  public EstimationAnalysis conceptSets(List<EstimationConceptSet> conceptSets) {
    this.conceptSets = conceptSets;
    return this;
  }

  public EstimationAnalysis addConceptSetsItem(EstimationConceptSet conceptSetsItem) {
    if (this.conceptSets == null) {
      this.conceptSets = new ArrayList<EstimationConceptSet>();
    }
    this.conceptSets.add(conceptSetsItem);
    return this;
  }

  /**
   * Get conceptSets
   * @return conceptSets
   **/
  @JsonProperty("conceptSets")
  public List<EstimationConceptSet> getConceptSets() {
    return conceptSets;
  }

  public void setConceptSets(List<EstimationConceptSet> conceptSets) {
    this.conceptSets = conceptSets;
  }

  public EstimationAnalysis conceptSetCrossReference(List<ConceptSetCrossReference> conceptSetCrossReference) {
    this.conceptSetCrossReference = conceptSetCrossReference;
    return this;
  }

  public EstimationAnalysis addConceptSetCrossReferenceItem(ConceptSetCrossReference conceptSetCrossReferenceItem) {
    if (this.conceptSetCrossReference == null) {
      this.conceptSetCrossReference = new ArrayList<ConceptSetCrossReference>();
    }
    this.conceptSetCrossReference.add(conceptSetCrossReferenceItem);
    return this;
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

  public EstimationAnalysis negativeControls(List<NegativeControl> negativeControls) {
    this.negativeControls = negativeControls;
    return this;
  }

  public EstimationAnalysis addNegativeControlsItem(NegativeControl negativeControlsItem) {
    if (this.negativeControls == null) {
      this.negativeControls = new ArrayList<NegativeControl>();
    }
    this.negativeControls.add(negativeControlsItem);
    return this;
  }

  /**
   * Get negativeControls
   * @return negativeControls
   **/
  @JsonProperty("negativeControls")
  public List<NegativeControl> getNegativeControls() {
    return negativeControls;
  }

  public void setNegativeControls(List<NegativeControl> negativeControls) {
    this.negativeControls = negativeControls;
  }

  public EstimationAnalysis doPositiveControlSynthesis(Boolean doPositiveControlSynthesis) {
    this.doPositiveControlSynthesis = doPositiveControlSynthesis;
    return this;
  }

  /**
   * Get doPositiveControlSynthesis
   * @return doPositiveControlSynthesis
   **/
  @JsonProperty("doPositiveControlSynthesis")
  public Boolean isisDoPositiveControlSynthesis() {
    return doPositiveControlSynthesis;
  }

  public void setDoPositiveControlSynthesis(Boolean doPositiveControlSynthesis) {
    this.doPositiveControlSynthesis = doPositiveControlSynthesis;
  }

  public EstimationAnalysis positiveControlSynthesisArgs(PositiveControlSynthesisArgs positiveControlSynthesisArgs) {
    this.positiveControlSynthesisArgs = positiveControlSynthesisArgs;
    return this;
  }

  /**
   * Get positiveControlSynthesisArgs
   * @return positiveControlSynthesisArgs
   **/
  @JsonProperty("positiveControlSynthesisArgs")
  public PositiveControlSynthesisArgs getPositiveControlSynthesisArgs() {
    return positiveControlSynthesisArgs;
  }

  public void setPositiveControlSynthesisArgs(PositiveControlSynthesisArgs positiveControlSynthesisArgs) {
    this.positiveControlSynthesisArgs = positiveControlSynthesisArgs;
  }

  public EstimationAnalysis negativeControlOutcomeCohortDefinition(NegativeControlOutcomeCohortExpression negativeControlOutcomeCohortDefinition) {
    this.negativeControlOutcomeCohortDefinition = negativeControlOutcomeCohortDefinition;
    return this;
  }

  /**
   * Get negativeControlOutcomeCohortDefinition
   * @return negativeControlOutcomeCohortDefinition
   **/
  @JsonProperty("negativeControlOutcomeCohortDefinition")
  public NegativeControlOutcomeCohortExpression getNegativeControlOutcomeCohortDefinition() {
    return negativeControlOutcomeCohortDefinition;
  }

  public void setNegativeControlOutcomeCohortDefinition(NegativeControlOutcomeCohortExpression negativeControlOutcomeCohortDefinition) {
    this.negativeControlOutcomeCohortDefinition = negativeControlOutcomeCohortDefinition;
  }

  public EstimationAnalysis negativeControlExposureCohortDefinition(NegativeControlExposureCohortExpression negativeControlExposureCohortDefinition) {
    this.negativeControlExposureCohortDefinition = negativeControlExposureCohortDefinition;
    return this;
  }

  /**
   * Get negativeControlExposureCohortDefinition
   * @return negativeControlExposureCohortDefinition
   **/
  @JsonProperty("negativeControlExposureCohortDefinition")
  public NegativeControlExposureCohortExpression getNegativeControlExposureCohortDefinition() {
    return negativeControlExposureCohortDefinition;
  }

  public void setNegativeControlExposureCohortDefinition(NegativeControlExposureCohortExpression negativeControlExposureCohortDefinition) {
    this.negativeControlExposureCohortDefinition = negativeControlExposureCohortDefinition;
  }

  public EstimationAnalysis estimationAnalysisSettings(EstimationAnalysisSettings estimationAnalysisSettings) {
    this.estimationAnalysisSettings = estimationAnalysisSettings;
    return this;
  }

  /**
   * Get estimationAnalysisSettings
   * @return estimationAnalysisSettings
   **/
  @JsonProperty("estimationAnalysisSettings")
  public EstimationAnalysisSettings getEstimationAnalysisSettings() {
    return estimationAnalysisSettings;
  }

  public void setEstimationAnalysisSettings(EstimationAnalysisSettings estimationAnalysisSettings) {
    this.estimationAnalysisSettings = estimationAnalysisSettings;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EstimationAnalysis estimationAnalysis = (EstimationAnalysis) o;
    return Objects.equals(this.id, estimationAnalysis.id) &&
        Objects.equals(this.name, estimationAnalysis.name) &&
        Objects.equals(this.description, estimationAnalysis.description) &&
        Objects.equals(this.version, estimationAnalysis.version) &&
        Objects.equals(this.organizationName, estimationAnalysis.organizationName) &&
        Objects.equals(this.packageName, estimationAnalysis.packageName) &&
        Objects.equals(this.skeletonType, estimationAnalysis.skeletonType) &&
        Objects.equals(this.skeletonVersion, estimationAnalysis.skeletonVersion) &&
        Objects.equals(this.createdBy, estimationAnalysis.createdBy) &&
        Objects.equals(this.createdDate, estimationAnalysis.createdDate) &&
        Objects.equals(this.modifiedBy, estimationAnalysis.modifiedBy) &&
        Objects.equals(this.modifiedDate, estimationAnalysis.modifiedDate) &&
        Objects.equals(this.cohortDefinitions, estimationAnalysis.cohortDefinitions) &&
        Objects.equals(this.conceptSets, estimationAnalysis.conceptSets) &&
        Objects.equals(this.conceptSetCrossReference, estimationAnalysis.conceptSetCrossReference) &&
        Objects.equals(this.negativeControls, estimationAnalysis.negativeControls) &&
        Objects.equals(this.doPositiveControlSynthesis, estimationAnalysis.doPositiveControlSynthesis) &&
        Objects.equals(this.positiveControlSynthesisArgs, estimationAnalysis.positiveControlSynthesisArgs) &&
        Objects.equals(this.negativeControlOutcomeCohortDefinition, estimationAnalysis.negativeControlOutcomeCohortDefinition) &&
        Objects.equals(this.negativeControlExposureCohortDefinition, estimationAnalysis.negativeControlExposureCohortDefinition) &&
        Objects.equals(this.estimationAnalysisSettings, estimationAnalysis.estimationAnalysisSettings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, version, organizationName, packageName, skeletonType, skeletonVersion, createdBy, createdDate, modifiedBy, modifiedDate, cohortDefinitions, conceptSets, conceptSetCrossReference, negativeControls, doPositiveControlSynthesis, positiveControlSynthesisArgs, negativeControlOutcomeCohortDefinition, negativeControlExposureCohortDefinition, estimationAnalysisSettings);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EstimationAnalysis {\n");
    
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
    sb.append("    negativeControls: ").append(toIndentedString(negativeControls)).append("\n");
    sb.append("    doPositiveControlSynthesis: ").append(toIndentedString(doPositiveControlSynthesis)).append("\n");
    sb.append("    positiveControlSynthesisArgs: ").append(toIndentedString(positiveControlSynthesisArgs)).append("\n");
    sb.append("    negativeControlOutcomeCohortDefinition: ").append(toIndentedString(negativeControlOutcomeCohortDefinition)).append("\n");
    sb.append("    negativeControlExposureCohortDefinition: ").append(toIndentedString(negativeControlExposureCohortDefinition)).append("\n");
    sb.append("    estimationAnalysisSettings: ").append(toIndentedString(estimationAnalysisSettings)).append("\n");
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
