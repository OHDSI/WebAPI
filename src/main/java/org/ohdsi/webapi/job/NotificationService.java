package org.ohdsi.webapi.job;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;

import java.util.Date;
import java.util.List;

public interface NotificationService {
    List<JobExecution> findLast10(List<BatchStatus> hideStatuses);

    Date getLastViewedTime() throws Exception;

    void setLastViewedTime(Date stamp) throws Exception;
}
