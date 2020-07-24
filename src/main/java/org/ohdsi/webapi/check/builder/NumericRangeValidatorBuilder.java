package org.ohdsi.webapi.check.builder;

import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.common.NumericRangeValidator;

public class NumericRangeValidatorBuilder<T extends NumericRange> extends ValidatorBuilder<NumericRange> {

    @Override
    public Validator<NumericRange> build() {

        return new NumericRangeValidator<>(createChildPath(),  severity, errorMessage);
    }
}
