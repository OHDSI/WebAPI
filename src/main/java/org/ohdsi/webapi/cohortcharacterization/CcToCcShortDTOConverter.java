package org.ohdsi.webapi.cohortcharacterization;

import org.springframework.stereotype.Component;

@Component
public class CcToCcShortDTOConverter extends BaseCcToCcShortDTOConverter<CcShortDTO> {
    @Override
    protected CcShortDTO createResultObject() {
        return new CcShortDTO();
    }
}
