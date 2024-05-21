package org.ohdsi.webapi.estimation.specification;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.ohdsi.analysis.hydra.design.SkeletonTypeEnum;
import org.ohdsi.analysis.estimation.design.EstimationAnalysis;
import org.ohdsi.webapi.CommonDTO;
import org.ohdsi.webapi.analysis.AnalysisCohortDefinition;
import org.ohdsi.webapi.analysis.AnalysisConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetCrossReferenceImpl;

/**
 *
 * @author asena5
 */
@JsonIgnoreProperties(ignoreUnknown=true, value = {"createdBy", "createdDate", "modifiedBy", "modifiedDate"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EstimationAnalysisImpl implements EstimationAnalysis, CommonDTO {
  private Integer id = null;
  private String name = null;
  private String description = null;
  private String version = null;
  private String organizationName = null;
  private String packageName = null;
  private SkeletonTypeEnum skeletonType = SkeletonTypeEnum.COMPARATIVE_EFFECT_STUDY;
  private String skeletonVersion = "v0.0.1";
  private String createdBy = null;
  private String createdDate = null;
  private String modifiedBy = null;
  private String modifiedDate = null;
  private List<AnalysisCohortDefinition> cohortDefinitions = null;
  private List<AnalysisConceptSet> conceptSets = null;
  private List<ConceptSetCrossReferenceImpl> conceptSetCrossReference = null;
  private List<NegativeControlImpl> negativeControls = null;
  private Boolean doPositiveControlSynthesis = false;
  private PositiveControlSynthesisArgsImpl positiveControlSynthesisArgs = null;
  private NegativeControlOutcomeCohortExpressionImpl negativeControlOutcomeCohortDefinition = null;
  private NegativeControlExposureCohortExpressionImpl negativeControlExposureCohortDefinition = null;
  private EstimationAnalysisSettingsImpl estimationAnalysisSettings = null;

  /**
   * Identifier for the estimation specification
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
   * Name for the estimation specification
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
   * Version number of the specification
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
   * The person who created the specification 
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
   * The person who updated the specification 
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
    public EstimationAnalysisImpl addCohortDefinitionsItem(AnalysisCohortDefinition cohortDefinitionsItem) {
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
    public EstimationAnalysisImpl addConceptSetsItem(AnalysisConceptSet conceptSetsItem) {
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
     *
     * @param conceptSetCrossReferenceItem
     * @return
     */
    public EstimationAnalysisImpl addConceptSetCrossReferenceItem(ConceptSetCrossReferenceImpl conceptSetCrossReferenceItem) {
    if (this.conceptSetCrossReference == null) {
      this.conceptSetCrossReference = new ArrayList<>();
    }
    this.conceptSetCrossReference.add(conceptSetCrossReferenceItem);
    return this;
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
     * @param negativeControlsItem
     * @return
     */
    public EstimationAnalysisImpl addNegativeControlsItem(NegativeControlImpl negativeControlsItem) {
    if (this.negativeControls == null) {
      this.negativeControls = new ArrayList<>();
    }
    this.negativeControls.add(negativeControlsItem);
    return this;
  }

  /**
   * Get negativeControls
   * @return negativeControls
   **/
  @Override
  public List<NegativeControlImpl> getNegativeControls() {
    return negativeControls;
  }

    /**
     *
     * @param negativeControls
     */
    public void setNegativeControls(List<NegativeControlImpl> negativeControls) {
    this.negativeControls = negativeControls;
  }

    
  @Override
  public Boolean getDoPositiveControlSynthesis() {
    return doPositiveControlSynthesis;
  }

    /**
     *
     * @param doPositiveControlSynthesis
     */
    public void setDoPositiveControlSynthesis(Boolean doPositiveControlSynthesis) {
    this.doPositiveControlSynthesis = doPositiveControlSynthesis;
  }

  @Override  
  public PositiveControlSynthesisArgsImpl getPositiveControlSynthesisArgs() {
    return positiveControlSynthesisArgs;
  }

    /**
     *
     * @param positiveControlSynthesisArgs
     */
    public void setPositiveControlSynthesisArgs(PositiveControlSynthesisArgsImpl positiveControlSynthesisArgs) {
    this.positiveControlSynthesisArgs = positiveControlSynthesisArgs;
  }

  @Override  
  public NegativeControlOutcomeCohortExpressionImpl getNegativeControlOutcomeCohortDefinition() {
    return negativeControlOutcomeCohortDefinition;
  }

    /**
     *
     * @param negativeControlOutcomeCohortDefinition
     */
    public void setNegativeControlOutcomeCohortDefinition(NegativeControlOutcomeCohortExpressionImpl negativeControlOutcomeCohortDefinition) {
    this.negativeControlOutcomeCohortDefinition = negativeControlOutcomeCohortDefinition;
  }

    
  @Override
  public NegativeControlExposureCohortExpressionImpl getNegativeControlExposureCohortDefinition() {
    return negativeControlExposureCohortDefinition;
  }

    /**
     *
     * @param negativeControlExposureCohortDefinition
     */
    public void setNegativeControlExposureCohortDefinition(NegativeControlExposureCohortExpressionImpl negativeControlExposureCohortDefinition) {
    this.negativeControlExposureCohortDefinition = negativeControlExposureCohortDefinition;
  }

  @Override  
  public EstimationAnalysisSettingsImpl getEstimationAnalysisSettings() {
    return estimationAnalysisSettings;
  }

    /**
     *
     * @param estimationAnalysisSettings
     */
    public void setEstimationAnalysisSettings(EstimationAnalysisSettingsImpl estimationAnalysisSettings) {
    this.estimationAnalysisSettings = estimationAnalysisSettings;
  }
}