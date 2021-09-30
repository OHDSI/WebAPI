package org.ohdsi.webapi.check.checker.prediction.helper;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.prediction.design.PatientLevelPredictionAnalysis;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;

public class PredictionBuilderHelper {

    public static ValidatorGroupBuilder<PredictionAnalysisDTO, PatientLevelPredictionAnalysis> prepareAnalysisExpressionBuilder() {

        ValidatorGroupBuilder<PredictionAnalysisDTO, PatientLevelPredictionAnalysis> builder = new ValidatorGroupBuilder<PredictionAnalysisDTO, PatientLevelPredictionAnalysis>()
                .attrName("specification")
                .valueGetter(predictionAnalysis ->
                        Utils.deserialize(predictionAnalysis.getSpecification(), PatientLevelPredictionAnalysisImpl.class)
                )
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                )
                .groups(PredictionSpecificationBuilderHelper.prepareOutcomeCohortsBuilder(),
                        PredictionSpecificationBuilderHelper.prepareModelSettingsBuilder(),
                        PredictionSpecificationBuilderHelper.prepareTargetCohortsBuilder(),
                        PredictionSpecificationBuilderHelper.prepareCovariateSettingsBuilder(),
                        PredictionSpecificationBuilderHelper.preparePopulationSettingsBuilder(),
                        PredictionSpecificationBuilderHelper.prepareRunPlpArgsBuilder()
                );
        return builder;
    }
}
