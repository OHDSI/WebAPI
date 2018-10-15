package org.ohdsi.webapi.user.importer.service;

import com.odysseusinc.scheduler.service.BaseJobService;
import org.ohdsi.webapi.user.importer.model.UserImportJob;

import java.util.List;
import java.util.Optional;

public interface UserImportJobService extends BaseJobService<UserImportJob> {

  List<UserImportJob> getJobs();

  Optional<UserImportJob> getJob(Long id);
}
