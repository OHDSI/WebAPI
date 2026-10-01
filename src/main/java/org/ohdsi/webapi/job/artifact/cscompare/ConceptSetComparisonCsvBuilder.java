package org.ohdsi.webapi.job.artifact.cscompare;

import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobDiffEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Handles CSV row generation for concept set comparison differences
 */
@Component
public class ConceptSetComparisonCsvBuilder {

	private static final Logger logger = LoggerFactory.getLogger(ConceptSetComparisonCsvBuilder.class);

	private final ConceptSetRepository conceptSetRepository;

	public ConceptSetComparisonCsvBuilder(ConceptSetRepository conceptSetRepository) {
		this.conceptSetRepository = conceptSetRepository;
	}

	/**
	 * Build CSV header with all fields
	 */
	public String buildHeader() {
		return "Concept Set ID," +
			"Concept Set Name," +
			"Concept ID," +
			"In CS1 Only," +
			"In CS2 Only," +
			"In Both CS," +
			"Name Mismatch," +
			"Base Concept Name," +
			"Target Concept Name," +
			"Standard Concept Mismatch," +
			"Base Standard Concept," +
			"Target Standard Concept," +
			"Invalid Reason Mismatch," +
			"Base Invalid Reason," +
			"Target Invalid Reason," +
			"Concept Code Mismatch," +
			"Base Concept Code," +
			"Target Concept Code," +
			"Domain ID Mismatch," +
			"Base Domain ID," +
			"Target Domain ID," +
			"Vocabulary ID Mismatch," +
			"Base Vocabulary ID," +
			"Target Vocabulary ID," +
			"Concept Class ID Mismatch," +
			"Base Concept Class ID," +
			"Target Concept Class ID," +
			"Valid Start Date Mismatch," +
			"Base Valid Start Date," +
			"Target Valid Start Date," +
			"Valid End Date Mismatch," +
			"Base Valid End Date," +
			"Target Valid End Date\n";
	}

	/**
	 * Build CSV row for a single diff entity
	 */
	public String buildRow(ConceptSetCompareJobDiffEntity diff) {
		Integer conceptSetId = diff.getConceptSetId();
		String conceptSetName = getConceptSetName(conceptSetId);

		StringBuilder row = new StringBuilder();

		// Basic info
		row.append(escapeCsv(conceptSetId.toString())).append(",");
		row.append(escapeCsv(conceptSetName)).append(",");
		row.append(escapeCsv(diff.getConceptId() != null ? diff.getConceptId().toString() : "")).append(",");

		// Concept set membership - using Yes/No format
		row.append(escapeCsv(booleanToYesNo(diff.getConceptInCS1Only() != null && diff.getConceptInCS1Only() > 0))).append(",");
		row.append(escapeCsv(booleanToYesNo(diff.getConceptInCS2Only() != null && diff.getConceptInCS2Only() > 0))).append(",");
		row.append(escapeCsv(booleanToYesNo(diff.getConceptInCS1AndCS2() != null && diff.getConceptInCS1AndCS2() > 0))).append(",");

		// Name mismatch
		row.append(escapeCsv(booleanToYesNo(diff.getNameMismatch()))).append(",");
		row.append(escapeCsv(diff.getVocab1ConceptName())).append(",");
		row.append(escapeCsv(diff.getVocab2ConceptName())).append(",");

		// Standard concept mismatch
		row.append(escapeCsv(booleanToYesNo(diff.getStandardConceptMismatch()))).append(",");
		row.append(escapeCsv(diff.getVocab1StandardConcept())).append(",");
		row.append(escapeCsv(diff.getVocab2StandardConcept())).append(",");

		// Invalid reason mismatch
		row.append(escapeCsv(booleanToYesNo(diff.getInvalidReasonMismatch()))).append(",");
		row.append(escapeCsv(diff.getVocab1InvalidReason())).append(",");
		row.append(escapeCsv(diff.getVocab2InvalidReason())).append(",");

		// Concept code mismatch
		row.append(escapeCsv(booleanToYesNo(diff.getConceptCodeMismatch()))).append(",");
		row.append(escapeCsv(diff.getVocab1ConceptCode())).append(",");
		row.append(escapeCsv(diff.getVocab2ConceptCode())).append(",");

		// Domain ID mismatch
		row.append(escapeCsv(booleanToYesNo(diff.getDomainIdMismatch()))).append(",");
		row.append(escapeCsv(diff.getVocab1DomainId())).append(",");
		row.append(escapeCsv(diff.getVocab2DomainId())).append(",");

		// Vocabulary ID mismatch
		row.append(escapeCsv(booleanToYesNo(diff.getVocabularyIdMismatch()))).append(",");
		row.append(escapeCsv(diff.getVocab1VocabularyId())).append(",");
		row.append(escapeCsv(diff.getVocab2VocabularyId())).append(",");

		// Concept class ID mismatch
		row.append(escapeCsv(booleanToYesNo(diff.getConceptClassIdMismatch()))).append(",");
		row.append(escapeCsv(diff.getVocab1ConceptClassId())).append(",");
		row.append(escapeCsv(diff.getVocab2ConceptClassId())).append(",");

		// Valid start date mismatch
		row.append(escapeCsv(booleanToYesNo(diff.getValidStartDateMismatch()))).append(",");
		row.append(escapeCsv(diff.getVocab1ValidStartDate() != null ? diff.getVocab1ValidStartDate().toString() : "")).append(",");
		row.append(escapeCsv(diff.getVocab2ValidStartDate() != null ? diff.getVocab2ValidStartDate().toString() : "")).append(",");

		// Valid end date mismatch
		row.append(escapeCsv(booleanToYesNo(diff.getValidEndDateMismatch()))).append(",");
		row.append(escapeCsv(diff.getVocab1ValidEndDate() != null ? diff.getVocab1ValidEndDate().toString() : "")).append(",");
		row.append(escapeCsv(diff.getVocab2ValidEndDate() != null ? diff.getVocab2ValidEndDate().toString() : ""));

		row.append("\n");

		return row.toString();
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

	private String escapeCsv(String value) {
		if (value == null) {
			return "";
		}
		if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
			return "\"" + value.replace("\"", "\"\"") + "\"";
		}
		return value;
	}

	/**
	 * Convert boolean to Yes/No string
	 */
	private String booleanToYesNo(Boolean value) {
		if (value == null) {
			return "No";
		}
		return value ? "Yes" : "No";
	}
}