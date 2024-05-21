package org.ohdsi.webapi.service.cscompare;

import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.conceptset.ConceptSetComparison;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ExpressionFileUtils {
    private static final String CODE_AND_VOCABID_KEY = "%s:%s";
    private static final Collector<ConceptSetExpression.ConceptSetItem, ?, Map<String, Concept>> CONCEPT_MAP_COLLECTOR =
            Collectors.toMap(ExpressionFileUtils::getKey, item -> item.concept);
    private static final Collector<ConceptSetExpression.ConceptSetItem, ?, Map<String, String>> NAMES_MAP_COLLECTOR =
            Collectors.toMap(ExpressionFileUtils::getKey, item -> item.concept.conceptName);

    public static String getKey(final ConceptSetExpression.ConceptSetItem item) {
        return String.format(CODE_AND_VOCABID_KEY, item.concept.conceptCode, item.concept.vocabularyId);
    }

    public static String getKey(final ConceptSetComparison item) {
        return String.format(CODE_AND_VOCABID_KEY, item.conceptCode, item.vocabularyId);
    }

    public static Collection<ConceptSetComparison> combine(final Map<String, Concept> input1ex,
                                                           final Map<String, Concept> input2ex) {
        final Collection<ConceptSetComparison> outValues = new ArrayList<>();

        // combine "not found in DB from input1" and "not found in DB from input2" in one map (to deal with doubles)
        final Map<String, Concept> combinedMap = new HashMap<>(input1ex);
        combinedMap.putAll(input2ex);

        combinedMap.forEach((key, value) -> {
            final ConceptSetComparison out = new ConceptSetComparison();
            final boolean isInIntersection = input1ex.containsKey(key) && input2ex.containsKey(key);
            final boolean isIn1Only = !isInIntersection && input1ex.containsKey(key);
            final boolean isIn2Only = !isInIntersection && input2ex.containsKey(key);
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
                fromDb.stream().noneMatch(out -> out.conceptCode.equals(item.concept.conceptCode) && out.vocabularyId.equals(item.concept.vocabularyId))
        ).collect(CONCEPT_MAP_COLLECTOR);
    }

    public static Map<String, String> toNamesMap(final ConceptSetExpression.ConceptSetItem[] in1,
                                                 final ConceptSetExpression.ConceptSetItem[] in2) {
        final Map<String, String> names1 = Arrays.stream(in1).collect(NAMES_MAP_COLLECTOR);
        final Map<String, String> names2 = Arrays.stream(in2).collect(NAMES_MAP_COLLECTOR);
        final Map<String, String> namesCombined = new HashMap<>(names1);
        namesCombined.putAll(names2);
        return namesCombined;
    }
}
