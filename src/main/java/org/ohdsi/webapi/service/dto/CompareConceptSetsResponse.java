package org.ohdsi.webapi.service.dto;

import org.ohdsi.webapi.conceptset.ConceptSetComparison;

import java.util.Collection;

public class CompareConceptSetsResponse {

	private Collection<ConceptSetComparison> comparisons;
	private int cs1IncludedConceptsCount;
	private int cs1IncludedSourceCodesCount;
	private int cs2IncludedConceptsCount;
	private int cs2IncludedSourceCodesCount;

	public CompareConceptSetsResponse(Collection<ConceptSetComparison> comparisons, int cs1IncludedConceptsCount, int cs1IncludedSourceCodesCount, int cs2IncludedConceptsCount, int cs2IncludedSourceCodesCount) {
		this.comparisons = comparisons;
		this.cs1IncludedConceptsCount = cs1IncludedConceptsCount;
		this.cs1IncludedSourceCodesCount = cs1IncludedSourceCodesCount;
		this.cs2IncludedConceptsCount = cs2IncludedConceptsCount;
		this.cs2IncludedSourceCodesCount = cs2IncludedSourceCodesCount;
	}

	public Collection<ConceptSetComparison> getComparisons() {
		return comparisons;
	}

	public int getCs1IncludedConceptsCount() {
		return cs1IncludedConceptsCount;
	}

	public int getCs1IncludedSourceCodesCount() {
		return cs1IncludedSourceCodesCount;
	}

	public int getCs2IncludedConceptsCount() {
		return cs2IncludedConceptsCount;
	}

	public int getCs2IncludedSourceCodesCount() {
		return cs2IncludedSourceCodesCount;
	}
}
