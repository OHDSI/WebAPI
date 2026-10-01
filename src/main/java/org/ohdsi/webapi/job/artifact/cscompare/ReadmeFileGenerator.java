package org.ohdsi.webapi.job.artifact.cscompare;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobAuthorEntity;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobDiffEntity;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobEntity;
import org.ohdsi.webapi.util.GenericFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates README.txt metadata file for comparison reports
 */
@Component
public class ReadmeFileGenerator {

	private static final Logger logger = LoggerFactory.getLogger(ReadmeFileGenerator.class);

	private final GenericFileWriter fileWriter;

	public ReadmeFileGenerator(GenericFileWriter fileWriter) {
		this.fileWriter = fileWriter;
	}

	public void generate(Path workDir, JobExecution jobExecution, JobParameters jobParams,
											 ConceptSetCompareJobEntity compareJob, List<ConceptSetCompareJobDiffEntity> diffs) throws IOException {

		StringBuilder metadata = new StringBuilder();

		appendHeader(metadata);
		appendExecutionDetails(metadata, jobExecution);
		appendComparisonParameters(metadata, jobParams, compareJob);
		appendResultsSummary(metadata, compareJob, diffs);
		appendReportStructure(metadata, compareJob);

		Path metadataPath = workDir.resolve("README.txt");
		fileWriter.writeTextFile(metadataPath, pw -> pw.print(metadata.toString()));

		logger.debug("Created README file at {}", metadataPath);
	}

	private void appendHeader(StringBuilder sb) {
		sb.append("=====================================\n");
		sb.append("Concept Set Batch Comparison Report\n");
		sb.append("=====================================\n\n");
	}

	private void appendExecutionDetails(StringBuilder sb, JobExecution jobExecution) {
		sb.append("JOB EXECUTION DETAILS\n");
		sb.append("---------------------\n");
		sb.append("Execution ID: ").append(jobExecution.getId()).append("\n");
		sb.append("Job Name: ").append(jobExecution.getJobInstance().getJobName()).append("\n");
		sb.append("Status: ").append(jobExecution.getStatus()).append("\n");
		sb.append("Start Time: ").append(jobExecution.getStartTime()).append("\n");
		sb.append("End Time: ").append(jobExecution.getEndTime()).append("\n");

		if (jobExecution.getStartTime() != null && jobExecution.getEndTime() != null) {
			Duration duration = Duration.between(
				jobExecution.getStartTime().toInstant(),
				jobExecution.getEndTime().toInstant()
			);
			sb.append("Duration: ").append(formatDuration(duration)).append("\n");
		}
		sb.append("\n");
	}

	private void appendComparisonParameters(StringBuilder sb, JobParameters jobParams,
																					ConceptSetCompareJobEntity compareJob) {
		sb.append("COMPARISON PARAMETERS\n");
		sb.append("---------------------\n");
		sb.append("Source 1: ").append(jobParams.getString("source1Key")).append("\n");
		sb.append("Source 2: ").append(jobParams.getString("source2Key")).append("\n");

		if (compareJob != null) {
			sb.append("Vocabulary 1 Version: ")
				.append(compareJob.getVocab1Version() != null ? compareJob.getVocab1Version() : "N/A")
				.append("\n");
			sb.append("Vocabulary 2 Version: ")
				.append(compareJob.getVocab2Version() != null ? compareJob.getVocab2Version() : "N/A")
				.append("\n");

			// Handle multiple authors
			appendAuthorsInfo(sb, compareJob);

			sb.append("Compare Source Codes: ")
				.append(compareJob.getCompareSourceCodes() != null ? compareJob.getCompareSourceCodes() : false)
				.append("\n");

			// Add concept set IDs filter info
			if (StringUtils.isNotBlank(compareJob.getConceptSetIds())) {
				sb.append("Concept Set IDs Filter: ").append(compareJob.getConceptSetIds()).append("\n");
			}
		}

		sb.append("Created Date From: ")
			.append(jobParams.getString("createdDateFrom") != null ? jobParams.getString("createdDateFrom") : "N/A")
			.append("\n");
		sb.append("Created Date To: ")
			.append(jobParams.getString("createdDateTo") != null ? jobParams.getString("createdDateTo") : "N/A")
			.append("\n");
		sb.append("Updated Date From: ")
			.append(jobParams.getString("updatedDateFrom") != null ? jobParams.getString("updatedDateFrom") : "N/A")
			.append("\n");
		sb.append("Updated Date To: ")
			.append(jobParams.getString("updatedDateTo") != null ? jobParams.getString("updatedDateTo") : "N/A")
			.append("\n");
		sb.append("Tags: ")
			.append(jobParams.getString("tagsIds") != null ? jobParams.getString("tagsIds") : "N/A")
			.append("\n");
		sb.append("\n");
	}

