package org.ohdsi.webapi.service.cscompare;

import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.conceptset.ConceptSetComparison;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ExpressionFileUtils {
    private static final Collector<ConceptSetExpression.ConceptSetItem, ?, Map<String, Concept>> CONCEPT_MAP_COLLECTOR =
            Collectors.toMap(item -> item.concept.conceptName + ":" + item.concept.conceptCode + ":" + item.concept.vocabularyId, item -> item.concept);

    public static Collection<ConceptSetComparison> inExCombined(final Map<String, Concept> in1ex, final Map<String, Concept> in2ex, final Map<String, Concept> inIntersection) {
        final Collection<ConceptSetComparison> outValues = new ArrayList<>();
        // combine "only in 1" and "only in 2" in one map (to deal with doubles)
        final Map<String, Concept> inExCombinedMap = new HashMap<>(in1ex);
        inExCombinedMap.putAll(in2ex);

        inExCombinedMap.forEach((key, value) -> {
            final ConceptSetComparison out = new ConceptSetComparison();
            final boolean isInIntersection = inIntersection.containsKey(key);
            final boolean isIn1Only = !isInIntersection && in1ex.containsKey(key);
            final boolean isIn2Only = !isInIntersection && in2ex.containsKey(key);
            out.conceptIn1Only = isIn1Only ? 1L : 0;
            out.conceptIn2Only = isIn2Only ? 1L : 0;
            out.conceptIn1And2 = isInIntersection ? 1L : 0;
            out.conceptName = value.conceptName;
            out.conceptCode = value.conceptCode;
            out.vocabularyId = value.vocabularyId;

            outValues.add(out);
        });
        return outValues;
    }


    public static Map<String, Concept> toExclusionMap(final ConceptSetExpression.ConceptSetItem[] in1, final Collection<ConceptSetComparison> fromDb) {
        return Arrays.stream(in1).filter(item ->
                fromDb.stream().noneMatch(out -> out.conceptCode.equals(item.concept.conceptCode) && out.vocabularyId.equals(item.concept.vocabularyId) && out.conceptName.equals(item.concept.conceptName))
        ).collect(CONCEPT_MAP_COLLECTOR);
    }

    public static Map<String, Concept> toIntersectionMap(final ConceptSetExpression.ConceptSetItem[] in1, final ConceptSetExpression.ConceptSetItem[] in2) {
        return Arrays.stream(in1).filter(in1Item ->
                Arrays.stream(in2).anyMatch(in2Item -> in1Item.concept.conceptCode.equals(in2Item.concept.conceptCode) && in1Item.concept.vocabularyId.equals(in2Item.concept.vocabularyId))
        ).collect(CONCEPT_MAP_COLLECTOR);
    }

}
