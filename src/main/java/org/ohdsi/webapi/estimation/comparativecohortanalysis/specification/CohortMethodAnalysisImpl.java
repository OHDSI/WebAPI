package org.ohdsi.webapi.estimation.comparativecohortanalysis.specification;

import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.CohortMethodAnalysis;
import org.ohdsi.webapi.estimation.specification.AnalysisImpl;

/**
 * CohortMethodAnalysisImpl
 */
public class CohortMethodAnalysisImpl extends AnalysisImpl implements CohortMethodAnalysis {
  private String targetType = null;
  private String comparatorType = null;
  private GetDbCohortMethodDataArgsImpl getDbCohortMethodDataArgs = null;
  private CreateStudyPopulationArgsImpl createStudyPopArgs = null;
  private Boolean createPs = false;
  private CreatePsArgsImpl createPsArgs = null;
  private Boolean trimByPs = false;
  private TrimByPsArgsImpl trimByPsArgs = null;
  private Boolean trimByPsToEquipoise = false;
  private TrimByPsToEquipoiseArgsImpl trimByPsToEquipoiseArgs = null;
  private Boolean matchOnPs = false;
  private MatchOnPsArgsImpl matchOnPsArgs = null;
  private Boolean matchOnPsAndCovariates = false;
  private MatchOnPsAndCovariatesArgsImpl matchOnPsAndCovariatesArgs = null;
  private Boolean stratifyByPs = false;
  private StratifyByPsArgsImpl stratifyByPsArgs = null;
  private Boolean stratifyByPsAndCovariates = false;
  private StratifyByPsAndCovariatesArgsImpl stratifyByPsAndCovariatesArgs = null;
  private Boolean fitOutcomeModel = false;
  private FitOutcomeModelArgsImpl fitOutcomeModelArgs = null;

    /**
     *
     * @param attrClass
     */
    @Override
  public void setAttrClass(String attrClass) {
    super.setAttrClass("cmAnalysis");
  }

  /**
   * If more than one target is provided for each drugComparatorOutcome, this field should be used to select the specific target to use in this analysis. 
   * @return targetType
   **/
  @Override
  public String getTargetType() {
    return targetType;
  }

    /**
     *
     * @param targetType
     */
    public void setTargetType(String targetType) {
    this.targetType = targetType;
  }

  /**
   * If more than one comparator is provided for each drugComparatorOutcome, this field should be used to select the specific comparator to use in this analysis. 
   * @return comparatorType
   **/
  @Override
  public String getComparatorType() {
    return comparatorType;
  }

    /**
     *
     * @param comparatorType
     */
    public void setComparatorType(String comparatorType) {
    this.comparatorType = comparatorType;
  }

  /**
   * Get getDbCohortMethodDataArgs
   * @return getDbCohortMethodDataArgs
   **/
  @Override
  public GetDbCohortMethodDataArgsImpl getDbCohortMethodDataArgs() {
    return getDbCohortMethodDataArgs;
  }

    /**
     *
     * @param getDbCohortMethodDataArgs
     */
    public void setGetDbCohortMethodDataArgs(GetDbCohortMethodDataArgsImpl getDbCohortMethodDataArgs) {
    this.getDbCohortMethodDataArgs = getDbCohortMethodDataArgs;
  }

  /**
   * Get createStudyPopArgs
   * @return createStudyPopArgs
   **/
  @Override
  public CreateStudyPopulationArgsImpl getCreateStudyPopArgs() {
    return createStudyPopArgs;
  }

    /**
     *
     * @param createStudyPopArgs
     */
    public void setCreateStudyPopArgs(CreateStudyPopulationArgsImpl createStudyPopArgs) {
    this.createStudyPopArgs = createStudyPopArgs;
  }

  /**
   * Should the createPs function be used in this analysis? 
   * @return createPs
   **/
  @Override
  public Boolean getCreatePs() {
    return createPs;
  }

    /**
     *
     * @param createPs
     */
    public void setCreatePs(Boolean createPs) {
    this.createPs = createPs;
  }

  /**
   * Get createPsArgs
   * @return createPsArgs
   **/
  @Override
  public CreatePsArgsImpl getCreatePsArgs() {
    return createPsArgs;
  }

    /**
     *
     * @param createPsArgs
     */
    public void setCreatePsArgs(CreatePsArgsImpl createPsArgs) {
    this.createPsArgs = createPsArgs;
  }

  /**
   * Should the trimByPs function be used in this analysis? 
   * @return trimByPs
   **/
  @Override
  public Boolean getTrimByPs() {
    return trimByPs;
  }

    /**
     *
     * @param trimByPs
     */
    public void setTrimByPs(Boolean trimByPs) {
    this.trimByPs = trimByPs;
  }

  /**
   * Get trimByPsArgs
   * @return trimByPsArgs
   **/
  @Override
  public TrimByPsArgsImpl getTrimByPsArgs() {
    return trimByPsArgs;
  }

    /**
     *
     * @param trimByPsArgs
     */
    public void setTrimByPsArgs(TrimByPsArgsImpl trimByPsArgs) {
    this.trimByPsArgs = trimByPsArgs;
  }

  /**
   * Should the trimByPsToEquipoise function be used in this analysis? 
   * @return trimByPsToEquipoise
   **/
  @Override
  public Boolean getTrimByPsToEquipoise() {
    return trimByPsToEquipoise;
  }

    /**
     *
     * @param trimByPsToEquipoise
     */
    public void setTrimByPsToEquipoise(Boolean trimByPsToEquipoise) {
    this.trimByPsToEquipoise = trimByPsToEquipoise;
  }

