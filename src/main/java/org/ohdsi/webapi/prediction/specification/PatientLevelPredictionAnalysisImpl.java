package org.ohdsi.webapi.prediction.specification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.ohdsi.analysis.prediction.design.PatientLevelPredictionAnalysis;
import org.ohdsi.analysis.hydra.design.SkeletonTypeEnum;
import org.ohdsi.webapi.CommonDTO;
import org.ohdsi.webapi.analysis.AnalysisCohortDefinition;
import org.ohdsi.webapi.analysis.AnalysisConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetCrossReferenceImpl;
import org.ohdsi.webapi.featureextraction.specification.CovariateSettingsImpl;

import static org.ohdsi.webapi.Constants.Params.PREDICTION_SKELETON_VERSION;
/**
 *
 * @author asena5
 */
@JsonIgnoreProperties(ignoreUnknown=true, value = {"createdBy", "createdDate", "modifiedBy", "modifiedDate"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientLevelPredictionAnalysisImpl implements PatientLevelPredictionAnalysis, CommonDTO {
  private Integer id = null;
  private String name = null;
  private String description = null;
  private String version = null;
  private String organizationName = null;
  private String packageName = null;
  private SkeletonTypeEnum skeletonType = SkeletonTypeEnum.PATIENT_LEVEL_PREDICTION_STUDY;
  private String skeletonVersion = PREDICTION_SKELETON_VERSION;
  private String createdBy = null;
  private String createdDate = null;
  private String modifiedBy = null;
  private String modifiedDate = null;
  private List<AnalysisCohortDefinition> cohortDefinitions = null;
  private List<AnalysisConceptSet> conceptSets = null;
  private List<ConceptSetCrossReferenceImpl> conceptSetCrossReference = null;
  private List<BigDecimal> targetIds = null;
  private List<BigDecimal> outcomeIds = null;
  private List<CovariateSettingsImpl> covariateSettings = null;
  private List<CreateStudyPopulationArgsImpl> populationSettings = null;
  private List<ModelSettingsImpl> modelSettings = null;
  private GetDbPLPDataArgsImpl getPlpDataArgs = null;
  private RunPlpArgsImpl runPlpArgs = null;

  /**
   * Identifier for the PLP specification
   * @return id
   **/
  @Override
  public Integer getId() {
    return id;
  }

    /**
     *
     * @param id
     */
    public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Name for the PLP specification
   * @return name
   **/
  @Override
  public String getName() {
    return name;
  }

    /**
     *
     * @param name
     */
    public void setName(String name) {
    this.name = name;
  }

  /**
   * The description of the study
   * @return description
   **/
  @Override
  public String getDescription() {
    return description;
  }

    /**
     *
     * @param description
     */
    public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Version number of the specification for use by the hydration package
   * @return version
   **/
  @Override
  public String getVersion() {
    return version;
  }

    /**
     *
     * @param version
     */
    public void setVersion(String version) {
    this.version = version;
  }

  /**
   * The organization that produced the specification
   * @return organizationName
   **/
  @Override
  public String getOrganizationName() {
    return organizationName;
  }

    /**
     *
     * @param organizationName
     */
    public void setOrganizationName(String organizationName) {
    this.organizationName = organizationName;
  }

  /**
   * The name of the R Package for execution
   * @return packageName
   **/
  @Override
  public String getPackageName() {
    return packageName;
  }

    /**
     *
     * @param packageName
     */
    public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  /**
   * The base skeleton R package 
   * @return skeletonType
   **/
  @Override
  public SkeletonTypeEnum getSkeletonType() {
    return skeletonType;
  }

    /**
     *
     * @param skeletonType
     */
    public void setSkeletonType(SkeletonTypeEnum skeletonType) {
    this.skeletonType = skeletonType;
  }

  /**
   * The corresponding skeleton version to use
   * @return skeletonVersion
   **/
  @Override
  public String getSkeletonVersion() {
    return skeletonVersion;
  }

    /**
     *
     * @param skeletonVersion
     */
    public void setSkeletonVersion(String skeletonVersion) {
    this.skeletonVersion = skeletonVersion;
  }

  /**
   * The person who created the analysis
   * @return createdBy
   **/
  @Override
  public String getCreatedBy() {
    return createdBy;
  }

    /**
     *
     * @param createdBy
     */
    public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * The date and time the estimation was first saved
   * @return createdDate
   **/
  @Override
  public String getCreatedDate() {
    return createdDate;
  }

    /**
     *
     * @param createdDate
     */
    public void setCreatedDate(String createdDate) {
    this.createdDate = createdDate;
  }

  /**
   * The last person to modify the analysis
   * @return modifiedBy
   **/
  @Override
  public String getModifiedBy() {
    return modifiedBy;
  }

    /**
     *
     * @param modifiedBy
     */
    public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  /**
   * The date and time the estimation was last saved
   * @return modifiedDate
   **/
  @Override
  public String getModifiedDate() {
    return modifiedDate;
  }

    /**
     *
     * @param modifiedDate
     */
    public void setModifiedDate(String modifiedDate) {
    this.modifiedDate = modifiedDate;
  }

    /**
     *
     * @param cohortDefinitionsItem
     * @return
     */
    public PatientLevelPredictionAnalysisImpl addCohortDefinitionsItem(AnalysisCohortDefinition cohortDefinitionsItem) {
    if (this.cohortDefinitions == null) {
      this.cohortDefinitions = new ArrayList<>();
    }
    this.cohortDefinitions.add(cohortDefinitionsItem);
    return this;
  }

  /**
   * Get cohortDefinitions
   * @return cohortDefinitions
   **/
  @Override
  public List<AnalysisCohortDefinition> getCohortDefinitions() {
    return cohortDefinitions;
  }

    /**
     *
     * @param cohortDefinitions
     */
    public void setCohortDefinitions(List<AnalysisCohortDefinition> cohortDefinitions) {
    this.cohortDefinitions = cohortDefinitions;
  }
  
    /**
     *
     * @param conceptSetsItem
     * @return
     */
    public PatientLevelPredictionAnalysisImpl addConceptSetsItem(AnalysisConceptSet conceptSetsItem) {
    if (this.conceptSets == null) {
      this.conceptSets = new ArrayList<>();
    }
    this.conceptSets.add(conceptSetsItem);
    return this;
  }

  /**
   * Get conceptSets
   * @return conceptSets
   **/
  @Override
  public List<AnalysisConceptSet> getConceptSets() {
    return conceptSets;
  }

    /**
     *
     * @param conceptSets
     */
    public void setConceptSets(List<AnalysisConceptSet> conceptSets) {
    this.conceptSets = conceptSets;
  }
  
  /**
   * Get conceptSetCrossReference
   * @return conceptSetCrossReference
   **/
  @Override
  public List<ConceptSetCrossReferenceImpl> getConceptSetCrossReference() {
    return conceptSetCrossReference;
  }

    /**
     *
     * @param conceptSetCrossReference
     */
    public void setConceptSetCrossReference(List<ConceptSetCrossReferenceImpl> conceptSetCrossReference) {
    this.conceptSetCrossReference = conceptSetCrossReference;
  }

    /**
     *
     * @param targetIdsItem
     * @return
     */
    public PatientLevelPredictionAnalysisImpl addTargetIdsItem(BigDecimal targetIdsItem) {
    if (this.targetIds == null) {
      this.targetIds = new ArrayList<>();
    }
    this.targetIds.add(targetIdsItem);
    return this;
  }

  /**
   * A list of the cohort IDs from the cohortDefinition collection for use as targets in the prediction 
   * @return targetIds
   **/
  @Override
  public List<BigDecimal> getTargetIds() {
    return targetIds;
  }

    /**
     *
     * @param targetIds
     */
    public void setTargetIds(List<BigDecimal> targetIds) {
    this.targetIds = targetIds;
  }

    /**
     *
     * @param outcomeIdsItem
     * @return
     */
    public PatientLevelPredictionAnalysisImpl addOutcomeIdsItem(BigDecimal outcomeIdsItem) {
    if (this.outcomeIds == null) {
      this.outcomeIds = new ArrayList<>();
    }
    this.outcomeIds.add(outcomeIdsItem);
    return this;
  }

  /**
   * A list of the cohort IDs from the cohortDefinition collection for use as outcomes in the prediction 
   * @return outcomeIds
   **/
  @Override
  public List<BigDecimal> getOutcomeIds() {
    return outcomeIds;
  }

    /**
     *
     * @param outcomeIds
     */
    public void setOutcomeIds(List<BigDecimal> outcomeIds) {
    this.outcomeIds = outcomeIds;
  }

    /**
     *
     * @param covariateSettingsItem
     * @return
     */
    public PatientLevelPredictionAnalysisImpl addCovariateSettingsItem(CovariateSettingsImpl covariateSettingsItem) {
    if (this.covariateSettings == null) {
      this.covariateSettings = new ArrayList<>();
    }
    this.covariateSettings.add(covariateSettingsItem);
    return this;
  }

  /**
   * Get covariateSettings
   * @return covariateSettings
   **/
  @Override
  public List<CovariateSettingsImpl> getCovariateSettings() {
    return covariateSettings;
  }

    /**
     *
     * @param covariateSettings
     */
    public void setCovariateSettings(List<CovariateSettingsImpl> covariateSettings) {
    this.covariateSettings = covariateSettings;
  }

    /**
     *
     * @param populationSettingsItem
     * @return
     */
    public PatientLevelPredictionAnalysisImpl addPopulationSettingsItem(CreateStudyPopulationArgsImpl populationSettingsItem) {
    if (this.populationSettings == null) {
      this.populationSettings = new ArrayList<>();
    }
    this.populationSettings.add(populationSettingsItem);
    return this;
  }

  /**
   * Get populationSettings
   * @return populationSettings
   **/
  @Override
  public List<CreateStudyPopulationArgsImpl> getPopulationSettings() {
    return populationSettings;
  }

    /**
     *
     * @param populationSettings
     */
    public void setPopulationSettings(List<CreateStudyPopulationArgsImpl> populationSettings) {
    this.populationSettings = populationSettings;
  }

    /**
     *
     * @param modelSettingsItem
     * @return
     */
    public PatientLevelPredictionAnalysisImpl addModelSettingsItem(ModelSettingsImpl modelSettingsItem) {
    if (this.modelSettings == null) {
      this.modelSettings = new ArrayList<>();
    }
    this.modelSettings.add(modelSettingsItem);
    return this;
  }

  /**
   * Get modelSettings
   * @return modelSettings
   **/
  @Override
  public List<ModelSettingsImpl> getModelSettings() {
    return modelSettings;
  }

    /**
     *
     * @param modelSettings
     */
    public void setModelSettings(List<ModelSettingsImpl> modelSettings) {
    this.modelSettings = modelSettings;
  }

  /**
   * Get getPlpDataArgs
   * @return getPlpDataArgs
   **/
  @Override
  public GetDbPLPDataArgsImpl getPlpDataArgs() {
    return getPlpDataArgs;
  }

    /**
     *
     * @param getPlpDataArgs
     */
    public void setGetPlpDataArgs(GetDbPLPDataArgsImpl getPlpDataArgs) {
    this.getPlpDataArgs = getPlpDataArgs;
  }

  /**
   * Get runPlpArgs
   * @return runPlpArgs
   **/
  @Override
  public RunPlpArgsImpl getRunPlpArgs() {
    return runPlpArgs;
  }

    /**
     *
     * @param runPlpArgs
     */
    public void setRunPlpArgs(RunPlpArgsImpl runPlpArgs) {
    this.runPlpArgs = runPlpArgs;
  }
}
