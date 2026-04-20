package org.ohdsi.webapi.job;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.security.authz.UserEntity;
import org.ohdsi.webapi.security.authz.UserRepository;
import org.ohdsi.webapi.security.identity.WebApiPrincipal;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ohdsi.webapi.batch.SearchableJobExecutionDao;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.SOURCE_KEY;

/**
 * REST Services related to working with the system notifications
 *
 * @summary Notifications
 */
@RestController
@RequestMapping("/notifications")
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final int MAX_SIZE = 10;
    private static final int PAGE_SIZE = MAX_SIZE * 10;
    private static final List<String> WHITE_LIST = new ArrayList<>();
    private static final List<String> FOLDING_KEYS = new ArrayList<>();

    private final SearchableJobExecutionDao jobExecutionDao;
    private final AuthorizationService permissionManager;
    private final UserRepository userRepository;
    private final GenericConversionService conversionService;

    public NotificationServiceImpl(SearchableJobExecutionDao jobExecutionDao, List<GeneratesNotification> whiteList,
                                   AuthorizationService permissionManager, UserRepository userRepository,
                                   @Qualifier("conversionService") GenericConversionService conversionService) {
        this.jobExecutionDao = jobExecutionDao;
        this.permissionManager = permissionManager;
        this.userRepository = userRepository;
        this.conversionService = conversionService;
        whiteList.forEach(g -> {
            WHITE_LIST.add(g.getJobName());
            FOLDING_KEYS.add(g.getExecutionFoldingKey());
        });
        // Folding key for warming source key job
        FOLDING_KEYS.add(SOURCE_KEY);
    }

    /**
     * Get the list of notifications
     *
     * @summary Get all notifications
     * @param hideStatuses Used to filter statuses - passes as a comma-delimited list
     * @param refreshJobs Boolean - when true, it will refresh the cache of notifications
     * @return List of job execution resources
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<JobExecutionResource> list(
            @RequestParam(value = "hide_statuses", required = false) String hideStatuses,
            @RequestParam(value = "refreshJobs", defaultValue = "FALSE") Boolean refreshJobs) {
        List<BatchStatus> statuses = new ArrayList<>();
        if (StringUtils.isNotEmpty(hideStatuses)) {
            for (String status : hideStatuses.split(",")) {
                try {
                    statuses.add(BatchStatus.valueOf(status));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid argument passed as batch status: {}", status);
                }
            }
        }
        List<JobExecutionInfo> executionInfos;
        if (refreshJobs) {
            executionInfos = findRefreshCacheLastJobs();
        } else {
            executionInfos = findLastJobs(statuses);
        }
        return executionInfos.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Gets the date when notifications were last viewed
     *
     * @summary Get notification last viewed date
     * @return The date when notifications were last viewed
     */
    @GetMapping(value = "/viewed", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public Date getLastViewedTimeEndpoint() {
        try {
            return getLastViewedTime();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the date when notifications were last viewed
     *
     * @summary Set notification last viewed date
     * @param stamp The date to set
     */
    @PostMapping(value = "/viewed", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void setLastViewedTimeEndpoint(@RequestBody Date stamp) {
        try {
            setLastViewedTime(stamp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JobExecutionResource toDTO(JobExecutionInfo entity) {
        return conversionService.convert(entity, JobExecutionResource.class);
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
            // Spring Batch 5: getStartTime returns LocalDateTime
            final java.time.LocalDateTime xStartTime = x != null ? x.getJobExecution().getStartTime() : null;
            final java.time.LocalDateTime yStartTime = y != null ? y.getJobExecution().getStartTime() : null;
            return xStartTime != null ?
                    yStartTime != null ?
                            xStartTime.isAfter(yStartTime) ? x : y
                            : x
                    : y;
        };
        final Map<String, JobExecutionInfo> allJobMap = new HashMap<>();
        final Map<String, JobExecutionInfo> userJobMap = new HashMap<>();
        
        // Fetch all job executions with parameters in a single query
        final List<JobExecution> allExecutions = jobExecutionDao.getJobExecutionsWithParams();
        
        // Iterate through results and break when we have enough
        for (JobExecution jobExec : allExecutions) {
            // Ignore completed jobs when user does not want to see them
            if (hideStatuses.contains(jobExec.getStatus())) {
                continue;
            }
            
            if (!refreshJobsOnly && isInWhiteList(jobExec)) {
                // Check if this is the current user's job
                boolean isMine = isMine(jobExec);
                if (userJobMap.size() < maxSize && isMine) {
                    JobExecutionInfo executionInfo = new JobExecutionInfo(jobExec, JobOwnerType.USER_JOB);
                    userJobMap.merge(getFoldingKey(jobExec), executionInfo, mergeFunction);
                }
                if (allJobMap.size() < maxSize) {
                    JobExecutionInfo executionInfo = new JobExecutionInfo(jobExec, JobOwnerType.ALL_JOB);
                    allJobMap.merge(getFoldingKey(jobExec), executionInfo, mergeFunction);
                }
            } else if (refreshJobsOnly) {
                // Show warming/cache refresh jobs
                if (allJobMap.size() < maxSize && jobExec.getJobInstance().getJobName().startsWith("warming ")) {
                    JobExecutionInfo executionInfo = new JobExecutionInfo(jobExec, JobOwnerType.ALL_JOB);
                    allJobMap.merge(getFoldingKey(jobExec), executionInfo, mergeFunction);
                }
            }

            // Break when we have enough results
            if ((refreshJobsOnly || userJobMap.size() >= maxSize) && allJobMap.size() >= maxSize) {
                break;
            }
        }

        final List<JobExecutionInfo> jobs = new ArrayList<>(allJobMap.values());
        jobs.addAll(userJobMap.values());
        return jobs;
    }

    @Override
    public Date getLastViewedTime() throws Exception {
        WebApiPrincipal principal = permissionManager.getAuthenticatedPrincipal();
        final UserEntity user = userRepository.findById(principal.getUserId()).orElse(null);
        return user != null ? user.getLastViewedNotificationsTime() : null;
    }

    @Override
    public void setLastViewedTime(Date stamp) throws Exception {
        WebApiPrincipal principal = permissionManager.getAuthenticatedPrincipal();
        final UserEntity user = userRepository.findById(principal.getUserId()).orElse(null);
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
        WebApiPrincipal principal = permissionManager.getAuthenticatedPrincipal();

        final String login = principal.getName();
        final String jobAuthor = jobExec.getJobParameters().getString(Constants.Params.JOB_AUTHOR);
        return Objects.equals(login, jobAuthor);
    }
}
