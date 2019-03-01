package org.ohdsi.webapi.analysis.converter;

import org.ohdsi.webapi.analysis.AnalysisConceptSet;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.springframework.stereotype.Component;

@Component
public class AnalysisConceptSetToConceptSetConverter<T extends AnalysisConceptSet> extends BaseConversionServiceAwareConverter<T, ConceptSetDTO> {
 
    @Override
    public ConceptSetDTO convert(T source) {
        ConceptSetDTO cs = new ConceptSetDTO();
        cs.setName(source.name);
        return cs;
    }
}
