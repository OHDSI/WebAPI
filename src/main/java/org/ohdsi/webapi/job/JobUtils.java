package org.ohdsi.webapi.job;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import org.ohdsi.webapi.Constants;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameter.ParameterType;
import org.springframework.batch.core.JobParameters;

/**
 *
 */
public final class JobUtils {

    // Arrays.asList was used to provide easy extending ability in future
    private static List<String> PROTECTED_PARAMS = Arrays.asList(Constants.Params.UPDATE_PASSWORD);

    public static JobInstanceResource toJobInstanceResource(final JobInstance jobInstance) {
        final JobInstanceResource job = new JobInstanceResource(jobInstance.getId());
        job.setName(jobInstance.getJobName());
        return job;
    }
    
    public static JobExecutionResource toJobExecutionResource(final JobExecution jobExecution) {
        final JobExecutionResource execution = new JobExecutionResource(
                toJobInstanceResource(jobExecution.getJobInstance()), jobExecution.getId());
        execution.setStatus(jobExecution.getStatus().name());
        execution.setStartDate(jobExecution.getStartTime());
        execution.setEndDate(jobExecution.getEndTime());
        execution.setExitStatus(jobExecution.getExitStatus().getExitCode());
        JobParameters jobParams = jobExecution.getJobParameters();
        if (jobParams != null) {
            Map<String, JobParameter> params = jobParams.getParameters();
            if (params != null && !params.isEmpty()) {
                Map<String, Object> jobParametersResource = new HashMap<String, Object>();
                Set<String> keys = params.keySet().stream()
                        .filter(k -> !PROTECTED_PARAMS.contains(k))
                        .collect(Collectors.toSet());
                for (String key : keys) {
                    jobParametersResource.put(key, params.get(key).getValue());
                }
                execution.setJobParametersResource(jobParametersResource);
            }
        }
        return execution;
    }
    
    /**
     * Create List of JobExecutionResource objects containing job parameters.
     * <p>
     * See /resources/job/sql/jobExecutions.sql for ResultSet expectations.
     * 
     * @param rs
     * @return
     * @throws SQLException
     */
    public static List<JobExecutionResource> toJobExecutionResource(final ResultSet rs) throws SQLException {
        //TODO order by executionId
        List<JobExecutionResource> jobs = new ArrayList<>();
        JobExecutionResource jobexec = null;
        Map<String, Object> map = new HashMap<>();
        while (rs.next()) {
            Long id = rs.getLong(1);
            if (jobexec != null) {//possible continuation
                if (!jobexec.getExecutionId().equals(id)) {
                    //no continuation
                    jobexec.setJobParametersResource(map);
                    jobs.add(jobexec);
                    jobexec = null;
                    map = null;
                }
            }
            if (jobexec == null) {
                map = new HashMap<String, Object>();
                //JobParameters jobParameters = getJobParameters(id);
                JobInstance jobInstance = new JobInstance(rs.getLong(10), rs.getString(11));
                JobExecution jobExecution = new JobExecution(jobInstance, null);//jobParameters);
                jobExecution.setId(id);
                
                jobExecution.setStartTime(rs.getTimestamp(2));
                jobExecution.setEndTime(rs.getTimestamp(3));
                jobExecution.setStatus(BatchStatus.valueOf(rs.getString(4)));
                jobExecution.setExitStatus(new ExitStatus(rs.getString(5), rs.getString(6)));
                jobExecution.setCreateTime(rs.getTimestamp(7));
                jobExecution.setLastUpdated(rs.getTimestamp(8));
                jobExecution.setVersion(rs.getInt(9));
                jobexec = toJobExecutionResource(jobExecution);
            }
            
            //parameters starts at 12
            String key = rs.getString(12);

            if (!PROTECTED_PARAMS.contains(key)) {
                ParameterType type = ParameterType.valueOf(rs.getString(13));
                JobParameter value = null;
                switch (type) {
                    case STRING: {
                        value = new JobParameter(rs.getString(14), rs.getString(18).equalsIgnoreCase("Y"));
                        break;
                    }
                    case LONG: {
                        value = new JobParameter(rs.getLong(16), rs.getString(18).equalsIgnoreCase("Y"));
                        break;
                    }
                    case DOUBLE: {
                        value = new JobParameter(rs.getDouble(17), rs.getString(18).equalsIgnoreCase("Y"));
                        break;
                    }
                    case DATE: {
                        value = new JobParameter(rs.getTimestamp(15), rs.getString(18).equalsIgnoreCase("Y"));
                        break;
                    }
                }

                // No need to assert that value is not null because it's an enum
                map.put(key, value.getValue());//value);
            }
            
        }
        if (jobexec != null && jobexec.getExecutionId() != null) {
            jobexec.setJobParametersResource(map);
            jobs.add(jobexec);
        }
        return jobs;
    }
    
}
