/*
 * Copyright 2017 fdefalco.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.cdmresults;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.webapi.cache.ResultsCache;
import org.ohdsi.webapi.cdmresults.cache.CDMResultsCache;
import org.ohdsi.webapi.cdmresults.mapper.BaseRecordCountMapper;
import org.ohdsi.webapi.cdmresults.mapper.DescendantRecordAndPersonCountMapper;
import org.ohdsi.webapi.cdmresults.mapper.DescendantRecordCountMapper;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author fdefalco
 */
public class CDMResultsCacheTasklet implements Tasklet {
    private static final Logger log = LoggerFactory.getLogger(CDMResultsCacheTasklet.class);

    private static final String FALLBACK_CONCEPT_COUNT_SQL =
            "/resources/cdmresults/sql/loadConceptRecordCountCacheFull.sql";
    private static final String FALLBACK_CONCEPT_COUNT_PERSON_SQL =
            "/resources/cdmresults/sql/loadConceptRecordPersonCountCacheFull.sql";
    private static final String CONCEPT_COUNT_SQL =
            "/resources/cdmresults/sql/loadConceptRecordCountCache.sql";
    private static final String CONCEPT_COUNT_PERSON_SQL =
            "/resources/cdmresults/sql/loadConceptRecordPersonCountCache.sql";

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final Source source;
    private final DescendantRecordCountMapper descendantRecordCountMapper;
    private final boolean usePersonCount;

    public CDMResultsCacheTasklet(final JdbcTemplate t, final TransactionTemplate transactionTemplate, final Source s,
                                  boolean usePersonCount) {
        jdbcTemplate = t;
        this.transactionTemplate = transactionTemplate;
        source = s;
        descendantRecordCountMapper = new DescendantRecordCountMapper();
        this.usePersonCount = usePersonCount;
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) {
        CDMResultsCache cdmResultsCache = new CDMResultsCache();
        warm(cdmResultsCache);

        ResultsCache.getAll().put(source.getSourceKey(), cdmResultsCache);
        return RepeatStatus.FINISHED;
    }
    
    private void warm(CDMResultsCache cdmResultsCache) {
        try {
            String sqlPath = this.usePersonCount ? CONCEPT_COUNT_PERSON_SQL : CONCEPT_COUNT_SQL;
            warmCdmCache(cdmResultsCache, sqlPath);
        } catch (Exception e) {
            log.warn("Failed to warm cache for {}. Trying to execute caching from scratch. Exception: {}",
                    source.getSourceKey(), e.getLocalizedMessage());
            try {
                String sqlPath = this.usePersonCount ? FALLBACK_CONCEPT_COUNT_PERSON_SQL : FALLBACK_CONCEPT_COUNT_SQL;
                warmCdmCache(cdmResultsCache, sqlPath);
            } catch (Exception ex) {
                log.error("Failed to warm cache from scratch for {}. Exception: {}", source.getSourceKey(), ex.getLocalizedMessage());
            }
        }
    }

    private void warmCdmCache(CDMResultsCache cdmResultsCache, String sqlScriptPath) {
        String resultTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        String sql_statement = ResourceHelper.GetResourceAsString(sqlScriptPath);
        String[] tables = {"resultTableQualifier", "vocabularyTableQualifier"};
        String[] tableValues = {resultTableQualifier, vocabularyTableQualifier};

        PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql_statement, tables, tableValues,
          SessionUtils.sessionId());

        BaseRecordCountMapper<?> mapper;
        mapper = this.usePersonCount ? new DescendantRecordAndPersonCountMapper() : new DescendantRecordCountMapper();

        transactionTemplate.execute(s -> {
            jdbcTemplate.query(psr.getSql(), psr.getSetter(), resultSet -> {
                DescendantRecordCount descendantRecordCount = mapper.mapRow(resultSet);
                cdmResultsCache.cacheValue(descendantRecordCount);
            });
            return null;
        });
        cdmResultsCache.warm();
    }

}
