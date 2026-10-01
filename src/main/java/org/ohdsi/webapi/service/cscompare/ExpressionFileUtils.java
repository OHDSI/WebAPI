package org.ohdsi.webapi.service.cscompare;

import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.conceptset.ConceptSetComparison;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

		// combine "not found in DB from input1" and "not found in DB from input2" in one map
		final Map<String, Concept> combinedMap = new HashMap<>(input1ex);
		combinedMap.putAll(input2ex);

		combinedMap.forEach((key, value) -> {
			final ConceptSetComparison out = new ConceptSetComparison();
			final boolean isInIntersection = input1ex.containsKey(key) && input2ex.containsKey(key);
			final boolean isIn1Only = !isInIntersection && input1ex.containsKey(key);
			final boolean isIn2Only = !isInIntersection && input2ex.containsKey(key);

			// Set concept set membership flags
			out.conceptInCS1Only = isIn1Only ? 1L : 0;
			out.conceptInCS2Only = isIn2Only ? 1L : 0;
			out.conceptInCS1AndCS2 = isInIntersection ? 1L : 0;

			// Get concepts from each input if present
			Concept concept1 = input1ex.get(key);
			Concept concept2 = input2ex.get(key);

			// Set vocab1 fields if from input1
			if (concept1 != null) {
				out.vocab1ConceptName = concept1.conceptName;
				out.vocab1StandardConcept = concept1.standardConcept;
				out.vocab1InvalidReason = concept1.invalidReason;
				out.vocab1ConceptCode = concept1.conceptCode;
				out.vocab1DomainId = concept1.domainId;
				out.vocab1VocabularyId = concept1.vocabularyId;
				out.vocab1ConceptClassId = concept1.conceptClassId;
			}

			// Set vocab2 fields if from input2
			if (concept2 != null) {
				out.vocab2ConceptName = concept2.conceptName;
				out.vocab2StandardConcept = concept2.standardConcept;
				out.vocab2InvalidReason = concept2.invalidReason;
				out.vocab2ConceptCode = concept2.conceptCode;
				out.vocab2DomainId = concept2.domainId;
				out.vocab2VocabularyId = concept2.vocabularyId;
				out.vocab2ConceptClassId = concept2.conceptClassId;
			}

			// Use whichever concept is available for the general fields
			Concept conceptToUse = concept1 != null ? concept1 : concept2;
			out.conceptCode = conceptToUse.conceptCode;
			out.vocabularyId = conceptToUse.vocabularyId;

			// Check for mismatches if in both
			if (isInIntersection && concept1 != null && concept2 != null) {
				out.nameMismatch = !Objects.equals(concept1.conceptName, concept2.conceptName);
				out.standardConceptMismatch = !Objects.equals(concept1.standardConcept, concept2.standardConcept);
				out.invalidReasonMismatch = !Objects.equals(concept1.invalidReason, concept2.invalidReason);
				out.conceptCodeMismatch = !Objects.equals(concept1.conceptCode, concept2.conceptCode);
				out.domainIdMismatch = !Objects.equals(concept1.domainId, concept2.domainId);
				out.vocabularyIdMismatch = !Objects.equals(concept1.vocabularyId, concept2.vocabularyId);
				out.conceptClassIdMismatch = !Objects.equals(concept1.conceptClassId, concept2.conceptClassId);
			} else {
				out.nameMismatch = false;
				out.standardConceptMismatch = false;
				out.invalidReasonMismatch = false;
				out.conceptCodeMismatch = false;
				out.domainIdMismatch = false;
				out.vocabularyIdMismatch = false;
				out.conceptClassIdMismatch = false;
				out.validStartDateMismatch = false;
				out.validEndDateMismatch = false;
			}

			outValues.add(out);
		});
		return outValues;
	}

	public static Map<String, Concept> toExclusionMap(final ConceptSetExpression.ConceptSetItem[] in1,
																										final Collection<ConceptSetComparison> fromDb) {
		return Arrays.stream(in1).filter(item ->
			fromDb.stream().noneMatch(out ->
				out.conceptCode.equals(item.concept.conceptCode) &&
					out.vocabularyId.equals(item.concept.vocabularyId))
		).collect(CONCEPT_MAP_COLLECTOR);
	}

	// Overloaded method that returns a single map (for single expression)
	public static Map<String, String> toNamesMap(final ConceptSetExpression.ConceptSetItem[] items) {
		return Arrays.stream(items).collect(NAMES_MAP_COLLECTOR);
	}

	// Keep the old method signature for backward compatibility, but mark as deprecated
	@Deprecated
	public static Map<String, String> toNamesMap(final ConceptSetExpression.ConceptSetItem[] in1,
																							 final ConceptSetExpression.ConceptSetItem[] in2) {
		final Map<String, String> names1 = Arrays.stream(in1).collect(NAMES_MAP_COLLECTOR);
		final Map<String, String> names2 = Arrays.stream(in2).collect(NAMES_MAP_COLLECTOR);
		final Map<String, String> namesCombined = new HashMap<>(names1);
		namesCombined.putAll(names2);
		return namesCombined;
	}
}