package org.ohdsi.webapi.check.validator.concept;

import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.warning.WarningSeverity;

import java.util.Objects;

public class ConceptValidator extends Validator<Concept[]> {
    private static final String EMPTY = "empty";

    public ConceptValidator(Path path, WarningSeverity severity, String errorMessage) {

        super(path, severity, errorMessage);
    }

    @Override
    public boolean validate(Concept[] value, Context context) {

        boolean isValid = true;
        if (Objects.nonNull(value) && value.length == 0) {
            context.addWarning(getSeverity(), getErrorMessage(value), path);
            isValid = false;
        }
        return isValid;
    }

    protected String getDefaultErrorMessage() {

        return EMPTY;
    }
}
