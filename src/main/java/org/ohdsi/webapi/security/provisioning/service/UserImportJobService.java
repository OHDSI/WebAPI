package org.ohdsi.webapi.security.provisioning.service;

import org.ohdsi.webapi.arachne.scheduler.service.BaseJobService;
import org.ohdsi.webapi.security.provisioning.model.UserImportJob;
import org.ohdsi.webapi.security.provisioning.model.UserImportJobHistoryItem;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface UserImportJobService extends BaseJobService<UserImportJob> {

  List<UserImportJob> getJobs();

  Optional<UserImportJob> getJob(Long id);

  Stream<UserImportJobHistoryItem> getJobHistoryItems(Long id);

  Optional<UserImportJobHistoryItem> getLatestHistoryItem(Long id);
}
