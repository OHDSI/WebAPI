package org.ohdsi.webapi.check.builder.concept;

import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.webapi.check.builder.ValidatorBuilder;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.concept.ConceptValidator;

public class ConceptValidatorBuilder extends ValidatorBuilder<Concept[]> {

    @Override
    public Validator build() {

        return new ConceptValidator(createChildPath(), severity, errorMessage);
    }
}
