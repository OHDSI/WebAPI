package org.ohdsi.webapi.batch;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Spring Batch 5.x compatible replacement for the discontinued
 * spring-batch-admin JdbcSearchableJobExecutionDao.
 * <p>
 * Spring Batch 5 changed all Date fields on JobExecution to LocalDateTime,
 * so the old library's RowMapper calls setStartTime(java.util.Date) which
 * throws NoSuchMethodError at runtime.
 */
public class JdbcSearchableJobExecutionDao implements SearchableJobExecutionDao {

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
    public List<JobExecution> getJobExecutionsWithParams() {
        String sql = applyPrefix(GET_EXECUTIONS_WITH_PARAMS);
        return jdbcTemplate.query(sql, new JobExecutionWithParamsResultSetExtractor());
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
     * ResultSetExtractor that collapses multiple parameter rows into single JobExecution objects.
     * Reads result set containing job execution data with multiple rows per execution (one per parameter),
     * groups by execution ID, and builds a single JobExecution with all parameters populated.
     * No reflection needed - parameters are passed to JobExecution constructor.
     */
    private static class JobExecutionWithParamsResultSetExtractor implements ResultSetExtractor<List<JobExecution>> {
        @Override
        public List<JobExecution> extractData(ResultSet rs) throws SQLException {
            List<JobExecution> results = new java.util.ArrayList<>();
            
            // Track state while iterating through rows
            Long currentExecId = null;
            Long currentExecInstanceId = null;
            String currentJobName = null;
            Timestamp currentStartTime = null;
            Timestamp currentEndTime = null;
            Timestamp currentCreateTime = null;
            Timestamp currentLastUpdated = null;
            String currentStatus = null;
            String currentExitCode = null;
            String currentExitMessage = null;
            Integer currentVersion = null;
            Map<String, JobParameter<?>> currentParams = new HashMap<>();

            while (rs.next()) {
                Long execId = rs.getLong("JOB_EXECUTION_ID");

                // New execution ID detected - materialize previous execution with its parameters
                if (currentExecId != null && !Objects.equals(currentExecId, execId)) {
                    // Build JobExecution with parameters already included
                    JobExecution execution = buildJobExecutionWithParams(
                            currentExecId, currentExecInstanceId, currentJobName,
                            currentStartTime, currentEndTime, currentCreateTime, currentLastUpdated,
                            currentStatus, currentExitCode, currentExitMessage, currentVersion,
                            currentParams);
                    results.add(execution);
                    currentParams.clear();
                }

                // Capture execution data (only store once per unique execution ID)
                if (currentExecId == null || !Objects.equals(currentExecId, execId)) {
                    currentExecId = execId;
                    currentExecInstanceId = rs.getLong("JOB_INSTANCE_ID");
                    currentJobName = rs.getString("JOB_NAME");
                    currentStartTime = rs.getTimestamp("START_TIME");
                    currentEndTime = rs.getTimestamp("END_TIME");
                    currentCreateTime = rs.getTimestamp("CREATE_TIME");
                    currentLastUpdated = rs.getTimestamp("LAST_UPDATED");
                    currentStatus = rs.getString("STATUS");
                    currentExitCode = rs.getString("EXIT_CODE");
                    currentExitMessage = rs.getString("EXIT_MESSAGE");
                    currentVersion = rs.getInt("VERSION");
                }

                // Accumulate parameters for current execution
                String paramName = rs.getString("PARAMETER_NAME");
                String paramValue = rs.getString("PARAMETER_VALUE");
                String paramType = rs.getString("PARAMETER_TYPE");
                currentParams.put(paramName, createJobParameter(paramName, paramValue, paramType));
            }

            // Handle the last execution
            if (currentExecId != null) {
                JobExecution execution = buildJobExecutionWithParams(
                        currentExecId, currentExecInstanceId, currentJobName,
                        currentStartTime, currentEndTime, currentCreateTime, currentLastUpdated,
                        currentStatus, currentExitCode, currentExitMessage, currentVersion,
                        currentParams);
                results.add(execution);
            }

            return results;
        }

        /**
         * Builds a JobExecution with parameters passed directly to the constructor.
         * This avoids the need for reflection - parameters are properly initialized.
         */
        private static JobExecution buildJobExecutionWithParams(
                Long executionId, Long jobInstanceId, String jobName,
                Timestamp startTime, Timestamp endTime, Timestamp createTime, Timestamp lastUpdated,
                String status, String exitCode, String exitMessage, Integer version,
                Map<String, JobParameter<?>> params) throws SQLException {
            
            // Create JobInstance first
            JobInstance jobInstance = new JobInstance(jobInstanceId, jobName);
            
            // Create JobParameters with all accumulated parameters
            JobParameters jobParameters = new JobParameters(params);
            
            // Create JobExecution with both JobInstance and JobParameters
            JobExecution execution = new JobExecution(jobInstance, jobParameters);
            execution.setId(executionId);

            // Set remaining fields
            execution.setStartTime(toLocalDateTime(startTime));
            execution.setEndTime(toLocalDateTime(endTime));
            execution.setCreateTime(toLocalDateTime(createTime));
            execution.setLastUpdated(toLocalDateTime(lastUpdated));
            execution.setStatus(BatchStatus.valueOf(status));
            execution.setExitStatus(new ExitStatus(exitCode, exitMessage));
            execution.setVersion(version);

            return execution;
        }
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
