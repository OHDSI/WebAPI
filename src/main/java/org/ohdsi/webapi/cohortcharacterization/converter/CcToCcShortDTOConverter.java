package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.webapi.cohortcharacterization.dto.CcShortDTO;
import org.springframework.stereotype.Component;

@Component
public class CcToCcShortDTOConverter extends BaseCcToCcShortDTOConverter<CcShortDTO> {
    @Override
    protected CcShortDTO createResultObject() {
        return new CcShortDTO();
    }
}
