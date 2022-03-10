package org.ohdsi.webapi.job;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.batch.admin.service.SearchableJobExecutionDao;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.ohdsi.webapi.Constants.Params.SOURCE_KEY;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final int MAX_SIZE = 10;
    private static final int PAGE_SIZE = MAX_SIZE * 10;
    private static final List<String> WHITE_LIST = new ArrayList<>();
    private static final List<String> FOLDING_KEYS = new ArrayList<>();

    private final SearchableJobExecutionDao jobExecutionDao;
    private final PermissionManager permissionManager;
    private final UserRepository userRepository;

    @Value("#{!'${security.provider}'.equals('DisabledSecurity')}")
    private boolean securityEnabled;

    public NotificationServiceImpl(SearchableJobExecutionDao jobExecutionDao, List<GeneratesNotification> whiteList, PermissionManager permissionManager, UserRepository userRepository) {
        this.jobExecutionDao = jobExecutionDao;
        this.permissionManager = permissionManager;
        this.userRepository = userRepository;
        whiteList.forEach(g -> {
            WHITE_LIST.add(g.getJobName());
            FOLDING_KEYS.add(g.getExecutionFoldingKey());
        });
        // Folding key for warming source key job
        FOLDING_KEYS.add(SOURCE_KEY);
    }

    @Override
    public List<JobExecutionInfo> findLastJobs(List<BatchStatus> hideStatuses) {
        return findJobs(hideStatuses, MAX_SIZE, false);
    }

    @Override
    public List<JobExecutionInfo> findRefreshCacheLastJobs() {
        return findJobs(Collections.emptyList(), MAX_SIZE, true);
    }

    public List<JobExecutionInfo> findJobs(List<BatchStatus> hideStatuses, int maxSize, boolean refreshJobsOnly) {
        BiFunction<JobExecutionInfo, JobExecutionInfo, JobExecutionInfo> mergeFunction = (x, y) -> {
            final Date xStartTime = x != null ? x.getJobExecution().getStartTime() : null;
            final Date yStartTime = y != null ? y.getJobExecution().getStartTime() : null;
            return xStartTime != null ?
                    yStartTime != null ?
                            xStartTime.after(yStartTime) ? x
                                    : y
                            : x
                    : y;
        };
        final Map<String, JobExecutionInfo> allJobMap = new HashMap<>();
        final Map<String, JobExecutionInfo> userJobMap = new HashMap<>();
        for (int start = 0; (!refreshJobsOnly && userJobMap.size() < MAX_SIZE) || allJobMap.size() < MAX_SIZE; start += PAGE_SIZE) {
            final List<JobExecution> page = jobExecutionDao.getJobExecutions(start, PAGE_SIZE);
            if(page.size() == 0) {
                break;
            }
            for (JobExecution jobExec: page) {
                // ignore completed jobs when user does not want to see them
                if (hideStatuses.contains(jobExec.getStatus())) {
                    continue;
                }
                if (!refreshJobsOnly && isInWhiteList(jobExec)) {
                    boolean isMine = isMine(jobExec);
                    if (userJobMap.size() < MAX_SIZE && isMine) {
                        JobExecutionInfo executionInfo = new JobExecutionInfo(jobExec, JobOwnerType.USER_JOB);
                        userJobMap.merge(getFoldingKey(jobExec), executionInfo, mergeFunction);
                    }
                    if (allJobMap.size() < MAX_SIZE) {
                        JobExecutionInfo executionInfo = new JobExecutionInfo(jobExec, JobOwnerType.ALL_JOB);
                        allJobMap.merge(getFoldingKey(jobExec), executionInfo, mergeFunction);
                    }
                } else if (refreshJobsOnly) {
                    if (allJobMap.size() < MAX_SIZE && jobExec.getJobInstance().getJobName().startsWith("warming ")) {
                        JobExecutionInfo executionInfo = new JobExecutionInfo(jobExec, JobOwnerType.ALL_JOB);
                        allJobMap.merge(getFoldingKey(jobExec), executionInfo, mergeFunction);
                    }
                }

                if ((refreshJobsOnly || userJobMap.size() >= maxSize) && allJobMap.size() >= maxSize) {
                    break;
                }
            }
        }

        final List<JobExecutionInfo> jobs = new ArrayList<>(allJobMap.values());
        jobs.addAll(userJobMap.values());
        return jobs;
    }

    @Override
    public Date getLastViewedTime() throws Exception {
        final UserEntity user = securityEnabled ? permissionManager.getCurrentUser() : null;
        return user != null ? user.getLastViewedNotificationsTime() : null;
    }

    @Override
    public void setLastViewedTime(Date stamp) throws Exception {
        final UserEntity user = securityEnabled ? permissionManager.getCurrentUser() : null;
        if(user != null) {
            user.setLastViewedNotificationsTime(stamp);
            userRepository.save(user);
        }
    }

    private static String getFoldingKey(JobExecution entity) {
        final Optional<String> key = entity.getJobParameters().getParameters().keySet().stream().filter(FOLDING_KEYS::contains).findAny();
        return key.map(s -> s + "_" + entity.getJobParameters().getString(s) + "_" + entity.getJobParameters().getString("source_id"))
                .orElseGet(() -> String.valueOf(entity.getId()));
    }

    private static boolean isInWhiteList(JobExecution entity) {
        return WHITE_LIST.contains(entity.getJobInstance().getJobName());
    }
    
    private boolean isMine(JobExecution jobExec) {
        final String login = securityEnabled ? permissionManager.getSubjectName() : null;
        final String jobAuthor = jobExec.getJobParameters().getString(Constants.Params.JOB_AUTHOR);
        return Objects.equals(login, jobAuthor);
    }
}
