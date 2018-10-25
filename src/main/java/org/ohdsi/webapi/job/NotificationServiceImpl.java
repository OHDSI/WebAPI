package org.ohdsi.webapi.job;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.batch.admin.service.SearchableJobExecutionDao;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final int MAX_SIZE = 10;
    private static final int PAGE_SIZE = MAX_SIZE * 10;
    private static final List<String> WHITE_LIST = new ArrayList<>();
    private static final List<String> FOLDING_KEYS = new ArrayList<>();

    private final SearchableJobExecutionDao jobExecutionDao;
    private final PermissionManager permissionManager;
    private final UserRepository userRepository;

    public NotificationServiceImpl(SearchableJobExecutionDao jobExecutionDao, List<GeneratesNotification> whiteList, PermissionManager permissionManager, UserRepository userRepository) {
        this.jobExecutionDao = jobExecutionDao;
        this.permissionManager = permissionManager;
        this.userRepository = userRepository;
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
            for (JobExecution jobExec: page) {
                if (isInWhiteList(jobExec) && isMine(jobExec)) {
                    result.merge(getFoldingKey(jobExec), jobExec, (x, y) -> {
                        final Date xStartTime = x.getStartTime();
                        final Date yStartTime = y.getStartTime();
                        return xStartTime != null ?  yStartTime != null ? xStartTime.after(yStartTime) ? x : y : x: y;
                    });
                }
                if (result.size() >= MAX_SIZE) {
                    break;
                }
            }
        }
        return new ArrayList<>(result.values());
    }

    @Override
    public Date getLastViewedTime() throws Exception {
        final UserEntity user = permissionManager.getCurrentUser();
        return user != null ? user.getLastViewedNotificationsTime() : null;
    }

    @Override
    public void setLastViewedTime(Date stamp) throws Exception {
        final UserEntity user = permissionManager.getCurrentUser();
        user.setLastViewedNotificationsTime(stamp);
        userRepository.save(user);
    }

    private static String getFoldingKey(JobExecution entity) {
        final Optional<String> key = entity.getJobParameters().getParameters().keySet().stream().filter(FOLDING_KEYS::contains).findAny();
        return key.isPresent() ? entity.getJobParameters().getString(key.get()) : String.valueOf(entity.getId());
    }

    private static boolean isInWhiteList(JobExecution entity) {
        return WHITE_LIST.contains(entity.getJobInstance().getJobName());
    }
    
    private boolean isMine(JobExecution jobExec) {
        final String login = permissionManager.getSubjectName();
        final String jobAuthor = jobExec.getJobParameters().getString(Constants.Params.JOB_AUTHOR);
        return jobAuthor == null || Objects.equals(login, jobAuthor);
    }
}
