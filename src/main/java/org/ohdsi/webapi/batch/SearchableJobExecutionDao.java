package org.ohdsi.webapi.batch;

import org.springframework.batch.core.JobExecution;

import java.util.Collection;
import java.util.List;

/**
 * Custom interface replacing the discontinued spring-batch-admin
 * SearchableJobExecutionDao, compatible with Spring Batch 5.x.
 */
public interface SearchableJobExecutionDao {

    List<JobExecution> getJobExecutions(int start, int count);

    List<JobExecution> getJobExecutions(String jobName, int start, int count);

    int countJobExecutions();

    Collection<JobExecution> getRunningJobExecutions();
}
