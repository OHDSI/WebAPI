package org.ohdsi.webapi.job;

import org.springframework.batch.admin.service.SearchableJobExecutionDao;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final int MAX_SIZE = 20;
    private static final int PAGE_SIZE = MAX_SIZE * 10;
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
        final Map<String, JobExecution> result = new HashMap<>();
        for (int start = 0; result.size() < MAX_SIZE; start += PAGE_SIZE) {
            final List<JobExecution> page = jobExecutionDao.getJobExecutions(start, PAGE_SIZE);
            if(page.size() == 0) {
                break;
            }
            page.stream().filter(NotificationServiceImpl::whiteList).forEach(entity -> {
                result.merge(getFoldingKey(entity), entity, (x, y) -> {
                    final Date xStartTime = x.getStartTime();
                    final Date yStartTime = y.getStartTime();
                    return xStartTime != null ?  yStartTime != null ? xStartTime.after(yStartTime) ? x : y : x: y;
                });
            });
        }
        return result.values().stream().sorted(Comparator.comparing(JobExecution::getStartTime).reversed()).collect(Collectors.toList());
    }

    private static String getFoldingKey(JobExecution entity) {
        final Optional<String> key = entity.getJobParameters().getParameters().keySet().stream().filter(FOLDING_KEYS::contains).findAny();
        return key.isPresent() ? entity.getJobParameters().getString(key.get()) : String.valueOf(entity.getId());
    }

    private static boolean whiteList(JobExecution entity) {
        return WHITE_LIST.contains(entity.getJobInstance().getJobName());
    }
}
