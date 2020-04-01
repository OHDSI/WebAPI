package org.ohdsi.webapi.check.validator.cohort;

import org.ohdsi.circe.cohortdefinition.ConditionEra;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;

public class BaseCriteriaValidator<T extends Criteria> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Condition era
        Rule<T> conditionEraRule =
                createRule(createPath("condition era"), reporter)
                        .setValueAccessor(t -> ConditionEra.class.isInstance(t) ? t : null )
                        .addValidator(new ConditionEraValidator());
        rules.add(conditionEraRule);
    }

    private static final class ConditionEraValidator<T extends ConditionEra> extends CriteriaValidator<T> {
        @Override
        protected void buildInternal() {
            super.buildInternal();

            // Codeset
            Rule<T> codesetRule =
                    createRuleWithDefaultValidator(createPath("codeset"), reporter)
                            .setValueAccessor(t -> t.codesetId);
            rules.add(codesetRule);

            // Gender
            Rule<T> genderRule =
                    createRuleWithDefaultValidator(createPath("gender"), reporter)
                            .setValueAccessor(t -> t.gender);
            rules.add(genderRule);
        }
    }
}
