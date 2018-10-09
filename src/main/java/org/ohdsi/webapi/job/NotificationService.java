package org.ohdsi.webapi.job;

import org.springframework.batch.core.JobExecution;

import java.util.List;

public interface NotificationService {
    List<JobExecution> findAll();
}
