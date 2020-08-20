package org.ohdsi.webapi.job;

import org.springframework.batch.core.BatchStatus;

import java.util.Date;
import java.util.List;

public interface NotificationService {
    List<JobExecutionInfo> findLastJobs(List<BatchStatus> hideStatuses);

    List<JobExecutionInfo> findAllLastJobs();

    Date getLastViewedTime() throws Exception;

    void setLastViewedTime(Date stamp) throws Exception;
}
