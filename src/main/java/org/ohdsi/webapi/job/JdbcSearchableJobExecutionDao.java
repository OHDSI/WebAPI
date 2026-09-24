package org.ohdsi.webapi.job;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Spring Batch 5.x compatible replacement for the discontinued
 * spring-batch-admin JdbcSearchableJobExecutionDao.
 * <p>
 * Spring Batch 5 changed all Date fields on JobExecution to LocalDateTime,
 * so the old library's RowMapper calls setStartTime(java.util.Date) which
 * throws NoSuchMethodError at runtime.
 */
public class JdbcSearchableJobExecutionDao implements SearchableJobExecutionDao {

    private static final int FETCH_SIZE = 100;

    private static final String GET_RUNNING_EXECUTIONS =
            "SELECT E.JOB_EXECUTION_ID, E.START_TIME, E.END_TIME, E.STATUS, E.EXIT_CODE, E.EXIT_MESSAGE, " +
            "E.CREATE_TIME, E.LAST_UPDATED, E.VERSION, E.JOB_INSTANCE_ID " +
            "FROM %PREFIX%JOB_EXECUTION E WHERE E.END_TIME IS NULL ORDER BY E.JOB_EXECUTION_ID DESC";

    private static final String GET_COUNT =
            "SELECT COUNT(*) FROM %PREFIX%JOB_EXECUTION";

    private static final String GET_EXECUTIONS =
            "SELECT E.JOB_EXECUTION_ID, E.START_TIME, E.END_TIME, E.STATUS, E.EXIT_CODE, E.EXIT_MESSAGE, " +
            "E.CREATE_TIME, E.LAST_UPDATED, E.VERSION, E.JOB_INSTANCE_ID, I.JOB_NAME " +
            "FROM %PREFIX%JOB_EXECUTION E " +
            "JOIN %PREFIX%JOB_INSTANCE I ON E.JOB_INSTANCE_ID = I.JOB_INSTANCE_ID " +
            "ORDER BY E.JOB_EXECUTION_ID DESC";

    private static final String GET_EXECUTIONS_BY_NAME =
            "SELECT E.JOB_EXECUTION_ID, E.START_TIME, E.END_TIME, E.STATUS, E.EXIT_CODE, E.EXIT_MESSAGE, " +
            "E.CREATE_TIME, E.LAST_UPDATED, E.VERSION, E.JOB_INSTANCE_ID, I.JOB_NAME " +
            "FROM %PREFIX%JOB_EXECUTION E " +
            "JOIN %PREFIX%JOB_INSTANCE I ON E.JOB_INSTANCE_ID = I.JOB_INSTANCE_ID " +
            "WHERE I.JOB_NAME = ? " +
            "ORDER BY E.JOB_EXECUTION_ID DESC";

    private static final String GET_EXECUTIONS_WITH_PARAMS =
            "SELECT E.JOB_EXECUTION_ID, E.START_TIME, E.END_TIME, E.STATUS, E.EXIT_CODE, E.EXIT_MESSAGE, " +
            "E.CREATE_TIME, E.LAST_UPDATED, E.VERSION, E.JOB_INSTANCE_ID, I.JOB_NAME, " +
            "P.PARAMETER_NAME, P.PARAMETER_VALUE, P.PARAMETER_TYPE " +
            "FROM %PREFIX%JOB_EXECUTION E " +
            "JOIN %PREFIX%JOB_INSTANCE I ON E.JOB_INSTANCE_ID = I.JOB_INSTANCE_ID " +
            "INNER JOIN %PREFIX%JOB_EXECUTION_PARAMS P ON E.JOB_EXECUTION_ID = P.JOB_EXECUTION_ID " +
            "ORDER BY E.JOB_EXECUTION_ID DESC, P.PARAMETER_NAME";

    private JdbcTemplate jdbcTemplate;
    private String tablePrefix = "BATCH_";

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    @Override
    public List<JobExecution> getJobExecutions(int start, int count) {
        String sql = applyPrefix(GET_EXECUTIONS);
        // Use OFFSET/FETCH for pagination (standard SQL)
        sql += " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return jdbcTemplate.query(sql, new JobExecutionWithNameRowMapper(), start, count);
    }