  /**
   * Get trimByPsToEquipoiseArgs
   * @return trimByPsToEquipoiseArgs
   **/
  @Override
  public TrimByPsToEquipoiseArgsImpl getTrimByPsToEquipoiseArgs() {
    return trimByPsToEquipoiseArgs;
  }

    /**
     *
     * @param trimByPsToEquipoiseArgs
     */
    public void setTrimByPsToEquipoiseArgs(TrimByPsToEquipoiseArgsImpl trimByPsToEquipoiseArgs) {
    this.trimByPsToEquipoiseArgs = trimByPsToEquipoiseArgs;
  }

  /**
   * Should the matchOnPsAndCovariates function be used in this analysis? 
   * @return matchOnPs
   **/
  @Override
  public Boolean getMatchOnPs() {
    return matchOnPs;
  }

    /**
     *
     * @param matchOnPs
     */
    public void setMatchOnPs(Boolean matchOnPs) {
    this.matchOnPs = matchOnPs;
  }

  /**
   * Get matchOnPsArgs
   * @return matchOnPsArgs
   **/
  @Override
  public MatchOnPsArgsImpl getMatchOnPsArgs() {
    return matchOnPsArgs;
  }

    /**
     *
     * @param matchOnPsArgs
     */
    public void setMatchOnPsArgs(MatchOnPsArgsImpl matchOnPsArgs) {
    this.matchOnPsArgs = matchOnPsArgs;
  }

  /**
   * Should the matchOnPsAndCovariates function be used in this analysis? 
   * @return matchOnPsAndCovariates
   **/
  @Override
  public Boolean getMatchOnPsAndCovariates() {
    return matchOnPsAndCovariates;
  }

    /**
     *
     * @param matchOnPsAndCovariates
     */
    public void setMatchOnPsAndCovariates(Boolean matchOnPsAndCovariates) {
    this.matchOnPsAndCovariates = matchOnPsAndCovariates;
  }

    /**
     *
     * @param matchOnPsAndCovariatesArgs
     * @return
     */
    public CohortMethodAnalysis matchOnPsAndCovariatesArgs(MatchOnPsAndCovariatesArgsImpl matchOnPsAndCovariatesArgs) {
    this.matchOnPsAndCovariatesArgs = matchOnPsAndCovariatesArgs;
    return this;
  }

  /**
   * Get matchOnPsAndCovariatesArgs
   * @return matchOnPsAndCovariatesArgs
   **/
  @Override
  public MatchOnPsAndCovariatesArgsImpl getMatchOnPsAndCovariatesArgs() {
    return matchOnPsAndCovariatesArgs;
  }

    /**
     *
     * @param matchOnPsAndCovariatesArgs
     */
    public void setMatchOnPsAndCovariatesArgs(MatchOnPsAndCovariatesArgsImpl matchOnPsAndCovariatesArgs) {
    this.matchOnPsAndCovariatesArgs = matchOnPsAndCovariatesArgs;
  }

  /**
   * Should the stratifyByPs function be used in this analysis? 
   * @return stratifyByPs
   **/
  @Override
  public Boolean getStratifyByPs() {
    return stratifyByPs;
  }

    /**
     *
     * @param stratifyByPs
     */
    public void setStratifyByPs(Boolean stratifyByPs) {
    this.stratifyByPs = stratifyByPs;
  }

  /**
   * Get stratifyByPsArgs
   * @return stratifyByPsArgs
   **/
  @Override
  public StratifyByPsArgsImpl getStratifyByPsArgs() {
    return stratifyByPsArgs;
  }

    /**
     *
     * @param stratifyByPsArgs
     */
    public void setStratifyByPsArgs(StratifyByPsArgsImpl stratifyByPsArgs) {
    this.stratifyByPsArgs = stratifyByPsArgs;
  }

  /**
   * Should the stratifyByPsAndCovariates function be used in this analysis? 
   * @return stratifyByPsAndCovariates
   **/
  @Override
  public Boolean getStratifyByPsAndCovariates() {
    return stratifyByPsAndCovariates;
  }

    /**
     *
     * @param stratifyByPsAndCovariates
     */
    public void setStratifyByPsAndCovariates(Boolean stratifyByPsAndCovariates) {
    this.stratifyByPsAndCovariates = stratifyByPsAndCovariates;
  }

  /**
   * Get stratifyByPsAndCovariatesArgs
   * @return stratifyByPsAndCovariatesArgs
   **/
  @Override
  public StratifyByPsAndCovariatesArgsImpl getStratifyByPsAndCovariatesArgs() {
    return stratifyByPsAndCovariatesArgs;
  }

    /**
     *
     * @param stratifyByPsAndCovariatesArgs
     */
    public void setStratifyByPsAndCovariatesArgs(StratifyByPsAndCovariatesArgsImpl stratifyByPsAndCovariatesArgs) {
    this.stratifyByPsAndCovariatesArgs = stratifyByPsAndCovariatesArgs;
  }

  /**
   * Should the fitOutcomeModel function be used in this analysis? 
   * @return fitOutcomeModel
   **/
  @Override
  public Boolean getFitOutcomeModel() {
    return fitOutcomeModel;
  }

    /**
     *
     * @param fitOutcomeModel
     */
    public void setFitOutcomeModel(Boolean fitOutcomeModel) {
    this.fitOutcomeModel = fitOutcomeModel;
  }

  /**
   * Get fitOutcomeModelArgs
   * @return fitOutcomeModelArgs
   **/
  @Override
  public FitOutcomeModelArgsImpl getFitOutcomeModelArgs() {
    return fitOutcomeModelArgs;
  }

    /**
     *
     * @param fitOutcomeModelArgs
     */
    public void setFitOutcomeModelArgs(FitOutcomeModelArgsImpl fitOutcomeModelArgs) {
    this.fitOutcomeModelArgs = fitOutcomeModelArgs;
  }
}
