package org.ohdsi.webapi.check.validator.cohort;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;

public class CriteriaValidator<T extends Criteria> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Criteria group
        Rule<T> criteriaRule =
                createRule(createPath(), reporter)
                        .setValueAccessor(t -> t != null ? t.CorrelatedCriteria : null)
                        .addValidator(new CriteriaGroupValidator());
        rules.add(criteriaRule);
    }
}