	/**
	 * Append authors information to the StringBuilder
	 */
	private void appendAuthorsInfo(StringBuilder sb, ConceptSetCompareJobEntity compareJob) {
		if (compareJob.getAuthors() != null && !compareJob.getAuthors().isEmpty()) {
			if (compareJob.getAuthors().size() == 1) {
				// Single author - display on one line
				ConceptSetCompareJobAuthorEntity authorEntity = compareJob.getAuthors().iterator().next();
				String authorName = getAuthorDisplayName(authorEntity);
				sb.append("Author: ").append(authorName).append("\n");
			} else {
				// Multiple authors - display as list
				sb.append("Authors (").append(compareJob.getAuthors().size()).append("):\n");
				List<String> authorNames = compareJob.getAuthors().stream()
					.map(this::getAuthorDisplayName)
					.sorted()
					.collect(Collectors.toList());

				for (String authorName : authorNames) {
					sb.append("  - ").append(authorName).append("\n");
				}
			}
		} else {
			sb.append("Author: N/A\n");
		}
	}

	/**
	 * Get display name for an author (name or login)
	 */
	private String getAuthorDisplayName(ConceptSetCompareJobAuthorEntity authorEntity) {
		if (authorEntity == null || authorEntity.getUser() == null) {
			return "Unknown";
		}

		String name = authorEntity.getUser().getName();
		String login = authorEntity.getUser().getLogin();

		if (name != null && !name.trim().isEmpty()) {
			return String.format("%s (%s)", name, login);
		} else {
			return login;
		}
	}

	private void appendResultsSummary(StringBuilder sb, ConceptSetCompareJobEntity compareJob,
																		List<ConceptSetCompareJobDiffEntity> diffs) {
		sb.append("RESULTS SUMMARY\n");
		sb.append("---------------\n");

		if (compareJob != null) {
			sb.append("Concept Sets Analyzed: ")
				.append(compareJob.getConceptSetsAnalyzed() != null ? compareJob.getConceptSetsAnalyzed() : 0)
				.append("\n");
			sb.append("Concept Sets with Differences: ")
				.append(compareJob.getConceptSetsWithDiffs() != null ? compareJob.getConceptSetsWithDiffs() : 0)
				.append("\n\n");
		}

		if (diffs != null && !diffs.isEmpty()) {
			ComparisonStatistics stats = new ComparisonStatistics(diffs);

			sb.append("Total Differences Found: ").append(stats.getTotalDiffs()).append("\n");
			sb.append("  - Included Concept Differences: ").append(stats.getIncludedConceptDiffs()).append("\n");
			sb.append("  - Source Code Differences: ").append(stats.getSourceCodeDiffs()).append("\n");
			sb.append("Unique Concepts with Differences: ").append(stats.getUniqueConcepts()).append("\n");
			sb.append("\n");
			sb.append("Mismatch Counts:\n");
			sb.append("  - Name Mismatches: ").append(stats.getNameMismatches()).append("\n");
			sb.append("  - Standard Concept Mismatches: ").append(stats.getStandardConceptMismatches()).append("\n");
			sb.append("  - Invalid Reason Mismatches: ").append(stats.getInvalidReasonMismatches()).append("\n");
			sb.append("  - Concept Code Mismatches: ").append(stats.getConceptCodeMismatches()).append("\n");
			sb.append("  - Domain ID Mismatches: ").append(stats.getDomainIdMismatches()).append("\n");
			sb.append("  - Vocabulary ID Mismatches: ").append(stats.getVocabularyIdMismatches()).append("\n");
			sb.append("  - Concept Class ID Mismatches: ").append(stats.getConceptClassIdMismatches()).append("\n");
			sb.append("  - Valid Start Date Mismatches: ").append(stats.getValidStartDateMismatches()).append("\n");
			sb.append("  - Valid End Date Mismatches: ").append(stats.getValidEndDateMismatches()).append("\n");
		} else {
			sb.append("No differences found between the two sources.\n");
		}
		sb.append("\n");
	}

	private void appendReportStructure(StringBuilder sb, ConceptSetCompareJobEntity compareJob) {
		sb.append("REPORT STRUCTURE\n");
		sb.append("----------------\n");
		sb.append("summary.txt - Overall statistics summary\n");
		sb.append("README.txt - Overall log and runtime statistics\n");
		sb.append("all_included_codes_diff.csv - A diff report file for all of the affected concept sets in a single file – for Included Codes\n");
		sb.append("all_source_codes_diff.csv - A diff report file for all of the affected concept sets in a single file – for Included Source Codes\n");
		sb.append("concept_set_<ID>_diff.csv - A diff report file per concept set – for Included Codes\n");

		if (compareJob != null && compareJob.getCompareSourceCodes() != null && compareJob.getCompareSourceCodes()) {
			sb.append("concept_set_<ID>_source_codes_diff.csv - A diff report file per concept set – for Included Source Codes\n");
		}
		sb.append("\n");
	}

	private String formatDuration(Duration duration) {
		long hours = duration.toHours();
		long minutes = duration.toMinutes() % 60;
		long seconds = duration.getSeconds() % 60;

		if (hours > 0) {
			return String.format("%dh %dm %ds", hours, minutes, seconds);
		} else if (minutes > 0) {
			return String.format("%dm %ds", minutes, seconds);
		} else {
			return String.format("%ds", seconds);
		}
	}
}