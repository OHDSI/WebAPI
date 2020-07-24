package org.ohdsi.webapi.check.checker.prediction.helper;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.prediction.design.PatientLevelPredictionAnalysis;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;

public class PredictionBuilderUtils {

    public static ValidatorGroupBuilder<PredictionAnalysisDTO, PatientLevelPredictionAnalysis> prepareAnalysisExpressionBuilder() {

        ValidatorGroupBuilder<PredictionAnalysisDTO, PatientLevelPredictionAnalysis> builder = new ValidatorGroupBuilder<PredictionAnalysisDTO, PatientLevelPredictionAnalysis>()
                .attrName("specification")
                .valueGetter(predictionAnalysis ->
                        Utils.deserialize(predictionAnalysis.getSpecification(), PatientLevelPredictionAnalysisImpl.class)
                )
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                )
                .groups(PredictionSpecificationBuilderUtils.prepareOutcomeCohortsBuilder(),
                        PredictionSpecificationBuilderUtils.prepareModelSettingsBuilder(),
                        PredictionSpecificationBuilderUtils.prepareTargetCohortsBuilder(),
                        PredictionSpecificationBuilderUtils.prepareCovariateSettingsBuilder(),
                        PredictionSpecificationBuilderUtils.preparePopulationSettingsBuilder(),
                        PredictionSpecificationBuilderUtils.prepareRunPlpArgsBuilder()
                );
        return builder;
    }
}
