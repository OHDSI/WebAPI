package org.ohdsi.webapi.check.checker.characterization.helper;

import org.ohdsi.webapi.check.builder.IterableForEachValidatorBuilder;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.cohortcharacterization.dto.BaseCcDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcStrataDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataImplDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;

import java.util.Collection;

public class CharacterizationHelper {

    public static ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<FeAnalysisShortDTO>> prepareFeatureAnalysesBuilder() {

        ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<FeAnalysisShortDTO>> builder = new ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<FeAnalysisShortDTO>>()
                .attrName("feature analyses")
                .valueGetter(BaseCcDTO::getFeatureAnalyses)
                .validators(new NotNullNotEmptyValidatorBuilder<>());
        return builder;
    }

    public static ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<CohortMetadataImplDTO>> prepareCohortBuilder() {

        ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<CohortMetadataImplDTO>> builder = new ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<CohortMetadataImplDTO>>()
                .attrName("cohorts")
                .valueGetter(BaseCcDTO::getCohorts)
                .validators(new NotNullNotEmptyValidatorBuilder<>());
        return builder;
    }

    public static ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<? extends CcStrataDTO>> prepareStratifyRuleBuilder() {

        return new ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<? extends CcStrataDTO>>()
                .valueGetter(t -> t.getStratas())
                .validators(
                        new IterableForEachValidatorBuilder<CcStrataDTO>()
                                .groups(CharacterizationStrataHelper.prepareStrataBuilder())
                );
    }
}
