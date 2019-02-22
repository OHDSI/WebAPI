package org.ohdsi.webapi.analysis.converter;

import java.util.ArrayList;
import java.util.List;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.analysis.AnalysisConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;

public class AnalysisConceptSetToConceptSetItemConverter<T extends AnalysisConceptSet> extends BaseConversionServiceAwareConverter<T, List<ConceptSetItem>> {
    
    @Override
    public List<ConceptSetItem> convert(T source) {
        List<ConceptSetItem> csiList = new ArrayList<>();
        for(ConceptSetExpression.ConceptSetItem i : source.expression.items) {
            ConceptSetItem csi = new ConceptSetItem();
            csi.setConceptId(i.concept.conceptId);
            csi.setIncludeDescendants(i.includeDescendants ? 1 : 0);
            csi.setIncludeMapped(i.includeMapped ? 1 :0);
            csi.setIsExcluded(i.isExcluded ? 1 : 0);
            csiList.add(csi);
        }
        return csiList;
    }
    
}
