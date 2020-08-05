package org.ohdsi.webapi.job;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;

import java.util.Date;
import java.util.List;

public interface NotificationService {
    List<JobExecutionInfo> findLastJobs(List<BatchStatus> hideStatuses);

    Date getLastViewedTime() throws Exception;

    void setLastViewedTime(Date stamp) throws Exception;
}
