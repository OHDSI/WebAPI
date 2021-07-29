package org.ohdsi.webapi.estimation.specification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.ohdsi.analysis.estimation.design.EstimationAnalysisConst;
import org.ohdsi.analysis.estimation.design.EstimationTypeEnum;
import org.ohdsi.analysis.estimation.design.EstimationAnalysisSettings;
import org.ohdsi.analysis.estimation.design.Settings;
import org.ohdsi.webapi.estimation.comparativecohortanalysis.specification.ComparativeCohortAnalysisSettings;

/**
 *
 * @author asena5
 */
@JsonSubTypes({
  @JsonSubTypes.Type(value = ComparativeCohortAnalysisSettings.class, name = EstimationAnalysisConst.COMPARATIVE_COHORT_ANALYSIS)
})
@JsonIgnoreProperties({"estimationType"})
public abstract class EstimationAnalysisSettingsImpl implements EstimationAnalysisSettings {

    /**
     *
     * @return
     */
    @Override
  public abstract EstimationTypeEnum getEstimationType();
  
    /**
     *
     * @return
     */
    @Override
  public abstract Settings getAnalysisSpecification();
}

