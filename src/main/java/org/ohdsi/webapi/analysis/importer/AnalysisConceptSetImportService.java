package org.ohdsi.webapi.analysis.importer;

import java.util.List;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.analysis.AnalysisConceptSet;
import org.ohdsi.webapi.analysis.converter.AnalysisConceptSetToConceptSetConverter;
import org.ohdsi.webapi.analysis.converter.AnalysisConceptSetToConceptSetItemConverter;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.springframework.stereotype.Service;

@Service
public class AnalysisConceptSetImportService {
    private final ConceptSetService conceptSetService;
    private final AnalysisConceptSetToConceptSetConverter csConverter = new AnalysisConceptSetToConceptSetConverter();
    private final AnalysisConceptSetToConceptSetItemConverter csiConverter = new AnalysisConceptSetToConceptSetItemConverter();
    
    public AnalysisConceptSetImportService(ConceptSetService conceptSetService) {
        this.conceptSetService = conceptSetService;
    }
    
    public ConceptSetDTO persistConceptSet(final AnalysisConceptSet analysisConceptSet) {
        ConceptSetDTO cs = csConverter.convert(analysisConceptSet);
        cs = conceptSetService.createConceptSet(cs);
        final Integer conceptSetId = cs.getId();
        List<ConceptSetItem> csi = csiConverter.convert(analysisConceptSet);
        csi.forEach(n -> n.setConceptSetId(conceptSetId));
        conceptSetService.saveConceptSetItems(cs.getId(), csi.stream().toArray(ConceptSetItem[]::new));
        return cs;
    }
}
