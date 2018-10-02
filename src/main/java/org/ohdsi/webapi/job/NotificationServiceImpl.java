package org.ohdsi.webapi.job;

import org.springframework.batch.admin.service.SearchableJobExecutionDao;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final int MAX_SIZE = 20;
    private static final List<Object> WHITE_LIST = Arrays.asList("generateCohort");

    private final SearchableJobExecutionDao jobExecutionDao;

    public NotificationServiceImpl(SearchableJobExecutionDao jobExecutionDao) {
        this.jobExecutionDao = jobExecutionDao;
    }

    @Override
    public List<JobExecutionResource> findAll() {
        return jobExecutionDao.getJobExecutions(0, Integer.MAX_VALUE).stream()
                .filter(this::whiteList)
                .limit(MAX_SIZE)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private boolean whiteList(JobExecution entity) {
        return WHITE_LIST.contains(entity.getJobInstance().getJobName());
    }

    private JobExecutionResource toDTO(JobExecution entity) {
        final JobInstance instance = entity.getJobInstance();
        final JobInstanceResource instanceResource = new JobInstanceResource(instance.getInstanceId(), instance.getJobName());
        final JobExecutionResource result = new JobExecutionResource(instanceResource, entity.getJobId());
        result.setStatus(entity.getStatus().name());
        result.setExitStatus(entity.getExitStatus().getExitCode());
        result.setStartDate(entity.getStartTime());
        result.setEndDate(entity.getEndTime());
        return result;
    }
}
