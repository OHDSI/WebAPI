package org.ohdsi.circe.check;

import org.ohdsi.circe.check.checkers.*;
import org.ohdsi.circe.cohortdefinition.CohortExpression;

import java.util.ArrayList;
import java.util.List;

public class Checker implements Check {

    private List<Check> getChecks() {
        List<Check> checks = new ArrayList<>();
        checks.add(new UnusedConceptsCheck());
        checks.add(new ExitCriteriaCheck());
        checks.add(new ExitCriteriaDaysOffsetCheck());
        checks.add(new RangeCheck());
        checks.add(new ConceptCheck());
        checks.add(new AttributeCheck());
        checks.add(new TextCheck());
        checks.add(new IncompleteRuleCheck());
        checks.add(new InitialEventCheck());
        checks.add(new NoExitCriteriaCheck());
        checks.add(new ConceptSetCriteriaCheck());
        checks.add(new DrugEraCheck());
        checks.add(new OcurrenceCheck());
        checks.add(new DuplicatesCriteriaCheck());
        checks.add(new DuplicatesConceptSetCheck());
        checks.add(new TreatmentLineCheck());
        checks.add(new DrugDomainCheck());
        checks.add(new EmptyConceptSetCheck());
        checks.add(new EventsProgressionCheck());
        checks.add(new TimeWindowCheck());
        checks.add(new TimePatternCheck());
        checks.add(new DomainTypeCheck());
        checks.add(new CriteriaContradictionsCheck());
        checks.add(new DeathTimeWindowCheck());
        return checks;
    }

    @Override
    public List<Warning> check(final CohortExpression expression) {

        List<Warning> result = new ArrayList<>();
        for(Check check : getChecks()) {
            result.addAll(check.check(expression));
        }
        return result;
    }
}