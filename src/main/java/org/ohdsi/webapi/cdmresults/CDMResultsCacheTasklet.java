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

/**
 * @author fdefalco
 */
public class CDMResultsCacheTasklet implements Tasklet {
    private static final Logger log = LoggerFactory.getLogger(CDMResultsCacheTasklet.class);

    private final JdbcTemplate jdbcTemplate;
    private final Source source;
    private final DescendantRecordCountMapper descendantRecordCountMapper;

    public CDMResultsCacheTasklet(final JdbcTemplate t, final Source s) {
        jdbcTemplate = t;
        source = s;
        descendantRecordCountMapper = new DescendantRecordCountMapper();
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) {
        CDMResultsCache cdmResultsCache = new CDMResultsCache();
        warm(cdmResultsCache);

        ResultsCache.getAll().put(source.getSourceKey(), cdmResultsCache);
        return RepeatStatus.FINISHED;
    }
    
    private void warm(CDMResultsCache cdmResultsCache) {

        String resultTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        String sql_statement = ResourceHelper.GetResourceAsString("/resources/cdmresults/sql/loadConceptRecordCountCache.sql");
        String[] tables = {"resultTableQualifier", "vocabularyTableQualifier"};
        String[] tableValues = {resultTableQualifier, vocabularyTableQualifier};

        PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql_statement, tables, tableValues, SessionUtils.sessionId());

        try {
            jdbcTemplate.query(psr.getSql(), psr.getSetter(), resultSet -> {
                DescendantRecordCount descendantRecordCount = descendantRecordCountMapper.mapRow(resultSet);
                cdmResultsCache.cacheValue(descendantRecordCount);
            });
            cdmResultsCache.warm();
        } catch (Exception e) {
            log.error("Failed to warm cache for {}. Exception: {}", source.getSourceKey(), e.getLocalizedMessage());
        }
    }

}
