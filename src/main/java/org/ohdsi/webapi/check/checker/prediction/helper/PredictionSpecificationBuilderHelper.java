package org.ohdsi.webapi.check.checker.prediction.helper;

import java.math.BigDecimal;
import java.util.Collection;
import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.featureextraction.design.CovariateSettings;
import org.ohdsi.analysis.prediction.design.CreateStudyPopulationArgs;
import org.ohdsi.analysis.prediction.design.ModelSettings;
import org.ohdsi.analysis.prediction.design.PatientLevelPredictionAnalysis;
import org.ohdsi.analysis.prediction.design.RunPlpArgs;
import org.ohdsi.webapi.check.builder.DuplicateValidatorBuilder;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class PredictionSpecificationBuilderHelper {

    public static ValidatorGroupBuilder<PatientLevelPredictionAnalysis, RunPlpArgs> prepareRunPlpArgsBuilder() {

        ValidatorGroupBuilder<PatientLevelPredictionAnalysis, RunPlpArgs> builder = new ValidatorGroupBuilder<PatientLevelPredictionAnalysis, RunPlpArgs>()
                .attrName("settings")
                .valueGetter(PatientLevelPredictionAnalysis::getRunPlpArgs)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                )
                .groups(
                        RunPlpArgsBuilderHelper.prepareMinCovariateFractionBuilder(),
                        RunPlpArgsBuilderHelper.prepareTestFractionBuilder()
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<BigDecimal>> prepareOutcomeCohortsBuilder() {

        ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<BigDecimal>> builder = new ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<BigDecimal>>()
                .attrName("outcome cohorts")
                .valueGetter(PatientLevelPredictionAnalysis::getOutcomeIds)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<BigDecimal>> prepareTargetCohortsBuilder() {

        ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<BigDecimal>> builder = new ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<BigDecimal>>()
                .attrName("target cohorts")
                .valueGetter(PatientLevelPredictionAnalysis::getTargetIds)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<? extends ModelSettings>> prepareModelSettingsBuilder() {

        ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<? extends ModelSettings>> builder = new ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<? extends ModelSettings>>()
                .attrName("model settings")
                .valueGetter(PatientLevelPredictionAnalysis::getModelSettings)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new DuplicateValidatorBuilder<ModelSettings, String>()
                                .elementGetter(Utils::serialize)
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<? extends CovariateSettings>> prepareCovariateSettingsBuilder() {

        ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<? extends CovariateSettings>> builder = new ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<? extends CovariateSettings>>()
                .attrName("covariate settings")
                .valueGetter(PatientLevelPredictionAnalysis::getCovariateSettings)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new DuplicateValidatorBuilder<CovariateSettings, String>()
                                .elementGetter(Utils::serialize)
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<? extends CreateStudyPopulationArgs>> preparePopulationSettingsBuilder() {

        ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<? extends CreateStudyPopulationArgs>> builder = new ValidatorGroupBuilder<PatientLevelPredictionAnalysis, Collection<? extends CreateStudyPopulationArgs>>()
                .attrName("population settings")
                .valueGetter(PatientLevelPredictionAnalysis::getPopulationSettings)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new DuplicateValidatorBuilder<CreateStudyPopulationArgs, String>()
                                .elementGetter(Utils::serialize)
                );
        return builder;
    }
}
