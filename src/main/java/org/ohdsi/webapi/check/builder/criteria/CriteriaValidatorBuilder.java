package org.ohdsi.webapi.check.builder.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.webapi.check.builder.ValidatorBuilder;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.criteria.CriteriaValidator;

public class CriteriaValidatorBuilder<T extends Criteria> extends ValidatorBuilder<T> {
    @Override
    public Validator<T> build() {
        return new CriteriaValidator<>(createChildPath(), severity, errorMessage);
    }
}
