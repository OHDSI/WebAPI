package org.ohdsi.webapi.job;

import org.springframework.batch.admin.service.SearchableJobExecutionDao;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final int MAX_SIZE = 20;
    private static final List<String> WHITE_LIST = new ArrayList<>();
    private static final List<String> FOLDING_KEYS = new ArrayList<>();

    private final SearchableJobExecutionDao jobExecutionDao;

    public NotificationServiceImpl(SearchableJobExecutionDao jobExecutionDao, List<GeneratesNotification> whiteList) {
        this.jobExecutionDao = jobExecutionDao;
        whiteList.forEach(g -> {
            WHITE_LIST.add(g.getJobName());
            FOLDING_KEYS.add(g.getExecutinFoldingKey());
        });
    }

    @Override
    public List<JobExecutionResource> findAll() {
        return jobExecutionDao.getJobExecutions(0, Integer.MAX_VALUE).stream()
                .filter(NotificationServiceImpl::whiteList)
                .collect(Collectors.toMap(NotificationServiceImpl::getFoldingKey, t -> t, getLatest()))
                .values().stream()
                .limit(MAX_SIZE)
                .map(NotificationServiceImpl::toDTO)
                .collect(Collectors.toList());
    }

    private static BinaryOperator<JobExecution> getLatest() {
        return (x, y) -> x.getStartTime().after(y.getStartTime()) ? x : y;
    }

    private static String getFoldingKey(JobExecution entity) {
        final Optional<String> key = entity.getJobParameters().getParameters().keySet().stream().filter(FOLDING_KEYS::contains).findAny();
        return key.isPresent() ? entity.getJobParameters().getString(key.get()) : String.valueOf(entity.getId());
    }

    private static boolean whiteList(JobExecution entity) {
        return WHITE_LIST.contains(entity.getJobInstance().getJobName());
    }

    private static JobExecutionResource toDTO(JobExecution entity) {
        final JobInstance instance = entity.getJobInstance();
        final JobInstanceResource instanceResource = new JobInstanceResource(instance.getInstanceId(), instance.getJobName());
        final JobExecutionResource result = new JobExecutionResource(instanceResource, entity.getJobId());
        result.setStatus(entity.getStatus().name());
        result.setExitStatus(entity.getExitStatus().getExitCode());
        result.setStartDate(entity.getStartTime());
        result.setEndDate(entity.getEndTime());
        result.setJobParametersResource(entity.getJobParameters().getParameters().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue())));
        return result;
    }

}
