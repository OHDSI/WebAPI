package org.ohdsi.webapi.check.validator.cohort;

import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.ForEachValidator;

public class CriteriaGroupValidator<T extends CriteriaGroup> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Demographic criteria
        Rule<T> criteriaRule =
                createRule(createPath("demographic"), reporter)
                        .setValueAccessor(t -> t != null ? t.demographicCriteriaList : null)
                        .addValidator(new ForEachValidator(new DemographicCriteriaValidator()));
        rules.add(criteriaRule);
    }
}
