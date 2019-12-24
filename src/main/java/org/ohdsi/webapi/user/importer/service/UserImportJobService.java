package org.ohdsi.webapi.user.importer.service;

import com.odysseusinc.scheduler.service.BaseJobService;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.ohdsi.webapi.user.importer.model.UserImportJobHistoryItem;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface UserImportJobService extends BaseJobService<UserImportJob> {

  List<UserImportJob> getJobs();

  Optional<UserImportJob> getJob(Long id);

  Stream<UserImportJobHistoryItem> getJobHistoryItems(Long id);

  Optional<UserImportJobHistoryItem> getLatestHistoryItem(Long id);
}
