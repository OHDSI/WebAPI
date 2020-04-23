package org.ohdsi.webapi.check.validator.prediction;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.featureextraction.design.CovariateSettings;
import org.ohdsi.analysis.prediction.design.CreateStudyPopulationArgs;
import org.ohdsi.analysis.prediction.design.ModelSettings;
import org.ohdsi.analysis.prediction.design.PatientLevelPredictionAnalysis;
import org.ohdsi.analysis.prediction.design.RunPlpArgs;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.DuplicateValidator;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;

import java.math.BigDecimal;
import java.util.Collection;

public class PredictionSpecificationValidator<T extends PatientLevelPredictionAnalysis> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Target cohorts
        prepareTargetCohortsRule();

        // Outcome cohorts
        prepareOutcomeCohortsRule();

        // Model settings
        prepareModelSettingsRule();

        // Covariate settings
        prepareCovariateSettingsRule();

        // Model settings
        preparePopulationSettingsRule();

        // Run plp args
        prepareRunPlpArgsRule();
    }

    private void prepareRunPlpArgsRule() {
        Rule<T, RunPlpArgs> rule = new Rule<T, RunPlpArgs>()
                .setPath(createPath("settings"))
                .setReporter(reporter)
                .setValueGetter(PatientLevelPredictionAnalysis::getRunPlpArgs)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new RunPlpArgsValidator<>());
        rules.add(rule);
    }

    private void prepareOutcomeCohortsRule() {
        Rule<T, Collection<BigDecimal>> rule = new Rule<T, Collection<BigDecimal>>()
                .setPath(createPath("outcome cohorts"))
                .setReporter(reporter)
                .setValueGetter(PatientLevelPredictionAnalysis::getOutcomeIds)
                .addValidator(new NotNullNotEmptyValidator<>());
        rules.add(rule);
    }

    private void prepareTargetCohortsRule() {
        Rule<T, Collection<BigDecimal>> rule = new Rule<T, Collection<BigDecimal>>()
                .setPath(createPath("target cohorts"))
                .setReporter(reporter)
                .setValueGetter(PatientLevelPredictionAnalysis::getTargetIds)
                .addValidator(new NotNullNotEmptyValidator<>());
        rules.add(rule);
    }

    private void prepareModelSettingsRule() {
        Rule<T, Collection<? extends ModelSettings>> rule = new Rule<T, Collection<? extends ModelSettings>>()
                .setPath(createPath("model settings"))
                .setReporter(reporter)
                .setValueGetter(PatientLevelPredictionAnalysis::getModelSettings)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new DuplicateValidator<ModelSettings, String>()
                        .setElementGetter(Utils::serialize));
        rules.add(rule);
    }

    private void prepareCovariateSettingsRule() {
        Rule<T, Collection<? extends CovariateSettings>> rule = new Rule<T, Collection<? extends CovariateSettings>>()
                .setPath(createPath("covariate settings"))
                .setReporter(reporter)
                .setValueGetter(PatientLevelPredictionAnalysis::getCovariateSettings)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new DuplicateValidator<CovariateSettings, String>()
                        .setElementGetter(Utils::serialize));
        rules.add(rule);
    }

    private void preparePopulationSettingsRule() {
        Rule<T, Collection<? extends CreateStudyPopulationArgs>> rule = new Rule<T, Collection<? extends CreateStudyPopulationArgs>>()
                .setPath(createPath("population settings"))
                .setReporter(reporter)
                .setValueGetter(PatientLevelPredictionAnalysis::getPopulationSettings)
                .addValidator(new NotNullNotEmptyValidator<>())
                .addValidator(new DuplicateValidator<CreateStudyPopulationArgs, String>()
                        .setElementGetter(Utils::serialize));
        rules.add(rule);
    }
}
