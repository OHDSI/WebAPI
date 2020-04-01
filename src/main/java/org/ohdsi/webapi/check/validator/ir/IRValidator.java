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

package org.ohdsi.webapi.check.validator.ir;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.ValueAccessor;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExpression;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;

public class IRValidator<T extends IRAnalysisDTO> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        ValueAccessor<T> expressionAccessor = t -> {
            try {
                return Utils.deserialize(t.getExpression(), IncidenceRateAnalysisExpression.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        // Analysis expression
        Rule<T> expressionRule = createRuleWithDefaultValidator(createPath(), reporter)
                .setValueAccessor(expressionAccessor)
                .addValidator(new IRAnalysisExpressionValidator());
        rules.add(expressionRule);
    }
}
