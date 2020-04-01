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

package org.ohdsi.webapi.check.validator.pathway;

import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.PredicateValidator;
import org.ohdsi.webapi.pathway.dto.BasePathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;

public class PathwayValidator<T extends PathwayAnalysisDTO> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Target cohorts
        Rule<T> targetCohortRule = createRuleWithDefaultValidator(createPath("target cohorts"), reporter)
                .setValueAccessor(BasePathwayAnalysisDTO::getTargetCohorts)
                .build();
        rules.add(targetCohortRule);

        // Event cohorts
        Rule<T> eventCohortRule = createRuleWithDefaultValidator(createPath("event cohorts"), reporter)
                .setValueAccessor(BasePathwayAnalysisDTO::getEventCohorts);
        rules.add(eventCohortRule);

        // Combination window
        Rule<T> combinationWindowRule = createRuleWithDefaultValidator(createPath("combination window"), reporter)
                .setValueAccessor(BasePathwayAnalysisDTO::getCombinationWindow)
                .setErrorTemplate("must be greater or equal to 0")
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 0));
        rules.add(combinationWindowRule);

        // Cell count window
        Rule<T> cellCountRule = createRuleWithDefaultValidator(createPath("minimum cell count"), reporter)
                .setValueAccessor(BasePathwayAnalysisDTO::getMinCellCount)
                .setErrorTemplate("must be greater or equal to 0")
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 0));
        rules.add(cellCountRule);

        // Maximum path length
        Rule<T> maxPathLengthRule = createRuleWithDefaultValidator(createPath("maximum path length"), reporter)
                .setValueAccessor(BasePathwayAnalysisDTO::getMaxDepth)
                .setErrorTemplate("must be between 1 and 10")
                .addValidator(new PredicateValidator<Integer>()
                        .setPredicate(v -> v >= 1 && v <= 10));
        rules.add(maxPathLengthRule);
    }
}
