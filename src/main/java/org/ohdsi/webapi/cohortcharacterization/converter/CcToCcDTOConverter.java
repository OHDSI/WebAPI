package org.ohdsi.webapi.cohortcharacterization.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import java.util.ArrayList;
import java.util.Set;
import org.ohdsi.webapi.cohortcharacterization.dto.CcParameterDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CcToCcDTOConverter extends BaseCcToCcShortDTOConverter<CohortCharacterizationDTO> {
    
    @Autowired
    private ConverterUtils converterUtils;
    
    @Override
    public CohortCharacterizationDTO convert(final CohortCharacterizationEntity source) {
        final CohortCharacterizationDTO cohortCharacterizationDTO = super.convert(source);

        cohortCharacterizationDTO.setFeatureAnalyses(converterUtils.convertList(toList(source.getFeatureAnalyses()), FeAnalysisDTO.class));
        cohortCharacterizationDTO.setCohorts(converterUtils.convertList(source.getCohortDefinitions(), CohortDTO.class));
        cohortCharacterizationDTO.setParameters(converterUtils.convertList(toList(source.getParameters()), CcParameterDTO.class));

        return cohortCharacterizationDTO;
    }

    @Override
    protected CohortCharacterizationDTO createResultObject() {
        return new CohortCharacterizationDTO();
    }

    private ArrayList<?> toList(final Set<?> analyses) {
        return new ArrayList<>(analyses);
    }
}
