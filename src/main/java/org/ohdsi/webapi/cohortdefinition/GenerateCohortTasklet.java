/*
 * Copyright 2015 Observational Health Data Sciences and Informatics <OHDSI.org>.
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
package org.ohdsi.webapi.cohortdefinition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysisTasklet;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class GenerateCohortTasklet implements Tasklet {

  private static final Log log = LogFactory.getLog(CohortAnalysisTasklet.class);
    
    private final String[] sql;
    
    private final JdbcTemplate jdbcTemplate;
    
    private final TransactionTemplate transactionTemplate;
    
    public GenerateCohortTasklet(final String[] taskSql, final JdbcTemplate jdbcTemplate,
        final TransactionTemplate transactionTemplate) {
        this.sql = taskSql;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }
    
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        
        try {
            final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {
                
                @Override
                public int[] doInTransaction(final TransactionStatus status) {
                    return GenerateCohortTasklet.this.jdbcTemplate.batchUpdate(GenerateCohortTasklet.this.sql);
                }
            });
            log.debug("Update count: " + ret.length);
        } catch (final TransactionException e) {
            log.error(e.getMessage(), e);
            throw e;//FAIL job status
        }
        return RepeatStatus.FINISHED;
    }
      
}
