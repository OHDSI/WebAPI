package org.ohdsi.webapi.check.checker.estimation.helper;

import java.util.List;
import org.ohdsi.analysis.estimation.design.NegativeControlOutcomeCohortExpression;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class NegativeControlOutcomeCohortExpressionHelper {

    public static ValidatorGroupBuilder<NegativeControlOutcomeCohortExpression, String> prepareOccurrenceTypeBuilder() {

        return new ValidatorGroupBuilder<NegativeControlOutcomeCohortExpression, String>()
                .attrName("type of occurrence of the event when selecting from the domain")
                .valueGetter(NegativeControlOutcomeCohortExpression::getOccurrenceType)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                );
    }

    public static ValidatorGroupBuilder<NegativeControlOutcomeCohortExpression, Boolean> prepareDetectOnDescendantsBuilder() {

        ValidatorGroupBuilder<NegativeControlOutcomeCohortExpression, Boolean> builder = new ValidatorGroupBuilder<NegativeControlOutcomeCohortExpression, Boolean>()
                .attrName("using of descendant concepts for the negative control outcome")
                .valueGetter(NegativeControlOutcomeCohortExpression::getDetectOnDescendants)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                );
        return builder;
    }

    public static ValidatorGroupBuilder<NegativeControlOutcomeCohortExpression, List<String>> prepareDomainsBuilder() {

        ValidatorGroupBuilder<NegativeControlOutcomeCohortExpression, List<String>> builder = new ValidatorGroupBuilder<NegativeControlOutcomeCohortExpression, List<String>>()
                .attrName("domains to detect negative control outcomes")
                .valueGetter(NegativeControlOutcomeCohortExpression::getDomains)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                );
        return builder;

    }
}
