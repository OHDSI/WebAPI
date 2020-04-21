package org.ohdsi.webapi.check.validator.estimation;

import org.ohdsi.analysis.estimation.design.NegativeControlOutcomeCohortExpression;
import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;

public class NegativeControlOutcomeCohortExpressionValidator<T extends NegativeControlOutcomeCohortExpression> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Occurrence type
        prepareCccurrenceTypeRule();

        // Detect on descendants
        prepareDetectOnDescendantsRule();

        // Minimum required continuous observation time
        prepareDomainsRule();

        // Domains to detect negative control outcomes
        prepareDomainsRule();
    }

    private void prepareCccurrenceTypeRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("type of occurrence of the event when selecting from the domain"), reporter)
                        .setValueAccessor(NegativeControlOutcomeCohortExpression::getOccurrenceType);
        rules.add(rule);
    }

    private void prepareDetectOnDescendantsRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("using of descendant concepts for the negative control outcome"), reporter)
                        .setValueAccessor(NegativeControlOutcomeCohortExpression::getDetectOnDescendants);
        rules.add(rule);
    }

    private void prepareDomainsRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("domains to detect negative control outcomes"), reporter)
                        .setValueAccessor(NegativeControlOutcomeCohortExpression::getDomains);
        rules.add(rule);
    }
}

