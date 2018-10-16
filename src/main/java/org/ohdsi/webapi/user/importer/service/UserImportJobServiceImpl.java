package org.ohdsi.webapi.user.importer.service;

import com.cronutils.model.definition.CronDefinition;
import com.odysseusinc.scheduler.model.ScheduledTask;
import com.odysseusinc.scheduler.service.BaseJobServiceImpl;
import org.ohdsi.webapi.user.importer.UserImportService;
import org.ohdsi.webapi.user.importer.exception.JobAlreadyExistException;
import org.ohdsi.webapi.user.importer.model.RoleGroupEntity;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.ohdsi.webapi.user.importer.repository.RoleGroupRepository;
import org.ohdsi.webapi.user.importer.repository.UserImportJobRepository;
import org.ohdsi.webapi.user.importer.utils.RoleGroupUtils;
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
  private final RoleGroupRepository roleGroupRepository;

  public UserImportJobServiceImpl(TaskScheduler taskScheduler,
                                  CronDefinition cronDefinition,
                                  UserImportJobRepository jobRepository,
                                  UserImportService userImportService,
                                  RoleGroupRepository roleGroupRepository) {

    super(taskScheduler, cronDefinition, jobRepository);
    this.userImportService = userImportService;
    this.jobRepository = jobRepository;
    this.roleGroupRepository = roleGroupRepository;
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
    List<RoleGroupEntity> existMapping = exists.getRoleGroupMapping();
    List<RoleGroupEntity> updatedMapping = updated.getRoleGroupMapping();
    List<RoleGroupEntity> deleted = RoleGroupUtils.findDeleted(existMapping, updatedMapping);
    List<RoleGroupEntity> created = RoleGroupUtils.findCreated(existMapping, updatedMapping);
    created.forEach(c -> c.setUserImportJob(exists));
    if (!deleted.isEmpty()) {
      roleGroupRepository.delete(deleted);
    }
    if (!created.isEmpty()) {
      existMapping.addAll(roleGroupRepository.save(created));
    }
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
