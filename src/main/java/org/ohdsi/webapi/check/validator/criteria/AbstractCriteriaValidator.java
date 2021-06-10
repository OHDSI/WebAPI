package org.ohdsi.webapi.check.validator.criteria;

import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.ValidatorGroup;
import org.ohdsi.webapi.check.warning.WarningSeverity;

import static org.ohdsi.webapi.check.checker.criteria.CorelatedCriteriaHelper.prepareCorelatedCriteriaBuilder;
import static org.ohdsi.webapi.check.checker.criteria.CriteriaGroupHelper.prepareCriteriaGroupArrayBuilder;
import static org.ohdsi.webapi.check.checker.criteria.CriteriaGroupHelper.prepareCriteriaGroupBuilder;
import static org.ohdsi.webapi.check.checker.criteria.CriteriaHelper.prepareCriteriaBuilder;
import static org.ohdsi.webapi.check.checker.criteria.DemographicHelper.prepareDemographicBuilder;

public abstract class AbstractCriteriaValidator<T> extends Validator<T> {
    public AbstractCriteriaValidator(Path path, WarningSeverity severity, String errorMessage) {
        super(path, severity, errorMessage);
    }

    protected ValidatorGroup<CriteriaGroup, DemographicCriteria[]> getDemographicCriteriaValidator() {
        ValidatorGroupBuilder<CriteriaGroup, DemographicCriteria[]> builder = prepareDemographicBuilder();
        builder.basePath(this.path);
        return builder.build();
    }

    protected ValidatorGroup<CriteriaGroup, CriteriaGroup[]> getCriteriaGroupArrayValidator() {
        ValidatorGroupBuilder<CriteriaGroup, CriteriaGroup[]> builder = prepareCriteriaGroupArrayBuilder();
        builder.basePath(this.path);
        return builder.build();
    }

    protected ValidatorGroup<Criteria, CriteriaGroup> getCriteriaGroupValidator() {
        ValidatorGroupBuilder<Criteria, CriteriaGroup> builder = prepareCriteriaGroupBuilder();
        builder.basePath(this.path);
        return builder.build();
    }

    protected ValidatorGroup<CriteriaGroup, CorelatedCriteria[]> getCorelatedCriteriaValidator() {
        ValidatorGroupBuilder<CriteriaGroup, CorelatedCriteria[]> builder = prepareCorelatedCriteriaBuilder();
        builder.basePath(this.path);
        return builder.build();
    }

    protected ValidatorGroup<CorelatedCriteria, Criteria> getCriteriaValidator() {
        ValidatorGroupBuilder<CorelatedCriteria, Criteria> builder = prepareCriteriaBuilder();
        builder.basePath(this.path);
        return builder.build();
    }
}
