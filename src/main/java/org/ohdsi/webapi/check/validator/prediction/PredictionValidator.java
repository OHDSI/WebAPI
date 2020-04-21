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

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.ValueAccessor;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;

public class PredictionValidator<T extends PredictionAnalysisDTO> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Analysis expression
        prepareAnalysisExpressionRule();
    }

    private void prepareAnalysisExpressionRule() {
        ValueAccessor<T> expressionAccessor = t -> {
            try {
                return Utils.deserialize(t.getSpecification(), PatientLevelPredictionAnalysisImpl.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        Rule<T> rule = createRuleWithDefaultValidator(createPath("specification"), reporter)
                .setValueAccessor(expressionAccessor)
                .addValidator(new PredictionSpecificationValidator());
        rules.add(rule);
    }
}
