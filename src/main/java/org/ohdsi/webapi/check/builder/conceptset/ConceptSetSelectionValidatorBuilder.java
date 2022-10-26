package org.ohdsi.webapi.check.builder.conceptset;

import org.ohdsi.circe.cohortdefinition.ConceptSetSelection;
import org.ohdsi.webapi.check.builder.ValidatorBuilder;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.conceptset.ConceptSetSelectionValidator;

public class ConceptSetSelectionValidatorBuilder<T extends ConceptSetSelection> extends ValidatorBuilder<T> {

    @Override
    public Validator<T> build() {

        return new ConceptSetSelectionValidator<>(createChildPath(), severity, errorMessage);
    }
}
