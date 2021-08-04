package org.ohdsi.webapi.check.checker.characterization.helper;

import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.cohortcharacterization.dto.CcStrataDTO;

import static org.ohdsi.webapi.check.checker.criteria.CorelatedCriteriaHelper.prepareCorelatedCriteriaBuilder;
import static org.ohdsi.webapi.check.checker.criteria.CriteriaGroupHelper.prepareCriteriaGroupArrayBuilder;
import static org.ohdsi.webapi.check.checker.criteria.DemographicHelper.prepareDemographicBuilder;

public class CharacterizationStrataHelper {
    public static ValidatorGroupBuilder<CcStrataDTO, CriteriaGroup> prepareStrataBuilder() {
        ValidatorGroupBuilder<CcStrataDTO, CriteriaGroup> builder = new ValidatorGroupBuilder<CcStrataDTO, CriteriaGroup>()
                .attrName("subgroup analyses")
                .valueGetter(t -> t.getCriteria())
                .groups(
                        prepareCriteriaGroupArrayBuilder(),
                        prepareDemographicBuilder(),
                        prepareCorelatedCriteriaBuilder()
                );
        return builder;
    }
}
