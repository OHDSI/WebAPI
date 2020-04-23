package org.ohdsi.webapi.check.validator.prediction;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.featureextraction.design.CovariateSettings;
import org.ohdsi.analysis.prediction.design.CreateStudyPopulationArgs;
import org.ohdsi.analysis.prediction.design.ModelSettings;
import org.ohdsi.analysis.prediction.design.PatientLevelPredictionAnalysis;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.DuplicateValidator;

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
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("settings"), reporter)
                        .setValueGetter(PatientLevelPredictionAnalysis::getRunPlpArgs)
                        .addValidator(new RunPlpArgsValidator());
        rules.add(rule);
    }

    private void prepareOutcomeCohortsRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("outcome cohorts"), reporter)
                        .setValueGetter(PatientLevelPredictionAnalysis::getOutcomeIds);
        rules.add(rule);
    }

    private void prepareTargetCohortsRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("target cohorts"), reporter)
                        .setValueGetter(PatientLevelPredictionAnalysis::getTargetIds);
        rules.add(rule);
    }

    private void prepareModelSettingsRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("model settings"), reporter)
                        .setValueGetter(PatientLevelPredictionAnalysis::getModelSettings)
                        .addValidator(new DuplicateValidator<ModelSettings>()
                                .setElementGetter(Utils::serialize));
        rules.add(rule);
    }

    private void prepareCovariateSettingsRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("covariate settings"), reporter)
                        .setValueGetter(PatientLevelPredictionAnalysis::getCovariateSettings)
                        .addValidator(new DuplicateValidator<CovariateSettings>()
                                .setElementGetter(Utils::serialize));
        rules.add(rule);
    }

    private void preparePopulationSettingsRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("population settings"), reporter)
                        .setValueGetter(PatientLevelPredictionAnalysis::getPopulationSettings)
                        .addValidator(new DuplicateValidator<CreateStudyPopulationArgs>()
                                .setElementGetter(Utils::serialize));
        rules.add(rule);
    }
}
