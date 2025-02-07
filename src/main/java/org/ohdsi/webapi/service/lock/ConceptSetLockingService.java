package org.ohdsi.webapi.service.lock;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.BooleanUtils;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.vocabulary.Concept;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetComparison;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.service.lock.dto.ConceptSetSnapshotActionRequest;
import org.ohdsi.webapi.service.lock.dto.ConceptSetSnapshotParameters;
import org.ohdsi.webapi.service.lock.dto.GetConceptSetSnapshotItemsRequest;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.versioning.domain.ConceptSetVersion;
import org.ohdsi.webapi.versioning.domain.VersionBase;
import org.ohdsi.webapi.versioning.domain.VersionType;
import org.ohdsi.webapi.versioning.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class ConceptSetLockingService extends AbstractDaoService {
	@Value("${snapshot.history.source.connection}")
	private String snapshotHistorySourceConnection;
	@Value("${snapshot.history.source.schema}")
	private String snapshotHistorySourceSchema;
	@Value("${snapshot.history.source.dialect}")
	private String snapshotHistorySourceDialect;
	@Value("${snapshot.history.source.username}")
	private String snapshotHistorySourceUsername;
	@Value("${snapshot.history.source.password}")
	private String snapshotHistorySourcePassword;

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private VocabularyService vocabularyService;
	@Autowired
	private SourceRepository sourceRepository;
	@Autowired
	private VersionService<ConceptSetVersion> versionService;

	@Transactional
	public void invokeSnapshotAction(int conceptSetId, ConceptSetSnapshotActionRequest snapshotActionRequest, ConceptSetExpression conceptSetExpression, Collection<Concept> includedConcepts, Collection<Concept> includedSourceCodes) throws JsonProcessingException {
		Source snapshotHistorySource = prepareSnapshotHistorySource();

		ConceptSet conceptSet = getConceptSetRepository().findById(conceptSetId);
		if (conceptSet == null) {
			throw new RuntimeException("Concept Set does not exist.");
		}

		Source source = sourceRepository.findBySourceKey(snapshotActionRequest.getSourceKey());
		List<VersionBase> versions = versionService.getVersions(VersionType.CONCEPT_SET, conceptSetId);

		Timestamp lockedDate = Timestamp.from(Instant.now());

		String snapshotCreatedBy = snapshotActionRequest.getUser();

		String vocabularyBundleName = source.getSourceName();
		String vocabularyBundleSchema = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
		String vocabularyBundleVersion = vocabularyService.getInfo(snapshotActionRequest.getSourceKey()).version;
		String conceptSetVersion = Integer.toString(versions.stream()
			.mapToInt(VersionBase::getVersion)
			.max()
			.orElse(1));

		final Function<JdbcTemplate, TransactionCallback<Collection<ConceptSetComparison>>> callbackFunction =
			jdbcTemplate -> (TransactionCallback<Collection<ConceptSetComparison>>) transactionStatus -> {
				try {
					Long snapshotMetadataId = jdbcTemplate.queryForObject(
						"INSERT INTO " + snapshotHistorySourceSchema + ".CONCEPT_SET_SNAPSHOT_METADATA " +
							"(CONCEPT_SET_ID, ACTION, LOCKED_DATE, LOCKED_BY, MESSAGE, VOCABULARY_BUNDLE_NAME, " +
							"VOCABULARY_BUNDLE_SCHEMA, VOCABULARY_BUNDLE_VERSION, CONCEPT_SET_VERSION) " +
							"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING ID",
						new Object[]{conceptSetId, snapshotActionRequest.getAction(), lockedDate, snapshotCreatedBy, snapshotActionRequest.getMessage(), vocabularyBundleName,
							vocabularyBundleSchema, vocabularyBundleVersion, conceptSetVersion}, Long.class);

					if (conceptSetExpression != null && includedConcepts != null && includedSourceCodes != null) {
						Arrays.stream(conceptSetExpression.items).forEach(conceptSetItem -> saveConceptSetExpressionItemSnapshot(jdbcTemplate, conceptSetItem, snapshotMetadataId));
						includedConcepts.forEach(concept -> saveIncludedConceptSnapshot(jdbcTemplate, concept, snapshotMetadataId));
						includedSourceCodes.forEach(sourceCode -> saveIncludedSourceCodeSnapshot(jdbcTemplate, sourceCode, snapshotMetadataId));
					}
					return null;
				} catch (Exception ex) {
					log.error("An error occurred during snapshot creation", ex);
					throw ex;
				}
			};

		executeInTransaction(snapshotHistorySource, callbackFunction);
	}

	private Source prepareSnapshotHistorySource() {
		Source source = new Source();
		source.setSourceConnection(snapshotHistorySourceConnection);
		source.setSourceDialect(snapshotHistorySourceDialect);
		source.setUsername(snapshotHistorySourceUsername);
		source.setPassword(snapshotHistorySourcePassword);
		return source;
	}

	private void saveConceptSetExpressionItemSnapshot(JdbcTemplate jdbcTemplate, ConceptSetExpression.ConceptSetItem conceptSetItem, Long snapshotMetadataId) {
		jdbcTemplate.update("INSERT INTO " + snapshotHistorySourceSchema + ".CONCEPT_SET_ITEM_SNAPSHOTS " +
				"(SNAPSHOT_METADATA_ID, CONCEPT_ID, CONCEPT_NAME, DOMAIN_ID, VOCABULARY_ID, CONCEPT_CLASS_ID, STANDARD_CONCEPT, CONCEPT_CODE, VALID_START_DATE, VALID_END_DATE, INVALID_REASON, IS_EXCLUDED, INCLUDE_DESCENDANTS, INCLUDE_MAPPED) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			snapshotMetadataId, conceptSetItem.concept.conceptId, conceptSetItem.concept.conceptName, conceptSetItem.concept.domainId, conceptSetItem.concept.vocabularyId,
			conceptSetItem.concept.conceptClassId, conceptSetItem.concept.standardConcept, conceptSetItem.concept.conceptCode,
			((Concept) (conceptSetItem.concept)).validStartDate, ((Concept) (conceptSetItem.concept)).validEndDate, conceptSetItem.concept.invalidReason,
			BooleanUtils.toInteger(conceptSetItem.isExcluded),
			BooleanUtils.toInteger(conceptSetItem.includeDescendants),
			BooleanUtils.toInteger(conceptSetItem.includeMapped));
	}

	private void saveIncludedConceptSnapshot(JdbcTemplate jdbcTemplate, Concept concept, Long snapshotMetadataId) {
		jdbcTemplate.update("INSERT INTO " + snapshotHistorySourceSchema + ".INCLUDED_CONCEPTS_SNAPSHOTS " +
				"(SNAPSHOT_METADATA_ID, CONCEPT_ID, CONCEPT_NAME, DOMAIN_ID, VOCABULARY_ID, CONCEPT_CLASS_ID, STANDARD_CONCEPT, CONCEPT_CODE, VALID_START_DATE, VALID_END_DATE, INVALID_REASON) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			snapshotMetadataId, concept.conceptId, concept.conceptName, concept.domainId, concept.vocabularyId,
			concept.conceptClassId, concept.standardConcept, concept.conceptCode, concept.validStartDate, concept.validEndDate, concept.invalidReason);
	}

	private void saveIncludedSourceCodeSnapshot(JdbcTemplate jdbcTemplate, Concept concept, Long snapshotMetadataId) {
		jdbcTemplate.update("INSERT INTO " + snapshotHistorySourceSchema + ".INCLUDED_SOURCE_CODES_SNAPSHOTS " +
				"(SNAPSHOT_METADATA_ID, CONCEPT_ID, CONCEPT_NAME, DOMAIN_ID, VOCABULARY_ID, CONCEPT_CLASS_ID, STANDARD_CONCEPT, CONCEPT_CODE, VALID_START_DATE, VALID_END_DATE, INVALID_REASON) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			snapshotMetadataId, concept.conceptId, concept.conceptName, concept.domainId, concept.vocabularyId,
			concept.conceptClassId, concept.standardConcept, concept.conceptCode, concept.validStartDate, concept.validEndDate, concept.invalidReason);
	}

	@Transactional(readOnly = true)
	public Map<Integer, Boolean> areLocked(List<Integer> conceptSetIds) {
		Source lockHistorySource = prepareSnapshotHistorySource();
		if (lockHistorySource == null) {
			throw new RuntimeException("Snapshot/Lock history source not found");
		}
		Map<Integer, Boolean> lockedStatusMap = new HashMap<>();

		CancelableJdbcTemplate jdbcTemplate = getSourceJdbcTemplate(lockHistorySource);
		for (Integer conceptSetId : conceptSetIds) {
			String sql = "SELECT ACTION FROM " + snapshotHistorySourceSchema + ".CONCEPT_SET_SNAPSHOT_METADATA " +
				"WHERE CONCEPT_SET_ID = ? " +
				"ORDER BY LOCKED_DATE DESC " +    // Ordering by LOCKED_DATE
				"LIMIT 1";                      // Only interested in the most recent record

			List<String> lastAction = jdbcTemplate.query(sql, new Object[]{conceptSetId}, (rs, rowNum) -> rs.getString("ACTION"));
			lockedStatusMap.put(conceptSetId, !lastAction.isEmpty() && "LOCK".equals(lastAction.get(0)));
		}
		return lockedStatusMap;
	}

	@Transactional(readOnly = true)
	public List<ConceptSetSnapshotParameters> listSnapshotsByConceptSetId(Integer conceptSetId) {
		Source lockHistorySource = prepareSnapshotHistorySource();
		if (lockHistorySource == null) {
			throw new RuntimeException("Snapshot/Lock history source not found");
		}

		String sql = "SELECT ID, ACTION, LOCKED_DATE, LOCKED_BY, VOCABULARY_BUNDLE_NAME, VOCABULARY_BUNDLE_SCHEMA, " +
			"VOCABULARY_BUNDLE_VERSION, CONCEPT_SET_VERSION, MESSAGE FROM " + snapshotHistorySourceSchema + ".CONCEPT_SET_SNAPSHOT_METADATA " +
			"WHERE CONCEPT_SET_ID = ?";

		CancelableJdbcTemplate jdbcTemplate = getSourceJdbcTemplate(lockHistorySource);

		return jdbcTemplate.query(sql, new Object[]{conceptSetId}, (rs, rowNum) -> {
			ConceptSetSnapshotParameters snapshot = new ConceptSetSnapshotParameters();
			snapshot.setSnapshotId(rs.getLong("ID"));
			snapshot.setAction(rs.getString("ACTION"));
			snapshot.setSnapshotDate(rs.getString("LOCKED_DATE"));
			snapshot.setUser(rs.getString("LOCKED_BY"));
			snapshot.setVocabularyBundleName(rs.getString("VOCABULARY_BUNDLE_NAME"));
			snapshot.setVocabularyBundleSchema(rs.getString("VOCABULARY_BUNDLE_SCHEMA"));
			snapshot.setVocabularyBundleVersion(rs.getString("VOCABULARY_BUNDLE_VERSION"));
			snapshot.setConceptSetVersion(rs.getString("CONCEPT_SET_VERSION"));
			snapshot.setMessage(rs.getString("MESSAGE"));
			return snapshot;
		});
	}


	@Transactional(readOnly = true)
	public List<ConceptSetExpression.ConceptSetItem> getConceptSetSnapshotItemsBySnapshotId(int snapshotId, GetConceptSetSnapshotItemsRequest.ItemType type) {
		String tableName;
		switch (type) {
			case EXPRESSION_ITEMS:
				tableName = snapshotHistorySourceSchema + ".CONCEPT_SET_ITEM_SNAPSHOTS";
				break;
			case CONCEPTS:
				tableName = snapshotHistorySourceSchema + ".INCLUDED_CONCEPTS_SNAPSHOTS";
				break;
			case SOURCE_CODES:
				tableName = snapshotHistorySourceSchema + ".INCLUDED_SOURCE_CODES_SNAPSHOTS";
				break;
			default:
				throw new IllegalArgumentException("Invalid ItemType provided");
		}
		String sql;
		if (type == GetConceptSetSnapshotItemsRequest.ItemType.EXPRESSION_ITEMS) {
			sql = "SELECT CONCEPT_ID, CONCEPT_NAME, DOMAIN_ID, VOCABULARY_ID, CONCEPT_CLASS_ID, " +
				"STANDARD_CONCEPT, CONCEPT_CODE, VALID_START_DATE, VALID_END_DATE, " +
				"INVALID_REASON, IS_EXCLUDED, INCLUDE_DESCENDANTS, INCLUDE_MAPPED " +
				"FROM " + tableName + " WHERE SNAPSHOT_METADATA_ID = ?";
		} else {
			sql = "SELECT CONCEPT_ID, CONCEPT_NAME, DOMAIN_ID, VOCABULARY_ID, CONCEPT_CLASS_ID, " +
				"STANDARD_CONCEPT, CONCEPT_CODE, VALID_START_DATE, VALID_END_DATE, " +
				"INVALID_REASON FROM " + tableName + " WHERE SNAPSHOT_METADATA_ID = ?";
		}

		Source lockHistorySource = prepareSnapshotHistorySource();
		CancelableJdbcTemplate jdbcTemplate = getSourceJdbcTemplate(lockHistorySource);

		return jdbcTemplate.query(sql, new Object[]{snapshotId}, (rs, rowNum) -> convertToConceptSetItem(rs, type));
	}

	private ConceptSetExpression.ConceptSetItem convertToConceptSetItem(ResultSet rs, GetConceptSetSnapshotItemsRequest.ItemType type) throws SQLException {
		Concept concept = new Concept();
		concept.conceptId = rs.getLong("CONCEPT_ID");
		concept.conceptName = rs.getString("CONCEPT_NAME");
		concept.domainId = rs.getString("DOMAIN_ID");
		concept.vocabularyId = rs.getString("VOCABULARY_ID");
		concept.conceptClassId = rs.getString("CONCEPT_CLASS_ID");
		concept.standardConcept = rs.getString("STANDARD_CONCEPT");
		concept.conceptCode = rs.getString("CONCEPT_CODE");
		concept.validStartDate = rs.getTimestamp("VALID_START_DATE");
		concept.validEndDate = rs.getTimestamp("VALID_END_DATE");
		concept.invalidReason = rs.getString("INVALID_REASON");

		ConceptSetExpression.ConceptSetItem item = new ConceptSetExpression.ConceptSetItem();
		item.concept = concept;

		if (type == GetConceptSetSnapshotItemsRequest.ItemType.EXPRESSION_ITEMS) {
			item.isExcluded = rs.getBoolean("IS_EXCLUDED");
			item.includeDescendants = rs.getBoolean("INCLUDE_DESCENDANTS");
			item.includeMapped = rs.getBoolean("INCLUDE_MAPPED");
		}

		return item;
	}

}
