package org.ohdsi.webapi.check.validator.estimation;

import org.ohdsi.analysis.estimation.design.NegativeControlOutcomeCohortExpression;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;

import java.util.List;

public class NegativeControlOutcomeCohortExpressionValidator<T extends NegativeControlOutcomeCohortExpression> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Occurrence type
        prepareOccurrenceTypeRule();

        // Detect on descendants
        prepareDetectOnDescendantsRule();

        // Minimum required continuous observation time
        prepareDomainsRule();

        // Domains to detect negative control outcomes
        prepareDomainsRule();
    }

    private void prepareOccurrenceTypeRule() {
        Rule<T, String> rule = new Rule<T, String>()
                .setPath(createPath("type of occurrence of the event when selecting from the domain"))
                .setReporter(reporter)
                .setValueGetter(NegativeControlOutcomeCohortExpression::getOccurrenceType)
                .addValidator(new NotNullNotEmptyValidator<>());
        rules.add(rule);
    }

    private void prepareDetectOnDescendantsRule() {
        Rule<T, Boolean> rule = new Rule<T, Boolean>()
                .setPath(createPath("using of descendant concepts for the negative control outcome"))
                .setReporter(reporter)
                .setValueGetter(NegativeControlOutcomeCohortExpression::getDetectOnDescendants)
                .addValidator(new NotNullNotEmptyValidator<>());
        rules.add(rule);
    }

    private void prepareDomainsRule() {
        Rule<T, List<String>> rule = new Rule<T, List<String>>()
                .setPath(createPath("domains to detect negative control outcomes"))
                .setReporter(reporter)
                .setValueGetter(NegativeControlOutcomeCohortExpression::getDomains)
                .addValidator(new NotNullNotEmptyValidator<>());
        rules.add(rule);
    }
}

