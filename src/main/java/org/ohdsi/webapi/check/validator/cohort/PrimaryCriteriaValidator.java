package org.ohdsi.webapi.check.validator.cohort;

import org.ohdsi.circe.cohortdefinition.PrimaryCriteria;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.ForEachValidator;

public class PrimaryCriteriaValidator<T extends PrimaryCriteria> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Criteria list
        Rule<T> ageRule =
                createRule(createPath(), reporter)
                        .setValueAccessor(t -> t != null ? t.criteriaList : null)
                        .addValidator(new ForEachValidator(new BaseCriteriaValidator()));
        rules.add(ageRule);
    }
}
