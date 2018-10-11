package org.ohdsi.webapi.job;

import org.springframework.batch.admin.service.SearchableJobExecutionDao;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
            FOLDING_KEYS.add(g.getExecutionFoldingKey());
        });
    }

    @Override
    public List<JobExecution> findAll() {
        return jobExecutionDao.getJobExecutions(0, Integer.MAX_VALUE).stream()
                .filter(NotificationServiceImpl::whiteList)
                .collect(Collectors.toMap(NotificationServiceImpl::getFoldingKey, t -> t, getLatest()))
                .values().stream()
                .limit(MAX_SIZE)
                .collect(Collectors.toList());
    }

    private static BinaryOperator<JobExecution> getLatest() {
        return (x, y) -> {
            final Date xStartTime = x.getStartTime();
            final Date yStartTime = y.getStartTime();
            return xStartTime != null ?  yStartTime != null ? xStartTime.compareTo(yStartTime) > 0 ? x : y : x: y;
        };
    }

    private static String getFoldingKey(JobExecution entity) {
        final Optional<String> key = entity.getJobParameters().getParameters().keySet().stream().filter(FOLDING_KEYS::contains).findAny();
        return key.isPresent() ? entity.getJobParameters().getString(key.get()) : String.valueOf(entity.getId());
    }

    private static boolean whiteList(JobExecution entity) {
        return WHITE_LIST.contains(entity.getJobInstance().getJobName());
    }
}
