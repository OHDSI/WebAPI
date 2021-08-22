package org.ohdsi.webapi.check.validator.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.warning.WarningSeverity;

import java.util.Objects;

public class CriteriaValidator<T extends Criteria> extends AbstractCriteriaValidator<T> {
    public CriteriaValidator(Path path, WarningSeverity severity, String errorMessage) {
        super(path, severity, errorMessage);
    }

    @Override
    public boolean validate(T value, Context context) {
        boolean result = true;

        if (Objects.nonNull(value.CorrelatedCriteria)) {
            result = getCriteriaGroupValidator().validate(value, context) && result;
        }

        return result;
    }
}
