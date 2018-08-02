package com.jnj.honeur.webapi.service;

import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.service.VocabularyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component("honeurConceptSetService")
@ConditionalOnProperty(value = "datasource.honeur.enabled", havingValue = "true")
public class HoneurConceptSetService extends ConceptSetService {

    @Autowired
    private VocabularyService vocabService;

    @Autowired
    private SourceService sourceService;

    @Override
    public ConceptSetExpression getConceptSetExpression(@PathParam("id") final int id) {
        HashMap<Long, ConceptSetItem> map = new HashMap<>();

        // collect the concept set items so we can lookup their properties later
        for (ConceptSetItem csi : getConceptSetItems(id)) {
            map.put(csi.getConceptId(), csi);
        }

        // create our expression to return
        ConceptSetExpression expression = new ConceptSetExpression();
//        expression.items = new ConceptSetExpression.ConceptSetItem[map.size()];
        ArrayList<ConceptSetExpression.ConceptSetItem> expressionItems = new ArrayList<>();

        // lookup the concepts we need information for
        long[] identifiers = new long[map.size()];
        int identifierIndex = 0;
        for (Long identifier : map.keySet()) {
            identifiers[identifierIndex] = identifier;
            identifierIndex++;
        }

        // assume we want to resolve using the priority vocabulary provider
//        SourceInfo vocabSourceInfo = sourceService.getPriorityVocabularySourceInfo();
//        Collection<Concept> concepts = vocabService.executeIdentifierLookup(vocabSourceInfo.sourceKey, identifiers);

        List<Collection<Concept>> conceptCollections = sourceService.getSources().stream()
                .map(sourceInfo -> vocabService.executeIdentifierLookup(sourceInfo.sourceKey, identifiers))
                .collect(Collectors.toList());

        // put the concept information into the expression along with the concept set item information
        for(Collection<Concept> concepts: conceptCollections) {
            for (Concept concept : concepts) {
                ConceptSetExpression.ConceptSetItem currentItem = new ConceptSetExpression.ConceptSetItem();
                currentItem.concept = concept;
                ConceptSetItem csi = map.get(concept.conceptId);
                currentItem.includeDescendants = (csi.getIncludeDescendants() == 1);
                currentItem.includeMapped = (csi.getIncludeMapped() == 1);
                currentItem.isExcluded = (csi.getIsExcluded() == 1);
                expressionItems.add(currentItem);
            }
        }
        expression.items = expressionItems.toArray(new ConceptSetExpression.ConceptSetItem[0]);

        return expression;
    }
}
