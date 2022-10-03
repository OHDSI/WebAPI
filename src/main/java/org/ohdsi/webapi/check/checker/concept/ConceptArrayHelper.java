package org.ohdsi.webapi.check.checker.concept;

import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.builder.concept.ConceptValidatorBuilder;

import java.util.function.Function;

public class ConceptArrayHelper {
    public static <T> ValidatorGroupBuilder<T, Concept[]> prepareConceptBuilder(
            Function<T, Concept[]> valueGetter, String attrName) {
        return new ValidatorGroupBuilder<T, Concept[]>()
                .attrName(attrName)
                .valueGetter(valueGetter)
                .validators(
                        new ConceptValidatorBuilder()
                );
    }
}
