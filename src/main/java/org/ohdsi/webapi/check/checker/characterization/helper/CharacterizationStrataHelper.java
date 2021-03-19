package org.ohdsi.webapi.check.checker.characterization.helper;

import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.checker.common.CriteriaGroupHelper;
import org.ohdsi.webapi.cohortcharacterization.dto.CcStrataDTO;

public class CharacterizationStrataHelper {
    public static ValidatorGroupBuilder<CcStrataDTO, CriteriaGroup> prepareStrataBuilder() {
        ValidatorGroupBuilder<CcStrataDTO, CriteriaGroup> builder = new ValidatorGroupBuilder<CcStrataDTO, CriteriaGroup>()
                .attrName("subgroup analyses")
                .valueGetter(t -> t.getCriteria())
                .groups(
                        CriteriaGroupHelper.prepareCriteriaGroupBuilder(),
                        CriteriaGroupHelper.prepareDemographicBuilder()
                );
        return builder;
    }
}
