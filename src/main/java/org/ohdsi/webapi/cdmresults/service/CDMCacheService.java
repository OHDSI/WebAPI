package org.ohdsi.webapi.cdmresults.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ohdsi.webapi.cdmresults.DescendantRecordCount;
import org.ohdsi.webapi.cdmresults.domain.CDMCacheEntity;
import org.ohdsi.webapi.cdmresults.mapper.BaseRecordCountMapper;
import org.ohdsi.webapi.cdmresults.mapper.DescendantRecordAndPersonCountMapper;
import org.ohdsi.webapi.cdmresults.mapper.DescendantRecordCountMapper;
import org.ohdsi.webapi.cdmresults.repository.CDMCacheRepository;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedSqlRender;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CDMCacheService extends AbstractDaoService {
    private static final int COUNTS_BATCH_SIZE = 1_000_000;
    private static final int COUNTS_BATCH_THRESHOLD = 250_000;
    private static final String CONCEPT_SQL = "/resources/cdmresults/sql/getConcepts.sql";
    private static final String PARTIAL_CONCEPT_COUNT_SQL = "/resources/cdmresults/sql/getConceptRecordCount.sql";
    private static final String PARTIAL_CONCEPT_COUNT_PERSON_SQL = "/resources/cdmresults/sql/getConceptRecordPersonCount.sql";
    private static final String BATCH_CONCEPT_COUNT_SQL = "/resources/cdmresults/sql/getBatchConceptRecordCount.sql";
    private static final String BATCH_CONCEPT_COUNT_PERSON_SQL = "/resources/cdmresults/sql/getBatchConceptRecordPersonCount.sql";

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchSize;

    @Value("${cache.achilles.usePersonCount:false}")
    private boolean usePersonCount;

    private final CDMCacheBatchService cdmCacheBatchService;

    private final CDMCacheRepository cdmCacheRepository;

    private final ConversionService conversionService;

    public CDMCacheService(CDMCacheBatchService cdmCacheBatchService,
                           ConversionService conversionService,
                           CDMCacheRepository cdmCacheRepository) {
        this.cdmCacheBatchService = cdmCacheBatchService;
        this.conversionService = conversionService;
        this.cdmCacheRepository = cdmCacheRepository;
    }

    public void warm(Source source) {
        try {
            TransactionTemplate transactionTemplate = getTransactionTemplateRequiresNew();

            transactionTemplate.execute(s -> {
                // Create full cache
                cacheRecords(source, null);
                return null;
            });
        } catch (Exception ex) {
            log.error("Failed to warm cache {}. Exception: {}", source.getSourceKey(), ex.getLocalizedMessage());
        }
    }

    public List<CDMCacheEntity> findAndCache(Source source, List<Integer> conceptIds) {
        if (CollectionUtils.isEmpty(conceptIds)) {
            return Collections.emptyList();
        }

        List<CDMCacheEntity> cacheEntities = find(source, conceptIds);
        // Get list of concept identifiers
        List<Integer> cacheConceptIds = cacheEntities.stream()
                .map(CDMCacheEntity::getConceptId)
                .collect(Collectors.toList());
        // If number of cached entities is not equal to the number of requested entities then
        // we have to get data from source
        if (cacheEntities.size() != conceptIds.size()) {
            List<Integer> cacheAbsentConceptIds = new ArrayList<>(CollectionUtils.subtract(conceptIds, cacheConceptIds));
            cacheRecords(source, cacheAbsentConceptIds);
            List<CDMCacheEntity> cacheAbsentEntities = find(source, cacheAbsentConceptIds);
            cacheEntities.addAll(cacheAbsentEntities);
        }

        return cacheEntities;
    }

    private List<CDMCacheEntity> find(Source source, List<Integer> conceptIds) {
        if (CollectionUtils.isEmpty(conceptIds)) {
            return Collections.emptyList();
        }

        int start = 0, size = conceptIds.size();
        List<CDMCacheEntity> cacheEntities = new ArrayList<>();
        int parameterLimit = PreparedSqlRender.getParameterLimit(source);
        // Get cached entities by calling the query for small pieces of 
        // the array of concept identifiers
        while (start < size) {
            int end = Math.min(start + parameterLimit, size);
            List<Integer> idsSlice = conceptIds.subList(start, end);
            start += parameterLimit;
            cacheEntities.addAll(cdmCacheRepository.findBySourceAndConceptIds(source.getSourceId(), idsSlice));
        }
        return cacheEntities;
    }

    private void cacheRecords(Source source, List<Integer> ids) {
        BaseRecordCountMapper<?> mapper = this.usePersonCount ? new DescendantRecordAndPersonCountMapper() : new DescendantRecordCountMapper();
        JdbcTemplate jdbcTemplate = this.getSourceJdbcTemplate(source);
        if (ids == null) {
            // Full cache
            // Make sure that query returns ordered collection of ids or sort it after query is executed
            PreparedStatementRenderer cpsr = getConceptPreparedStatementRenderer(source);
            ids = jdbcTemplate.query(cpsr.getSql(), cpsr.getSetter(), (rs, rowNum) -> rs.getInt(1));
            List<Pair<Integer, Integer>> minMaxPairs = new ArrayList<>();
            int start = 0;
            // Get ranges of minimal and maximum concept identifiers to use in query
            while (start < ids.size()) {
                int end = Math.min(ids.size() - 1, start + COUNTS_BATCH_SIZE - 1);
                // If we have small number of concepts for the next range - add them to the current range
                if (end + COUNTS_BATCH_THRESHOLD >= ids.size() - 1) {
                    end = ids.size() - 1;
                }
                minMaxPairs.add(new ImmutablePair<>(ids.get(start), ids.get(end)));
                start = end + 1;
            }
            // Clear list of identifiers so the GC can collect them
            ids.clear();
            minMaxPairs.forEach(pair -> {
                PreparedStatementRenderer psr = getBatchPreparedStatementRenderer(source, pair.getLeft(), pair.getRight());
                cacheRecords(source, psr, mapper, jdbcTemplate);
            });
        } else {
            // In case of getting records for concrete concepts we must call the query for small pieces of 
            // the array of concept identifiers
            // Take into account the fact that the identifiers are used in 2
            // places in the target query so the parameter limit will need to be divided
            int parameterLimit = Math.floorDiv(PreparedSqlRender.getParameterLimit(source), 2);
            int start = 0, size = ids.size();
            while (start < size) {
                int end = Math.min(start + parameterLimit, size);
                List<Integer> idsSlice = ids.subList(start, end);
                start += parameterLimit;
                PreparedStatementRenderer psr = getPartialPreparedStatementRenderer(source, idsSlice);
                cacheRecords(source, psr, mapper, jdbcTemplate);
            }
        }
    }

    private void cacheRecords(Source source, PreparedStatementRenderer psr,
                              BaseRecordCountMapper<?> mapper, JdbcTemplate jdbcTemplate) {
        List<CDMCacheEntity> rows = new ArrayList<>(batchSize);
        jdbcTemplate.setFetchSize(2000);
        jdbcTemplate.query(psr.getSql(), psr.getSetter(), resultSet -> {
            DescendantRecordCount row = mapper.mapRow(resultSet);
            CDMCacheEntity cacheEntity = conversionService.convert(row, CDMCacheEntity.class);
            rows.add(cacheEntity);
            if (rows.size() == batchSize) {
                // Persist or merge batch
                cdmCacheBatchService.save(source, rows);
                rows.clear();
            }
        });
        if (rows.size() > 0) {
            cdmCacheBatchService.save(source, rows);
        }
    }

    private PreparedStatementRenderer getConceptPreparedStatementRenderer(Source source) {
        String resultTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        String[] tables = {"resultTableQualifier", "vocabularyTableQualifier"};
        String[] tableValues = {resultTableQualifier, vocabularyTableQualifier};

        return new PreparedStatementRenderer(source, CONCEPT_SQL, tables, tableValues,
                SessionUtils.sessionId());
    }

    private PreparedStatementRenderer getPartialPreparedStatementRenderer(Source source, List<Integer> ids) {
        String resultTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        String[] tables = {"resultTableQualifier", "vocabularyTableQualifier"};
        String[] tableValues = {resultTableQualifier, vocabularyTableQualifier};
        // Caching of concrete concepts
        String sqlPath = this.usePersonCount ? PARTIAL_CONCEPT_COUNT_PERSON_SQL : PARTIAL_CONCEPT_COUNT_SQL;
        Integer[] identifiers = ids.toArray(new Integer[0]);
        
        return new PreparedStatementRenderer(source, sqlPath, tables, tableValues,
                "conceptIdentifiers", identifiers);
    }

    private PreparedStatementRenderer getBatchPreparedStatementRenderer(Source source, int min, int max) {
        String resultTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        String[] tables = {"resultTableQualifier", "vocabularyTableQualifier"};
        String[] tableValues = {resultTableQualifier, vocabularyTableQualifier};
        // Caching of concrete concepts
        String sqlPath = this.usePersonCount ? BATCH_CONCEPT_COUNT_PERSON_SQL : BATCH_CONCEPT_COUNT_SQL;
        String[] variables = new String[]{"conceptIdentifierMin", "conceptIdentifierMax"};
        Integer[] identifiers = new Integer[]{min, max};
        
        return new PreparedStatementRenderer(source, sqlPath, tables, tableValues,
                variables, identifiers);
    }
}
