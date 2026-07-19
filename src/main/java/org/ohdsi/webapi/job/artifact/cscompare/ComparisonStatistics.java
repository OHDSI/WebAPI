package org.ohdsi.webapi.job.artifact.cscompare;

import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobDiffEntity;

import java.util.List;

/**
 * Calculates statistics from comparison diff entities
 */
public class ComparisonStatistics {

	private final long totalDiffs;
	private final long includedConceptDiffs;
	private final long sourceCodeDiffs;
	private final long uniqueConcepts;
	private final long nameMismatches;
	private final long standardConceptMismatches;
	private final long invalidReasonMismatches;
	private final long conceptCodeMismatches;
	private final long domainIdMismatches;
	private final long vocabularyIdMismatches;
	private final long conceptClassIdMismatches;
	private final long validStartDateMismatches;
	private final long validEndDateMismatches;
	private final long cs1Only;
	private final long cs2Only;
	private final long bothCS;

	public ComparisonStatistics(List<ConceptSetCompareJobDiffEntity> diffs) {
		this.totalDiffs = diffs.size();

		this.includedConceptDiffs = diffs.stream()
			.filter(d -> d.getIsSourceCode() == null || !d.getIsSourceCode())
			.count();

		this.sourceCodeDiffs = diffs.stream()
			.filter(d -> d.getIsSourceCode() != null && d.getIsSourceCode())
			.count();

		this.uniqueConcepts = diffs.stream()
			.map(ConceptSetCompareJobDiffEntity::getConceptId)
			.distinct()
			.count();

		this.nameMismatches = countMismatch(diffs, ConceptSetCompareJobDiffEntity::getNameMismatch);
		this.standardConceptMismatches = countMismatch(diffs, ConceptSetCompareJobDiffEntity::getStandardConceptMismatch);
		this.invalidReasonMismatches = countMismatch(diffs, ConceptSetCompareJobDiffEntity::getInvalidReasonMismatch);
		this.conceptCodeMismatches = countMismatch(diffs, ConceptSetCompareJobDiffEntity::getConceptCodeMismatch);
		this.domainIdMismatches = countMismatch(diffs, ConceptSetCompareJobDiffEntity::getDomainIdMismatch);
		this.vocabularyIdMismatches = countMismatch(diffs, ConceptSetCompareJobDiffEntity::getVocabularyIdMismatch);
		this.conceptClassIdMismatches = countMismatch(diffs, ConceptSetCompareJobDiffEntity::getConceptClassIdMismatch);
		this.validStartDateMismatches = countMismatch(diffs, ConceptSetCompareJobDiffEntity::getValidStartDateMismatch);
		this.validEndDateMismatches = countMismatch(diffs, ConceptSetCompareJobDiffEntity::getValidEndDateMismatch);

		this.cs1Only = diffs.stream()
			.filter(d -> d.getConceptInCS1Only() != null && d.getConceptInCS1Only() > 0)
			.count();

		this.cs2Only = diffs.stream()
			.filter(d -> d.getConceptInCS2Only() != null && d.getConceptInCS2Only() > 0)
			.count();

		this.bothCS = diffs.stream()
			.filter(d -> d.getConceptInCS1AndCS2() != null && d.getConceptInCS1AndCS2() > 0)
			.count();
	}

	private long countMismatch(List<ConceptSetCompareJobDiffEntity> diffs,
														 java.util.function.Function<ConceptSetCompareJobDiffEntity, Boolean> getter) {
		return diffs.stream()
			.filter(d -> {
				Boolean value = getter.apply(d);
				return value != null && value;
			})
			.count();
	}

	// Getters
	public long getTotalDiffs() {
		return totalDiffs;
	}

	public long getIncludedConceptDiffs() {
		return includedConceptDiffs;
	}

	public long getSourceCodeDiffs() {
		return sourceCodeDiffs;
	}

	public long getUniqueConcepts() {
		return uniqueConcepts;
	}

	public long getNameMismatches() {
		return nameMismatches;
	}

	public long getStandardConceptMismatches() {
		return standardConceptMismatches;
	}

	public long getInvalidReasonMismatches() {
		return invalidReasonMismatches;
	}

	public long getConceptCodeMismatches() {
		return conceptCodeMismatches;
	}

	public long getDomainIdMismatches() {
		return domainIdMismatches;
	}

	public long getVocabularyIdMismatches() {
		return vocabularyIdMismatches;
	}

	public long getConceptClassIdMismatches() {
		return conceptClassIdMismatches;
	}

	public long getValidStartDateMismatches() {
		return validStartDateMismatches;
	}

	public long getValidEndDateMismatches() {
		return validEndDateMismatches;
	}

	public long getCs1Only() {
		return cs1Only;
	}

	public long getCs2Only() {
		return cs2Only;
	}

	public long getBothCS() {
		return bothCS;
	}
}