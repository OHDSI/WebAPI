package org.ohdsi.webapi.service.lock;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.BooleanUtils;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.vocabulary.Concept;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetComparison;
import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.service.dto.LockedConceptSetsResponse;
import org.ohdsi.webapi.service.lock.dto.ConceptSetSnapshotActionRequest;
import org.ohdsi.webapi.service.lock.dto.ConceptSetSnapshotParameters;
import org.ohdsi.webapi.service.lock.dto.GetConceptSetSnapshotItemsRequest;
import org.ohdsi.webapi.service.lock.dto.SnapshotAction;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.versioning.domain.ConceptSetVersion;
import org.ohdsi.webapi.versioning.domain.VersionBase;
import org.ohdsi.webapi.versioning.domain.VersionType;
import org.ohdsi.webapi.versioning.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ConceptSetLockingService extends AbstractDaoService {

    private static final String LOCKED_CONCEPT_SET_TAG_NAME = "Locked Concept Set";

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private VocabularyService vocabularyService;
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private VersionService<ConceptSetVersion> versionService;
    @Autowired
    private SnapshotHistorySourceProvider snapshotHistorySourceProvider;
    @Autowired
    private GenericConversionService conversionService;
    @Autowired
    private PermissionService permissionService;


    @Transactional
    public void invokeSnapshotAction(int conceptSetId, ConceptSetSnapshotActionRequest snapshotActionRequest, Supplier<ConceptSetExpression> conceptSetExpressionSupplier) {
        ConceptSet conceptSet = getConceptSetRepository().findById(conceptSetId);
        if (conceptSet == null) {
            throw new RuntimeException("Concept Set does not exist.");
        }

        Source source = sourceRepository.findBySourceKey(snapshotActionRequest.getSourceKey());
        List<VersionBase> versions = versionService.getVersions(VersionType.CONCEPT_SET, conceptSetId);

        Timestamp lockedDate = Timestamp.from(Instant.now());

        String snapshotCreatedBy = security.getSubject();

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
                                String.format("INSERT INTO %s.CONCEPT_SET_SNAPSHOT_METADATA " +
                                        "(CONCEPT_SET_ID, ACTION, ACTION_DATE, CREATED_BY, MESSAGE, VOCABULARY_BUNDLE_NAME, " +
                                        "VOCABULARY_BUNDLE_SCHEMA, VOCABULARY_BUNDLE_VERSION, CONCEPT_SET_VERSION) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING ID", snapshotHistorySourceProvider.getSnapshotHistorySourceSchema()),
                                new Object[]{conceptSetId, snapshotActionRequest.getAction().toString(), lockedDate, snapshotCreatedBy, snapshotActionRequest.getMessage(), vocabularyBundleName,
                                        vocabularyBundleSchema, vocabularyBundleVersion, conceptSetVersion}, Long.class);

                        if (snapshotActionRequest.isTakeSnapshot()) {
                            ConceptSetExpression conceptSetExpression = conceptSetExpressionSupplier.get();
                            Arrays.stream(conceptSetExpression.items).forEach(conceptSetItem -> saveConceptSetExpressionItemSnapshot(jdbcTemplate, conceptSetItem, snapshotMetadataId));

                            vocabularyService.executeIncludedConceptLookup(snapshotActionRequest.getSourceKey(), conceptSetExpression)
                                    .forEach(concept -> saveIncludedSnapshotItem(jdbcTemplate, concept, snapshotMetadataId, "INCLUDED_CONCEPTS_SNAPSHOTS"));

                            vocabularyService.executeMappedLookup(snapshotActionRequest.getSourceKey(), conceptSetExpression)
                                    .forEach(sourceCode -> saveIncludedSnapshotItem(jdbcTemplate, sourceCode, snapshotMetadataId, "INCLUDED_SOURCE_CODES_SNAPSHOTS"));
                        }
                        return null;
                    } catch (Exception ex) {
                        log.error("An error occurred during snapshot creation", ex);
                        throw ex;
                    }
                };

        executeInTransaction(snapshotHistorySourceProvider.getSnapshotHistorySource(), callbackFunction);

        updateLockedTag(conceptSet, snapshotActionRequest.getAction());
    }

    private void updateLockedTag(ConceptSet conceptSet, SnapshotAction snapshotAction) {
        try {
            getTagService().listInfo(LOCKED_CONCEPT_SET_TAG_NAME)
                    .stream()
                    .findFirst()
                    .ifPresent(lockedConceptSetTag -> {
                        if (snapshotAction == SnapshotAction.LOCK) {
                            assignTag(conceptSet, lockedConceptSetTag.getId());
                        } else {
                            unassignTag(conceptSet, lockedConceptSetTag.getId());
                        }
                    });
        } catch (Exception e) {
            log.error("Unable to update the concept set Locked Tag", e);
        }
    }


    private void saveConceptSetExpressionItemSnapshot(JdbcTemplate jdbcTemplate, ConceptSetExpression.ConceptSetItem conceptSetItem, Long snapshotMetadataId) {
        jdbcTemplate.update(String.format("INSERT INTO %s.CONCEPT_SET_ITEM_SNAPSHOTS " +
                        "(SNAPSHOT_METADATA_ID, CONCEPT_ID, CONCEPT_NAME, DOMAIN_ID, VOCABULARY_ID, CONCEPT_CLASS_ID, STANDARD_CONCEPT, CONCEPT_CODE, VALID_START_DATE, VALID_END_DATE, INVALID_REASON, IS_EXCLUDED, INCLUDE_DESCENDANTS, INCLUDE_MAPPED) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", snapshotHistorySourceProvider.getSnapshotHistorySourceSchema()),
                snapshotMetadataId, conceptSetItem.concept.conceptId, conceptSetItem.concept.conceptName, conceptSetItem.concept.domainId, conceptSetItem.concept.vocabularyId,
                conceptSetItem.concept.conceptClassId, conceptSetItem.concept.standardConcept, conceptSetItem.concept.conceptCode,
                ((Concept) (conceptSetItem.concept)).validStartDate, ((Concept) (conceptSetItem.concept)).validEndDate, conceptSetItem.concept.invalidReason,
                BooleanUtils.toInteger(conceptSetItem.isExcluded),
                BooleanUtils.toInteger(conceptSetItem.includeDescendants),
                BooleanUtils.toInteger(conceptSetItem.includeMapped));
    }

    private void saveIncludedSnapshotItem(JdbcTemplate jdbcTemplate, Concept concept, Long snapshotMetadataId, String snapshotItemTableName) {
        jdbcTemplate.update(String.format("INSERT INTO %s.%s (SNAPSHOT_METADATA_ID, CONCEPT_ID, CONCEPT_NAME, DOMAIN_ID, VOCABULARY_ID, CONCEPT_CLASS_ID, STANDARD_CONCEPT, CONCEPT_CODE, VALID_START_DATE, VALID_END_DATE, INVALID_REASON) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", snapshotHistorySourceProvider.getSnapshotHistorySourceSchema(), snapshotItemTableName),
                snapshotMetadataId, concept.conceptId, concept.conceptName, concept.domainId, concept.vocabularyId,
                concept.conceptClassId, concept.standardConcept, concept.conceptCode, concept.validStartDate, concept.validEndDate, concept.invalidReason);
    }

    @Transactional(readOnly = true)
    public Map<Integer, Boolean> areLocked(List<Integer> conceptSetIds) {
        Map<Integer, Boolean> lockedStatusMap = new HashMap<>();
        CancelableJdbcTemplate jdbcTemplate = getSourceJdbcTemplate(snapshotHistorySourceProvider.getSnapshotHistorySource());
        for (Integer conceptSetId : conceptSetIds) {
            String sql = String.format("SELECT ACTION FROM %s.CONCEPT_SET_SNAPSHOT_METADATA " +
                    "WHERE CONCEPT_SET_ID = ? " +
                    "ORDER BY ACTION_DATE DESC " +    // Ordering by ACTION_DATE, only interested in the most recent record
                    "LIMIT 1", snapshotHistorySourceProvider.getSnapshotHistorySourceSchema());
            List<String> lastAction = jdbcTemplate.query(sql, new Object[]{conceptSetId}, (rs, rowNum) -> rs.getString("ACTION"));
            lockedStatusMap.put(conceptSetId, !lastAction.isEmpty() && SnapshotAction.LOCK.toString().equals(lastAction.get(0)));
        }
        return lockedStatusMap;
    }

    @Transactional(readOnly = true)
    public ConceptSetSnapshotParameters getLastSnapshotByConceptSetId(Integer conceptSetId) {
        String sql = String.format("SELECT ID, ACTION, ACTION_DATE, CREATED_BY, VOCABULARY_BUNDLE_NAME, VOCABULARY_BUNDLE_SCHEMA, " +
                "VOCABULARY_BUNDLE_VERSION, CONCEPT_SET_VERSION, MESSAGE FROM %s.CONCEPT_SET_SNAPSHOT_METADATA " +
                "WHERE CONCEPT_SET_ID = ? ORDER BY ACTION_DATE DESC LIMIT 1", snapshotHistorySourceProvider.getSnapshotHistorySourceSchema());

        CancelableJdbcTemplate jdbcTemplate = getSourceJdbcTemplate(snapshotHistorySourceProvider.getSnapshotHistorySource());
        return Iterables.getOnlyElement(jdbcTemplate.query(sql, new Object[]{conceptSetId}, (rs, rowNum) -> {
            ConceptSetSnapshotParameters snapshot = new ConceptSetSnapshotParameters();
            snapshot.setSnapshotId(rs.getLong("ID"));
            snapshot.setAction(SnapshotAction.valueOf(rs.getString("ACTION")));
            snapshot.setSnapshotDate(rs.getString("ACTION_DATE"));
            snapshot.setUser(rs.getString("CREATED_BY"));
            snapshot.setVocabularyBundleName(rs.getString("VOCABULARY_BUNDLE_NAME"));
            snapshot.setVocabularyBundleSchema(rs.getString("VOCABULARY_BUNDLE_SCHEMA"));
            snapshot.setVocabularyBundleVersion(rs.getString("VOCABULARY_BUNDLE_VERSION"));
            snapshot.setConceptSetVersion(rs.getString("CONCEPT_SET_VERSION"));
            snapshot.setMessage(rs.getString("MESSAGE"));
            return snapshot;
        }));
    }

    @Transactional(readOnly = true)
    public List<ConceptSetSnapshotParameters> listSnapshotsByConceptSetId(Integer conceptSetId) {
        String sql = String.format("SELECT ID, ACTION, ACTION_DATE, CREATED_BY, VOCABULARY_BUNDLE_NAME, VOCABULARY_BUNDLE_SCHEMA, " +
                "VOCABULARY_BUNDLE_VERSION, CONCEPT_SET_VERSION, MESSAGE FROM %s.CONCEPT_SET_SNAPSHOT_METADATA " +
                "WHERE CONCEPT_SET_ID = ? ORDER BY ACTION_DATE DESC", snapshotHistorySourceProvider.getSnapshotHistorySourceSchema());

        CancelableJdbcTemplate jdbcTemplate = getSourceJdbcTemplate(snapshotHistorySourceProvider.getSnapshotHistorySource());
        return jdbcTemplate.query(sql, new Object[]{conceptSetId}, (rs, rowNum) -> toConceptSetSnapshotParameters(rs));
    }

    private ConceptSetSnapshotParameters toConceptSetSnapshotParameters(ResultSet rs) throws SQLException {
        ConceptSetSnapshotParameters snapshot = new ConceptSetSnapshotParameters();
        snapshot.setSnapshotId(rs.getLong("ID"));
        snapshot.setAction(SnapshotAction.valueOf(rs.getString("ACTION")));
        snapshot.setSnapshotDate(rs.getString("ACTION_DATE"));
        snapshot.setUser(rs.getString("CREATED_BY"));
        snapshot.setVocabularyBundleName(rs.getString("VOCABULARY_BUNDLE_NAME"));
        snapshot.setVocabularyBundleSchema(rs.getString("VOCABULARY_BUNDLE_SCHEMA"));
        snapshot.setVocabularyBundleVersion(rs.getString("VOCABULARY_BUNDLE_VERSION"));
        snapshot.setConceptSetVersion(rs.getString("CONCEPT_SET_VERSION"));
        snapshot.setMessage(rs.getString("MESSAGE"));
        return snapshot;
    }


    @Transactional(readOnly = true)
    public List<ConceptSetExpression.ConceptSetItem> getConceptSetSnapshotItemsBySnapshotId(int snapshotId, GetConceptSetSnapshotItemsRequest.ItemType type) {
        String tableName;
        switch (type) {
            case EXPRESSION_ITEMS:
                tableName = String.format("%s.CONCEPT_SET_ITEM_SNAPSHOTS", snapshotHistorySourceProvider.getSnapshotHistorySourceSchema());
                break;
            case CONCEPTS:
                tableName = String.format("%s.INCLUDED_CONCEPTS_SNAPSHOTS", snapshotHistorySourceProvider.getSnapshotHistorySourceSchema());
                break;
            case SOURCE_CODES:
                tableName = String.format("%s.INCLUDED_SOURCE_CODES_SNAPSHOTS", snapshotHistorySourceProvider.getSnapshotHistorySourceSchema());
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

        CancelableJdbcTemplate jdbcTemplate = getSourceJdbcTemplate(snapshotHistorySourceProvider.getSnapshotHistorySource());
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

    private Map<Integer, ConceptSetSnapshotParameters> getLockedConceptSetIdsWithMetadata() {
        String sql = String.format(
                "SELECT cs.ID, cs.ACTION, cs.ACTION_DATE, cs.CREATED_BY, cs.VOCABULARY_BUNDLE_NAME, " +
                        "cs.VOCABULARY_BUNDLE_SCHEMA, cs.VOCABULARY_BUNDLE_VERSION, cs.CONCEPT_SET_VERSION, cs.MESSAGE, cs.CONCEPT_SET_ID " +
                        "FROM %s.CONCEPT_SET_SNAPSHOT_METADATA cs " +
                        "WHERE cs.CONCEPT_SET_ID IN (" +
                        "    SELECT subcs.CONCEPT_SET_ID FROM %s.CONCEPT_SET_SNAPSHOT_METADATA subcs " +
                        "    WHERE subcs.ACTION_DATE = (" +
                        "        SELECT MAX(subsubcs.ACTION_DATE) FROM %s.CONCEPT_SET_SNAPSHOT_METADATA subsubcs " +
                        "        WHERE subsubcs.CONCEPT_SET_ID = subcs.CONCEPT_SET_ID" +
                        "    ) AND subcs.ACTION = 'LOCK' " +
                        ") AND cs.ACTION = 'LOCK' " +
                        "ORDER BY cs.ACTION_DATE DESC",
                snapshotHistorySourceProvider.getSnapshotHistorySourceSchema(),
                snapshotHistorySourceProvider.getSnapshotHistorySourceSchema(),
                snapshotHistorySourceProvider.getSnapshotHistorySourceSchema());

        CancelableJdbcTemplate jdbcTemplate = getSourceJdbcTemplate(snapshotHistorySourceProvider.getSnapshotHistorySource());

        Map<Integer, ConceptSetSnapshotParameters> resultMap = jdbcTemplate.query(sql, rs -> {
            HashMap<Integer, ConceptSetSnapshotParameters> map = new HashMap<>();
            while (rs.next()) {
                Integer conceptSetId = rs.getInt("CONCEPT_SET_ID");
                ConceptSetSnapshotParameters snapshot = toConceptSetSnapshotParameters(rs);
                map.put(conceptSetId, snapshot);
            }
            return map;
        });
        return resultMap;
    }

    public Collection<LockedConceptSetsResponse> getLockedConceptSets(boolean defaultGlobalReadPermissions) {
        Map<Integer, ConceptSetSnapshotParameters> lockedConceptSetMetadataByConceptSetId = getLockedConceptSetIdsWithMetadata();
        return getTransactionTemplate().execute(
                transactionStatus -> StreamSupport.stream(getConceptSetRepository().findAll(lockedConceptSetMetadataByConceptSetId.keySet()).spliterator(), false)
                        .filter(!defaultGlobalReadPermissions ? entity -> permissionService.hasReadAccess(entity) : entity -> true)
                        .map(conceptSet -> {
                            ConceptSetDTO dto = conversionService.convert(conceptSet, ConceptSetDTO.class);
                            permissionService.fillWriteAccess(conceptSet, dto);
                            permissionService.fillReadAccess(conceptSet, dto);
                            return dto;
                        }).map(dto -> new LockedConceptSetsResponse(dto, lockedConceptSetMetadataByConceptSetId.get(dto.getId())))
                        .collect(Collectors.toList()));
    }
}
