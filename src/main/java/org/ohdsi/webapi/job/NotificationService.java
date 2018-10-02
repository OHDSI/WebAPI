package org.ohdsi.webapi.job;

import java.util.List;

public interface NotificationService {
    List<JobExecutionResource> findAll();
}
