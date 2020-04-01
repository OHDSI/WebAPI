/*
 *   Copyright 2017 Observational Health Data Sciences and Informatics
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   Authors: Sergey Suvorov
 *
 */

package org.ohdsi.webapi.check.validator.prediction;

import org.ohdsi.analysis.prediction.design.PatientLevelPredictionAnalysis;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;

public class PredictionSpecificationValidator<T extends PatientLevelPredictionAnalysis> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Target cohorts
        Rule<T> targetCohortRule =
                createRuleWithDefaultValidator(createPath("target cohorts"), reporter)
                        .setValueAccessor(PatientLevelPredictionAnalysis::getTargetIds);
        rules.add(targetCohortRule);

        // Outcome cohorts
        Rule<T> eventCohortRule =
                createRuleWithDefaultValidator(createPath("outcome cohorts"), reporter)
                        .setValueAccessor(PatientLevelPredictionAnalysis::getOutcomeIds);
        rules.add(eventCohortRule);
    }
}
