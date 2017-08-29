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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cache.ResultsCache;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * @author fdefalco
 */
public class CDMResultsCacheTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;
    private final Source source;
    private final CDMResultsCache cdmResultsCache;

    public CDMResultsCacheTasklet(final JdbcTemplate t, final Source s) {
        jdbcTemplate = t;
        source = s;
        cdmResultsCache = new CDMResultsCache();
    }
    
    private HashMap<Long,Long[]> warmCache() {
        String resultTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        String sql_statement = ResourceHelper.GetResourceAsString("/resources/cdmresults/sql/loadConceptRecordCountCache.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[]{"resultTableQualifier", "vocabularyTableQualifier"}, new String[]{resultTableQualifier, vocabularyTableQualifier});
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

        return jdbcTemplate.query(sql_statement, new ResultSetExtractor<HashMap<Long,Long[]>>() {
            @Override
            public HashMap<Long,Long[]> extractData(ResultSet rs) throws SQLException, DataAccessException {
                HashMap<Long, Long[]> newCache = new HashMap<>();
                while (rs.next()) {
                    long id = rs.getLong("concept_id");
                    long record_count = rs.getLong("record_count");
                    long descendant_record_count = rs.getLong("descendant_record_count");

                    newCache.put(id, new Long[] { record_count, descendant_record_count });
                }
                return newCache;
            }
        });        
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        cdmResultsCache.cache = warmCache();
        cdmResultsCache.warm = true;
        ResultsCache resultsCache = new ResultsCache();
        resultsCache.getCaches().put(source.getSourceKey(), cdmResultsCache);
        return RepeatStatus.FINISHED;
    }

}
