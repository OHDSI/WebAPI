package org.ohdsi.webapi.check.checker.estimation.helper;

import static org.ohdsi.webapi.check.checker.estimation.helper.EstimationAnalysisSpecificationHelper.*;

import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.ComparativeCohortAnalysis;
import org.ohdsi.analysis.estimation.design.EstimationAnalysisSettings;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class EstimationSettingsHelper {

    public static ValidatorGroupBuilder<EstimationAnalysisSettings, ComparativeCohortAnalysis> prepareAnalysisSpecificationBuilder() {

        ValidatorGroupBuilder<EstimationAnalysisSettings, ComparativeCohortAnalysis> builder = new ValidatorGroupBuilder<EstimationAnalysisSettings, ComparativeCohortAnalysis>()
                .valueGetter(EstimationAnalysisSettings::getAnalysisSpecification)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                )
                .groups(
                        prepareTargetComparatorBuilder(),
                        prepareAnalysisSettingsBuilder()
                );
        return builder;
    }
}
