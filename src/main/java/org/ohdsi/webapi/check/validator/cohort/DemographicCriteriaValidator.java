package org.ohdsi.webapi.check.validator.cohort;

import org.ohdsi.circe.cohortdefinition.DemographicCriteria;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.DateRangeValidator;
import org.ohdsi.webapi.check.validator.common.NumericRangeValidator;

public class DemographicCriteriaValidator<T extends DemographicCriteria> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Age
        Rule<T> ageRule =
                createRuleWithDefaultValidator(createPath("age"), reporter)
                        .setValueAccessor(t -> t.age)
                        .addValidator(new NumericRangeValidator());
        rules.add(ageRule);

        // Gender
        Rule<T> genderRule =
                createRuleWithDefaultValidator(createPath("gender"), reporter)
                        .setValueAccessor(t -> t.gender);
        rules.add(genderRule);

        // Race
        Rule<T> raceRule =
                createRuleWithDefaultValidator(createPath("race"), reporter)
                        .setValueAccessor(t -> t.race);
        rules.add(raceRule);

        // Ethnicity
        Rule<T> ethnicityRule =
                createRuleWithDefaultValidator(createPath("ethnicity"), reporter)
                        .setValueAccessor(t -> t.ethnicity);
        rules.add(ethnicityRule);

        // Occurrence start date
        Rule<T> occurrenceStartDateRule =
                createRuleWithDefaultValidator(createPath("occurrence start date"), reporter)
                        .setValueAccessor(t -> t.occurrenceStartDate)
                        .addValidator(new DateRangeValidator());
        rules.add(occurrenceStartDateRule);

        // Occurence end date
        Rule<T> occurrenceEndDateRule =
                createRuleWithDefaultValidator(createPath("occurence end date"), reporter)
                        .setValueAccessor(t -> t.occurrenceEndDate)
                        .addValidator(new DateRangeValidator());
        rules.add(occurrenceEndDateRule);
    }
}
