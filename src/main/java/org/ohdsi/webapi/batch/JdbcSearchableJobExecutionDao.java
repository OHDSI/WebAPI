package org.ohdsi.webapi.batch;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

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

    private String applyPrefix(String sql) {
        return sql.replace("%PREFIX%", tablePrefix);
    }

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
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
