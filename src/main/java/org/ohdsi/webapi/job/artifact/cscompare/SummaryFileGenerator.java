package org.ohdsi.webapi.job.artifact.cscompare;

import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobDiffEntity;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobEntity;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobStatsEntity;
import org.ohdsi.webapi.service.cscompare.repository.ConceptSetCompareJobStatsRepository;
import org.ohdsi.webapi.util.GenericFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates summary.txt statistics file for comparison reports
 */
@Component
public class SummaryFileGenerator {

	private static final Logger logger = LoggerFactory.getLogger(SummaryFileGenerator.class);

	private final GenericFileWriter fileWriter;
	private final ConceptSetRepository conceptSetRepository;
	private final ConceptSetCompareJobStatsRepository compareJobStatsRepository;

	public SummaryFileGenerator(
		GenericFileWriter fileWriter,
		ConceptSetRepository conceptSetRepository,
		ConceptSetCompareJobStatsRepository compareJobStatsRepository) {
		this.fileWriter = fileWriter;
		this.conceptSetRepository = conceptSetRepository;
		this.compareJobStatsRepository = compareJobStatsRepository;
	}

	public void generate(Path workDir, ConceptSetCompareJobEntity compareJob,
											 List<ConceptSetCompareJobDiffEntity> diffs) throws IOException {

		StringBuilder summary = new StringBuilder();

		appendHeader(summary);
		appendOverallStatistics(summary, compareJob, diffs);
		appendConceptSetMembershipStatistics(summary, diffs);
		appendMismatchStatistics(summary, diffs);
		appendPerConceptSetBreakdown(summary, compareJob, diffs);

		Path summaryPath = workDir.resolve("summary.txt");
		fileWriter.writeTextFile(summaryPath, pw -> pw.print(summary.toString()));

		logger.debug("Created summary file at {}", summaryPath);
	}

	private void appendHeader(StringBuilder sb) {
		sb.append("Concept Set Batch Comparison - Statistical Summary\n");
		sb.append("==================================================\n\n");
	}

	private void appendOverallStatistics(StringBuilder sb, ConceptSetCompareJobEntity compareJob,
																			 List<ConceptSetCompareJobDiffEntity> diffs) {
		sb.append("OVERALL STATISTICS\n");
		sb.append("------------------\n");

		sb.append("Concept Sets Analyzed: ")
			.append(compareJob.getConceptSetsAnalyzed() != null ? compareJob.getConceptSetsAnalyzed() : 0)
			.append("\n");
		sb.append("Concept Sets with Differences: ")
			.append(compareJob.getConceptSetsWithDiffs() != null ? compareJob.getConceptSetsWithDiffs() : 0)
			.append("\n\n");

		ComparisonStatistics stats = new ComparisonStatistics(diffs);

		sb.append("Total Difference Records: ").append(stats.getTotalDiffs()).append("\n");
		sb.append("  - Included Concept Differences: ").append(stats.getIncludedConceptDiffs()).append("\n");
		sb.append("  - Source Code Differences: ").append(stats.getSourceCodeDiffs()).append("\n\n");
	}

	private void appendConceptSetMembershipStatistics(StringBuilder sb, List<ConceptSetCompareJobDiffEntity> diffs) {
		sb.append("CONCEPT SET MEMBERSHIP DIFFERENCES\n");
		sb.append("----------------------------------\n");

		ComparisonStatistics stats = new ComparisonStatistics(diffs);

		sb.append("Concepts in CS1 Only: ").append(stats.getCs1Only()).append("\n");
		sb.append("Concepts in CS2 Only: ").append(stats.getCs2Only()).append("\n");
		sb.append("Concepts in Both CS: ").append(stats.getBothCS()).append("\n\n");
	}

	private void appendMismatchStatistics(StringBuilder sb, List<ConceptSetCompareJobDiffEntity> diffs) {
		sb.append("ATTRIBUTE MISMATCH STATISTICS\n");
		sb.append("-----------------------------\n");

		ComparisonStatistics stats = new ComparisonStatistics(diffs);

		sb.append("Name Mismatches: ").append(stats.getNameMismatches()).append("\n");
		sb.append("Standard Concept Mismatches: ").append(stats.getStandardConceptMismatches()).append("\n");
		sb.append("Invalid Reason Mismatches: ").append(stats.getInvalidReasonMismatches()).append("\n");
		sb.append("Concept Code Mismatches: ").append(stats.getConceptCodeMismatches()).append("\n");
		sb.append("Domain ID Mismatches: ").append(stats.getDomainIdMismatches()).append("\n");
		sb.append("Vocabulary ID Mismatches: ").append(stats.getVocabularyIdMismatches()).append("\n");
		sb.append("Concept Class ID Mismatches: ").append(stats.getConceptClassIdMismatches()).append("\n");
		sb.append("Valid Start Date Mismatches: ").append(stats.getValidStartDateMismatches()).append("\n");
		sb.append("Valid End Date Mismatches: ").append(stats.getValidEndDateMismatches()).append("\n\n");
	}

