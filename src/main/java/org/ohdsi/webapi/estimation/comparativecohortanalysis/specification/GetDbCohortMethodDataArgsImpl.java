package org.ohdsi.webapi.estimation.comparativecohortanalysis.specification;

import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.GetDbCohortMethodDataArgs;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.RemoveDuplicateSubjectsEnum;
import org.ohdsi.webapi.RLangClassImpl;
import org.ohdsi.webapi.featureextraction.specification.CovariateSettingsImpl;
import java.util.Date;

/**
 * Create a parameter object for the function getDbCohortMethodData 
 */
public class GetDbCohortMethodDataArgsImpl extends RLangClassImpl implements GetDbCohortMethodDataArgs {
  private Date studyStartDate = null;
  private Date studyEndDate = null;
  private Boolean excludeDrugsFromCovariates = true;
  private Boolean firstExposureOnly = false;
  private RemoveDuplicateSubjectsEnum removeDuplicateSubjects = RemoveDuplicateSubjectsEnum.KEEP_ALL;
  private Boolean restrictToCommonPeriod = false;
  private Integer washoutPeriod = 0;
  private Integer maxCohortSize = 0;
  private CovariateSettingsImpl covariateSettings = null;

  /**
   * A calendar date specifying the minimum date that a cohort index date can appear. Date format is &#x27;yyyymmdd&#x27;. 
   * @return studyStartDate
   **/
  @Override
  public Date getStudyStartDate() {
    return this.studyStartDate == null ? null : new Date(this.studyStartDate.getTime());
  }

    /**
     *
     * @param studyStartDate
     */
    public void setStudyStartDate(Date studyStartDate) {
        this.studyStartDate = studyStartDate == null ? null : new Date(studyStartDate.getTime());
    }

  /**
   * A calendar date specifying the maximum date that a cohort index date can appear. Date format is &#x27;yyyymmdd&#x27;. Important - the study end date is also used to truncate risk windows, meaning no outcomes beyond the study end date will be considered. 
   * @return studyEndDate
   **/
  @Override
  public Date getStudyEndDate() {
    return this.studyEndDate == null ? null : new Date(this.studyEndDate.getTime());
  }

    /**
     *
     * @param studyEndDate
     */
    public void setStudyEndDate(Date studyEndDate) {
    this.studyEndDate = studyEndDate == null ? null : new Date(studyEndDate.getTime());
  }

  /**
   * Should the target and comparator drugs (and their descendant concepts) be excluded from the covariates? Note that this will work if the drugs are actualy drug concept IDs (and not cohort IDs). 
   * @return excludeDrugsFromCovariates
   **/
  @Override
  public Boolean getExcludeDrugsFromCovariates() {
    return excludeDrugsFromCovariates;
  }

    /**
     *
     * @param excludeDrugsFromCovariates
     */
    public void setExcludeDrugsFromCovariates(Boolean excludeDrugsFromCovariates) {
    this.excludeDrugsFromCovariates = excludeDrugsFromCovariates;
  }

  /**
   * Should only the first exposure per subject be included? Note that this is typically done in the createStudyPopulation function, but can already be done here for efficiency reasons. 
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
   * Remove subjects that are in both the target and comparator cohort? Note that this is typically done in the createStudyPopulation function, but can already be done here for efficiency reasons. 
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
   * The mininum required continuous observation time prior to index date for a person to be included in the cohort. Note that this is typically done in the createStudyPopulation function,but can already be done here for efficiency reasons. 
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
   * If either the target or the comparator cohort is larger than this number it will be sampled to this size. maxCohortSize &#x3D; 0 indicates no maximum size. 
   * @return maxCohortSize
   **/
  @Override
  public Integer getMaxCohortSize() {
    return maxCohortSize;
  }

    /**
     *
     * @param maxCohortSize
     */
    public void setMaxCohortSize(Integer maxCohortSize) {
    this.maxCohortSize = maxCohortSize;
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
}
