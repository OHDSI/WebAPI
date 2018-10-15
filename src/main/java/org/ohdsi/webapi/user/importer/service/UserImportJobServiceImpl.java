package org.ohdsi.webapi.user.importer.service;

import com.cronutils.model.definition.CronDefinition;
import com.odysseusinc.scheduler.model.ScheduledTask;
import com.odysseusinc.scheduler.repository.ArachneJobRepository;
import com.odysseusinc.scheduler.service.BaseJobServiceImpl;
import org.ohdsi.webapi.user.importer.UserImportService;
import org.ohdsi.webapi.user.importer.exception.JobAlreadyExistException;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.ohdsi.webapi.user.importer.repository.UserImportJobRepository;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class UserImportJobServiceImpl extends BaseJobServiceImpl<UserImportJob> implements UserImportJobService {

  private final UserImportService userImportService;
  private final UserImportJobRepository jobRepository;

  public UserImportJobServiceImpl(TaskScheduler taskScheduler,
                                  CronDefinition cronDefinition,
                                  UserImportJobRepository jobRepository,
                                  UserImportService userImportService) {

    super(taskScheduler, cronDefinition, jobRepository);
    this.userImportService = userImportService;
    this.jobRepository = jobRepository;
  }

  @Override
  protected void beforeCreate(UserImportJob job) {

    UserImportJob exists = jobRepository.findByProviderType(job.getProviderType());
    if (Objects.nonNull(exists)) {
      throw new JobAlreadyExistException();
    }
  }

  @Override
  protected void updateAdditionalFields(UserImportJob exists, UserImportJob updated) {

    exists.setProviderType(updated.getProviderType());
  }

  @Override
  protected ScheduledTask<UserImportJob> buildScheduledTask(UserImportJob userImportJob) {

    return new UserImportScheduledTask(userImportJob);
  }

  @Override
  public List<UserImportJob> getJobs() {

    return jobRepository.findAll();
  }

  @Override
  public Optional<UserImportJob> getJob(Long id) {

    return Optional.ofNullable(jobRepository.findOne(id));
  }

  private class UserImportScheduledTask extends ScheduledTask<UserImportJob> {

    UserImportScheduledTask(UserImportJob job) {
      super(job);
    }

    @Override
    public void run() {

    }
  }
}