	private void appendPerConceptSetBreakdown(StringBuilder sb, ConceptSetCompareJobEntity compareJob,
																						List<ConceptSetCompareJobDiffEntity> diffs) {
		// Get all stats for this compare job
		List<ConceptSetCompareJobStatsEntity> allStats =
			compareJobStatsRepository.findByCompareJobId(compareJob.getId());

		// Create lookup map by concept set ID
		Map<Integer, ConceptSetCompareJobStatsEntity> statsMap = allStats.stream()
			.collect(Collectors.toMap(
				ConceptSetCompareJobStatsEntity::getConceptSetId,
				stats -> stats
			));

		// Group diffs by concept set ID
		Map<Integer, Long> includedConceptSetCounts = diffs.stream()
			.filter(d -> d.getIsSourceCode() == null || !d.getIsSourceCode())
			.collect(Collectors.groupingBy(
				ConceptSetCompareJobDiffEntity::getConceptSetId,
				Collectors.counting()
			));

		// Included concepts breakdown
		sb.append("PER CONCEPT SET BREAKDOWN - INCLUDED CONCEPTS\n");
		sb.append("=============================================\n\n");

		appendConceptSetDetailedTable(sb, includedConceptSetCounts, statsMap, false);

		// Source codes breakdown (if applicable)
		if (compareJob.getCompareSourceCodes() != null && compareJob.getCompareSourceCodes()) {
			sb.append("\n\n");
			sb.append("PER CONCEPT SET BREAKDOWN - SOURCE CODES\n");
			sb.append("========================================\n\n");

			Map<Integer, Long> sourceCodeConceptSetCounts = diffs.stream()
				.filter(d -> d.getIsSourceCode() != null && d.getIsSourceCode())
				.collect(Collectors.groupingBy(
					ConceptSetCompareJobDiffEntity::getConceptSetId,
					Collectors.counting()
				));

			appendConceptSetDetailedTable(sb, sourceCodeConceptSetCounts, statsMap, true);
		}
	}

	private void appendConceptSetDetailedTable(
		StringBuilder sb,
		Map<Integer, Long> conceptSetDiffCounts,
		Map<Integer, ConceptSetCompareJobStatsEntity> statsMap,
		boolean isSourceCode) {

		// Table header
		sb.append(String.format("%-10s %-40s %-15s %-15s %-12s\n",
			"CS ID", "Name", "CS1 Count", "CS2 Count", "Diff Count"));
		sb.append(String.format("%-10s %-40s %-15s %-15s %-12s\n",
			"-----", "----", "---------", "---------", "----------"));

		// Get all unique concept set IDs that have either diffs or stats
		Set<Integer> allConceptSetIds = new HashSet<>();
		allConceptSetIds.addAll(conceptSetDiffCounts.keySet());
		allConceptSetIds.addAll(statsMap.keySet());

		// Sort by diff count (descending), then by concept set ID
		List<Integer> sortedIds = allConceptSetIds.stream()
			.sorted((id1, id2) -> {
				Long count1 = conceptSetDiffCounts.getOrDefault(id1, 0L);
				Long count2 = conceptSetDiffCounts.getOrDefault(id2, 0L);
				int countCompare = count2.compareTo(count1); // Descending
				return countCompare != 0 ? countCompare : id1.compareTo(id2);
			})
			.collect(Collectors.toList());

		// Generate table rows
		for (Integer csId : sortedIds) {
			ConceptSetCompareJobStatsEntity stats = statsMap.get(csId);
			Long diffCount = conceptSetDiffCounts.getOrDefault(csId, 0L);
			String csName = getConceptSetName(csId);

			int cs1Count;
			int cs2Count;

			if (isSourceCode) {
				cs1Count = stats != null ? stats.getCs1IncludedSourceCodesCount() : 0;
				cs2Count = stats != null ? stats.getCs2IncludedSourceCodesCount() : 0;
			} else {
				cs1Count = stats != null ? stats.getCs1IncludedConceptsCount() : 0;
				cs2Count = stats != null ? stats.getCs2IncludedConceptsCount() : 0;
			}

			sb.append(String.format("%-10d %-40s %-15d %-15d %-12d\n",
				csId,
				truncate(csName, 40),
				cs1Count,
				cs2Count,
				diffCount));
		}

		// Summary statistics
		sb.append("\n");
		sb.append("Summary:\n");
		sb.append("--------\n");

		if (statsMap.isEmpty()) {
			sb.append("No statistics available\n");
		} else {
			long totalCs1Count = statsMap.values().stream()
				.mapToLong(s -> isSourceCode ? s.getCs1IncludedSourceCodesCount() : s.getCs1IncludedConceptsCount())
				.sum();

			long totalCs2Count = statsMap.values().stream()
				.mapToLong(s -> isSourceCode ? s.getCs2IncludedSourceCodesCount() : s.getCs2IncludedConceptsCount())
				.sum();

			long totalDiffs = conceptSetDiffCounts.values().stream()
				.mapToLong(Long::longValue)
				.sum();

			sb.append(String.format("Total %s in CS1: %d\n",
				isSourceCode ? "Source Codes" : "Concepts", totalCs1Count));
			sb.append(String.format("Total %s in CS2: %d\n",
				isSourceCode ? "Source Codes" : "Concepts", totalCs2Count));
			sb.append(String.format("Total Differences: %d\n", totalDiffs));

			// Calculate average counts
			int conceptSetCount = statsMap.size();
			if (conceptSetCount > 0) {
				double avgCs1 = (double) totalCs1Count / conceptSetCount;
				double avgCs2 = (double) totalCs2Count / conceptSetCount;

				sb.append(String.format("Average %s per Concept Set in CS1: %.1f\n",
					isSourceCode ? "Source Codes" : "Concepts", avgCs1));
				sb.append(String.format("Average %s per Concept Set in CS2: %.1f\n",
					isSourceCode ? "Source Codes" : "Concepts", avgCs2));
			}
		}
	}

	private String getConceptSetName(Integer conceptSetId) {
		try {
			ConceptSet conceptSet = conceptSetRepository.findById(conceptSetId);
			return Optional.ofNullable(conceptSet).map(ConceptSet::getName).orElse("Unknown");
		} catch (Exception e) {
			logger.warn("Failed to retrieve concept set name for ID {}", conceptSetId, e);
			return "Unknown";
		}
	}

	private String truncate(String value, int maxLength) {
		if (value == null) {
			return "";
		}
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength - 3) + "...";
	}
}