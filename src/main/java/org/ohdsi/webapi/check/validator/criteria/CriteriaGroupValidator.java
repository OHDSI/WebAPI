package org.ohdsi.webapi.check.validator.criteria;

import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.warning.WarningSeverity;

import java.util.Objects;

public class CriteriaGroupValidator<T extends CriteriaGroup> extends AbstractCriteriaValidator<T> {
    public CriteriaGroupValidator(Path path, WarningSeverity severity, String errorMessage) {
        super(path, severity, errorMessage);
    }

    @Override
    public boolean validate(T value, Context context) {
        boolean result = true;

        if (Objects.nonNull(value.demographicCriteriaList)) {
            result = getDemographicCriteriaValidator().validate(value, context) && result;
        }
        if (Objects.nonNull(value.groups)) {
            result = getCriteriaGroupArrayValidator().validate(value, context) && result;
        }
        if (Objects.nonNull(value.criteriaList)) {
            result = getCorelatedCriteriaValidator().validate(value, context) && result;
        }

        return result;
    }
}