    @Override
    public List<JobExecution> getJobExecutions(String jobName, int start, int count) {
        String sql = applyPrefix(GET_EXECUTIONS_BY_NAME);
        sql += " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return jdbcTemplate.query(sql, new JobExecutionWithNameRowMapper(), jobName, start, count);
    }

    @Override
    public int countJobExecutions() {
        String sql = applyPrefix(GET_COUNT);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public Collection<JobExecution> getRunningJobExecutions() {
        String sql = applyPrefix(GET_RUNNING_EXECUTIONS);
        return jdbcTemplate.query(sql, new RunningJobExecutionRowMapper());
    }

    @Override
    public Stream<JobExecution> getJobExecutionsWithParams() {
        try {
            String sql = applyPrefix(GET_EXECUTIONS_WITH_PARAMS);
            Connection conn = jdbcTemplate.getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setFetchSize(FETCH_SIZE);
            ResultSet rs = stmt.executeQuery();

            // Create iterator that groups rows by execution ID
            JobExecutionIterator iterator = new JobExecutionIterator(rs);

            // Wrap iterator in Spliterator and create Stream with resource cleanup
            Spliterator<JobExecution> spliterator = Spliterators.spliteratorUnknownSize(
                    iterator,
                    Spliterator.ORDERED | Spliterator.NONNULL
            );

            return StreamSupport.stream(spliterator, false)
                    .onClose(() -> {
                        try { rs.close(); } catch (SQLException ignored) {}
                        try { stmt.close(); } catch (SQLException ignored) {}
                        try { conn.close(); } catch (SQLException ignored) {}
                    });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Streams JobExecution objects grouped by execution ID, accumulating parameters
     * from multiple result set rows into complete JobExecution objects.
     * 
     * Uses the lookahead pattern: when execution ID changes, we've already read the
     * first row of the next group and buffer it (via prefetched flag) to avoid
     * duplicate rs.next() calls.
     */
    private class JobExecutionIterator implements Iterator<JobExecution> {
        private final ResultSet rs;
        private boolean hasMore;
        private boolean prefetched;

        JobExecutionIterator(ResultSet rs) throws SQLException {
            this.rs = rs;
            this.hasMore = rs.next();
            this.prefetched = true;  // We've already advanced to first row
        }

        @Override
        public boolean hasNext() {
            return hasMore;
        }

        @Override
        public JobExecution next() {
            try {
                // If we have NOT already advanced, move cursor forward.
                // prefetched=true means cursor is already on first row of current group.
                if (!prefetched) {
                    hasMore = rs.next();
                }
                prefetched = false;

                if (!hasMore) {
                    throw new NoSuchElementException();
                }

                // Capture execution ID to detect group boundaries
                Long currentExecId = rs.getLong("JOB_EXECUTION_ID");

                // Accumulate parameters for this execution group
                Map<String, JobParameter<?>> currentParams = new HashMap<>();

                do {
                    // Add parameter from current row
                    currentParams.put(rs.getString("PARAMETER_NAME"), 
                        createJobParameter(rs.getString("PARAMETER_NAME"), 
                                          rs.getString("PARAMETER_VALUE"), 
                                          rs.getString("PARAMETER_TYPE")));

                    // Try to read next row
                    hasMore = rs.next();

                    if (!hasMore) {
                        // End of result set
                        break;
                    }

                } while (Objects.equals(currentExecId, rs.getLong("JOB_EXECUTION_ID")));

                // We have read ONE ROW AHEAD (first row of next execution).
                // Set prefetched=true so next call to next() doesn't call rs.next() again.
                prefetched = true;

                // Build and return JobExecution with all accumulated parameters
                // Read execution metadata directly from the buffer ResultSet row
                JobInstance jobInstance = new JobInstance(rs.getLong("JOB_INSTANCE_ID"), rs.getString("JOB_NAME"));
                JobParameters jobParameters = new JobParameters(currentParams);
                JobExecution execution = new JobExecution(jobInstance, jobParameters);
                execution.setId(currentExecId);
                execution.setStartTime(toLocalDateTime(rs.getTimestamp("START_TIME")));
                execution.setEndTime(toLocalDateTime(rs.getTimestamp("END_TIME")));
                execution.setCreateTime(toLocalDateTime(rs.getTimestamp("CREATE_TIME")));
                execution.setLastUpdated(toLocalDateTime(rs.getTimestamp("LAST_UPDATED")));
                execution.setStatus(BatchStatus.valueOf(rs.getString("STATUS")));
                execution.setExitStatus(new ExitStatus(rs.getString("EXIT_CODE"), rs.getString("EXIT_MESSAGE")));
                execution.setVersion(rs.getInt("VERSION"));

                return execution;

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String applyPrefix(String sql) {
        return sql.replace("%PREFIX%", tablePrefix);
    }

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    /**
     * Creates a typed JobParameter from database values.
     * Parses the PARAMETER_TYPE string to determine the Java type.
     * 
     * @param name Parameter name
     * @param value Parameter value as string
     * @param type Java class type name (e.g., "java.lang.String", "java.lang.Long")
     * @return JobParameter with appropriate type
     */
    private static JobParameter<?> createJobParameter(String name, String value, String type) {
        if (value == null) {
            return new JobParameter<>(null, String.class, false);
        }

        try {
            if ("java.lang.String".equals(type)) {
                return new JobParameter<>(value, String.class, false);
            } else if ("java.lang.Long".equals(type)) {
                return new JobParameter<>(Long.parseLong(value), Long.class, false);
            } else if ("java.lang.Double".equals(type)) {
                return new JobParameter<>(Double.parseDouble(value), Double.class, false);
            } else if ("java.time.LocalDateTime".equals(type)) {
                // Parse ISO format: YYYY-MM-DD"T"HH24:MI:SS
                LocalDateTime dt = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return new JobParameter<>(dt, LocalDateTime.class, false);
            }
        } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
            // Fallback to String if parsing fails
            return new JobParameter<>(value, String.class, false);
        }

        // Default: treat as String
        return new JobParameter<>(value, String.class, false);
    }



    /**
     * RowMapper for running executions (no JOB_NAME join).
     */
    private static class RunningJobExecutionRowMapper implements RowMapper<JobExecution> {
        @Override
        public JobExecution mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long jobExecutionId = rs.getLong("JOB_EXECUTION_ID");
            Long jobInstanceId = rs.getLong("JOB_INSTANCE_ID");

            // Minimal JobInstance - name will be populated by callers if needed
            JobInstance jobInstance = new JobInstance(jobInstanceId, "UNKNOWN");
            JobExecution execution = new JobExecution(jobExecutionId, new JobParameters());
            execution.setJobInstance(jobInstance);

            execution.setStartTime(toLocalDateTime(rs.getTimestamp("START_TIME")));
            execution.setEndTime(toLocalDateTime(rs.getTimestamp("END_TIME")));
            execution.setCreateTime(toLocalDateTime(rs.getTimestamp("CREATE_TIME")));
            execution.setLastUpdated(toLocalDateTime(rs.getTimestamp("LAST_UPDATED")));

            execution.setStatus(BatchStatus.valueOf(rs.getString("STATUS")));
            execution.setExitStatus(new ExitStatus(rs.getString("EXIT_CODE"), rs.getString("EXIT_MESSAGE")));
            execution.setVersion(rs.getInt("VERSION"));

            return execution;
        }
    }

    /**
     * RowMapper including JOB_NAME from joined JOB_INSTANCE table.
     */
    private static class JobExecutionWithNameRowMapper implements RowMapper<JobExecution> {
        @Override
        public JobExecution mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long jobExecutionId = rs.getLong("JOB_EXECUTION_ID");
            Long jobInstanceId = rs.getLong("JOB_INSTANCE_ID");
            String jobName = rs.getString("JOB_NAME");

            JobInstance jobInstance = new JobInstance(jobInstanceId, jobName);
            JobExecution execution = new JobExecution(jobExecutionId, new JobParameters());
            execution.setJobInstance(jobInstance);

            execution.setStartTime(toLocalDateTime(rs.getTimestamp("START_TIME")));
            execution.setEndTime(toLocalDateTime(rs.getTimestamp("END_TIME")));
            execution.setCreateTime(toLocalDateTime(rs.getTimestamp("CREATE_TIME")));
            execution.setLastUpdated(toLocalDateTime(rs.getTimestamp("LAST_UPDATED")));

            execution.setStatus(BatchStatus.valueOf(rs.getString("STATUS")));
            execution.setExitStatus(new ExitStatus(rs.getString("EXIT_CODE"), rs.getString("EXIT_MESSAGE")));
            execution.setVersion(rs.getInt("VERSION"));

            return execution;
        }
    }
}
