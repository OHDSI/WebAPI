package org.ohdsi.webapi.job;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.ohdsi.webapi.Constants.SYSTEM_USER;
import static org.ohdsi.webapi.Constants.WARM_CACHE_BY_USER;

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

        // Custom job is not associated with the entity
        WHITE_LIST.add(WARM_CACHE_BY_USER);
    }

    @Override
    public List<JobExecutionInfo> findLastJobs(List<BatchStatus> hideStatuses) {
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
        final Map<String, JobExecutionInfo> systemJobMap = new HashMap<>();
        final Map<String, JobExecutionInfo> userJobMap = new HashMap<>();
        for (int start = 0; userJobMap.size() < MAX_SIZE || systemJobMap.size() < MAX_SIZE; start += PAGE_SIZE) {
            final List<JobExecution> page = jobExecutionDao.getJobExecutions(start, PAGE_SIZE);
            if(page.size() == 0) {
                break;
            }
            for (JobExecution jobExec: page) {
                // ignore completed jobs when user does not want to see them
                if (hideStatuses.contains(jobExec.getStatus())) {
                    continue;
                }
                if (isInWhiteList(jobExec)) {
                    boolean isMine = isMine(jobExec);
                    if (userJobMap.size() < MAX_SIZE && isMine) {
                        JobExecutionInfo executionInfo = new JobExecutionInfo(jobExec, JobOwnerType.USER_JOB);
                        userJobMap.merge(getFoldingKey(jobExec), executionInfo, mergeFunction);
                    }
                    if (systemJobMap.size() < MAX_SIZE && !isMine) {
                        JobExecutionInfo executionInfo = new JobExecutionInfo(jobExec, JobOwnerType.SYSTEM_JOB);
                        systemJobMap.merge(getFoldingKey(jobExec), executionInfo, mergeFunction);
                    }

                    if (userJobMap.size() >= MAX_SIZE && systemJobMap.size() >= MAX_SIZE) {
                        break;
                    }
                }
            }
        }

        final List<JobExecutionInfo> jobs = new ArrayList<>(systemJobMap.values());
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
