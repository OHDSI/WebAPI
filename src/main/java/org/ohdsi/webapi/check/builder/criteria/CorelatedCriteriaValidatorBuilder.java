package org.ohdsi.webapi.check.builder.criteria;

import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.webapi.check.builder.ValidatorBuilder;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.criteria.CorelatedCriteriaValidator;

public class CorelatedCriteriaValidatorBuilder<T extends CorelatedCriteria> extends ValidatorBuilder<T> {
    @Override
    public Validator<T> build() {
        return new CorelatedCriteriaValidator<>(createChildPath(), severity, errorMessage);
    }
}
