package org.ohdsi.webapi.job;

import org.springframework.batch.core.JobExecution;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Custom interface replacing the discontinued spring-batch-admin
 * SearchableJobExecutionDao, compatible with Spring Batch 5.x.
 */
public interface SearchableJobExecutionDao {

    List<JobExecution> getJobExecutions(int start, int count);

    List<JobExecution> getJobExecutions(String jobName, int start, int count);

    int countJobExecutions();

    Collection<JobExecution> getRunningJobExecutions();

    /**
     * Returns a Stream of JobExecution objects with all parameters populated, lazily-evaluated
     * from a single database query grouped by execution ID.
     * 
     * <p>The stream uses a lookahead iterator pattern: rows are fetched from the database
     * on-demand as the caller consumes the stream. Rows are grouped by JOB_EXECUTION_ID,
     * and parameters are accumulated across multiple rows into single JobExecution objects.
     * No row data is copied; the iterator reads directly from the ResultSet cursor.
     * 
     * <p><b>Resource Management:</b> The stream automatically manages database resources
     * (ResultSet, Statement, Connection) via the onClose() hook. Resources are closed when:
     * <ul>
     *   <li>A terminal operation completes (e.g., forEach, collect, limit)</li>
     *   <li>Caller explicitly calls close() on the stream</li>
     *   <li>Try-with-resources block exits</li>
     * </ul>
     * 
     * <p><b>Usage Pattern:</b>
     * <pre>
     * try (Stream&lt;JobExecution&gt; stream = dao.getJobExecutionsWithParams()) {
     *     stream.filter(...).limit(maxSize).forEach(...);\n     * }
     * </pre>
     * 
     * <p><b>Single-Use Stream:</b> Like all streams backed by database resources, this stream
     * is single-use and forward-only. Attempting to reuse or parallelize will cause errors.
     * 
     * @return Stream of JobExecution objects with parameters populated. Must be used in
     *         try-with-resources or with explicit terminal operation for proper resource cleanup.
     * @throws SQLException if database access fails
     */
    Stream<JobExecution> getJobExecutionsWithParams();
}
