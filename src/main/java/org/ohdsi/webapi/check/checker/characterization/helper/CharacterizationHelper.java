package org.ohdsi.webapi.check.checker.characterization.helper;

import java.util.Collection;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.cohortcharacterization.dto.BaseCcDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;

public class CharacterizationHelper {

    public static ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<FeAnalysisShortDTO>> prepareFeatureAnalysesBuilder() {

        ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<FeAnalysisShortDTO>> builder = new ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<FeAnalysisShortDTO>>()
                .attrName("feature analyses")
                .valueGetter(BaseCcDTO::getFeatureAnalyses)
                .validators(new NotNullNotEmptyValidatorBuilder<>());
        return builder;
    }

    public static ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<CohortMetadataDTO>> prepareCohortBuilder() {

        ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<CohortMetadataDTO>> builder = new ValidatorGroupBuilder<CohortCharacterizationDTO, Collection<CohortMetadataDTO>>()
                .attrName("cohorts")
                .valueGetter(BaseCcDTO::getCohorts)
                .validators(new NotNullNotEmptyValidatorBuilder<>());
        return builder;
    }
}
