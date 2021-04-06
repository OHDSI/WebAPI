package org.ohdsi.webapi.check.builder.criteria;

import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.webapi.check.builder.ValidatorBuilder;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.criteria.CriteriaGroupValidator;

public class CriteriaGroupValidatorBuilder<T extends CriteriaGroup> extends ValidatorBuilder<T> {
    @Override
    public Validator<T> build() {
        return new CriteriaGroupValidator<>(createChildPath(), severity, errorMessage);
    }
}