package org.ohdsi.webapi.check.validator.criteria;

import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.warning.WarningSeverity;

import java.util.Objects;

public class CorelatedCriteriaValidator<T extends CorelatedCriteria> extends AbstractCriteriaValidator<T> {
    public CorelatedCriteriaValidator(Path path, WarningSeverity severity, String errorMessage) {
        super(path, severity, errorMessage);
    }

    @Override
    public boolean validate(T value, Context context) {
        boolean result = true;

        if (Objects.nonNull(value.criteria)) {
            result = getCriteriaValidator().validate(value, context) && result;
        }

        return result;
    }
}
