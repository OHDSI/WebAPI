package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataImplDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.springframework.stereotype.Component;

@Component
public class CcToCcDTOConverter extends BaseCcToCcDTOConverter<CohortCharacterizationDTO> {
    
    @Override
    public CohortCharacterizationDTO convert(final CohortCharacterizationEntity source) {
        final CohortCharacterizationDTO cohortCharacterizationDTO = super.convert(source);

        cohortCharacterizationDTO.setCohorts(converterUtils.convertSet(source.getCohortDefinitions(), CohortMetadataImplDTO.class));
        cohortCharacterizationDTO.setFeatureAnalyses(converterUtils.convertSet(source.getFeatureAnalyses(), FeAnalysisShortDTO.class));
        return cohortCharacterizationDTO;
    }

    @Override
    protected CohortCharacterizationDTO createResultObject() {
        return new CohortCharacterizationDTO();
    }
}
