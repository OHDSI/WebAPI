package org.ohdsi.webapi.job.artifact.cscompare;

import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobDiffEntity;
import org.ohdsi.webapi.util.GenericFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates CSV files for concept set comparison differences
 */
@Component
public class CsvFileGenerator {

	private static final Logger logger = LoggerFactory.getLogger(CsvFileGenerator.class);

	private final GenericFileWriter fileWriter;
	private final ConceptSetComparisonCsvBuilder csvBuilder;

	public CsvFileGenerator(GenericFileWriter fileWriter, ConceptSetComparisonCsvBuilder csvBuilder) {
		this.fileWriter = fileWriter;
		this.csvBuilder = csvBuilder;
	}

	/**
	 * Generate consolidated CSV for included codes
	 */
	public void generateIncludedCodesConsolidatedCsv(Path workDir, List<ConceptSetCompareJobDiffEntity> allDiffs) throws IOException {
		List<ConceptSetCompareJobDiffEntity> includedCodesDiffs = allDiffs.stream()
			.filter(d -> d.getIsSourceCode() == null || !d.getIsSourceCode())
			.collect(Collectors.toList());

		if (includedCodesDiffs.isEmpty()) {
			logger.info("No included codes differences found, skipping all_included_codes_diff.csv");
			return;
		}

		StringBuilder csv = new StringBuilder();
		csv.append(csvBuilder.buildHeader());

		for (ConceptSetCompareJobDiffEntity diff : includedCodesDiffs) {
			csv.append(csvBuilder.buildRow(diff));
		}

		Path csvPath = workDir.resolve("all_included_codes_diff.csv");
		fileWriter.writeTextFile(csvPath, pw -> pw.print(csv.toString()));
		logger.debug("Created included codes consolidated diff CSV at {}", csvPath);
	}

	/**
	 * Generate consolidated CSV for source codes
	 */
	public void generateSourceCodesConsolidatedCsv(Path workDir, List<ConceptSetCompareJobDiffEntity> allDiffs) throws IOException {
		List<ConceptSetCompareJobDiffEntity> sourceCodesDiffs = allDiffs.stream()
			.filter(d -> d.getIsSourceCode() != null && d.getIsSourceCode())
			.collect(Collectors.toList());

		if (sourceCodesDiffs.isEmpty()) {
			logger.info("No source codes differences found, skipping all_source_codes_diff.csv");
			return;
		}

		StringBuilder csv = new StringBuilder();
		csv.append(csvBuilder.buildHeader());

		for (ConceptSetCompareJobDiffEntity diff : sourceCodesDiffs) {
			csv.append(csvBuilder.buildRow(diff));
		}

		Path csvPath = workDir.resolve("all_source_codes_diff.csv");
		fileWriter.writeTextFile(csvPath, pw -> pw.print(csv.toString()));
		logger.debug("Created source codes consolidated diff CSV at {}", csvPath);
	}

	/**
	 * Generate individual CSV files per concept set
	 */
	public void generatePerConceptSetCsvs(Path workDir, List<ConceptSetCompareJobDiffEntity> allDiffs,
																				boolean isSourceCode) throws IOException {
		// Filter diffs based on isSourceCode flag
		List<ConceptSetCompareJobDiffEntity> filteredDiffs = allDiffs.stream()
			.filter(d -> {
				if (isSourceCode) {
					return d.getIsSourceCode() != null && d.getIsSourceCode();
				} else {
					return d.getIsSourceCode() == null || !d.getIsSourceCode();
				}
			})
			.collect(Collectors.toList());

		if (filteredDiffs.isEmpty()) {
			logger.info("No {} differences found", isSourceCode ? "source code" : "included concept");
			return;
		}

		// Group diffs by concept set ID
		Map<Integer, List<ConceptSetCompareJobDiffEntity>> diffsByConceptSet = filteredDiffs.stream()
			.collect(Collectors.groupingBy(ConceptSetCompareJobDiffEntity::getConceptSetId));

		logger.info("Creating individual CSV files for {} concept sets ({} mode)",
			diffsByConceptSet.size(),
			isSourceCode ? "source code" : "included concept");

		for (Map.Entry<Integer, List<ConceptSetCompareJobDiffEntity>> entry : diffsByConceptSet.entrySet()) {
			Integer conceptSetId = entry.getKey();
			List<ConceptSetCompareJobDiffEntity> diffs = entry.getValue();

			String filename = isSourceCode
				? String.format("concept_set_%d_source_codes_diff.csv", conceptSetId)
				: String.format("concept_set_%d_diff.csv", conceptSetId);

			generateSingleConceptSetCsv(workDir, filename, diffs);
		}
	}

	private void generateSingleConceptSetCsv(Path workDir, String filename,
																					 List<ConceptSetCompareJobDiffEntity> diffs) throws IOException {
		StringBuilder csv = new StringBuilder();
		csv.append(csvBuilder.buildHeader());

		for (ConceptSetCompareJobDiffEntity diff : diffs) {
			csv.append(csvBuilder.buildRow(diff));
		}

		Path csvPath = workDir.resolve(filename);
		fileWriter.writeTextFile(csvPath, pw -> pw.print(csv.toString()));
		logger.debug("Created concept set diff CSV at {}", csvPath);
	}
}