package org.ohdsi.webapi.user.importer.service;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.cronutils.model.definition.CronDefinition;
import com.odysseusinc.scheduler.model.ScheduledTask;
import com.odysseusinc.scheduler.service.BaseJobServiceImpl;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.user.importer.model.LdapProviderType;
import org.ohdsi.webapi.user.importer.model.RoleGroupEntity;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.ohdsi.webapi.user.importer.model.UserImportJobHistoryItem;
import org.ohdsi.webapi.user.importer.repository.RoleGroupRepository;
import org.ohdsi.webapi.user.importer.repository.UserImportJobHistoryItemRepository;
import org.ohdsi.webapi.user.importer.repository.UserImportJobRepository;
import org.ohdsi.webapi.user.importer.utils.RoleGroupUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ohdsi.webapi.Constants.SYSTEM_USER;

@Service
@Transactional
public class UserImportJobServiceImpl extends BaseJobServiceImpl<UserImportJob> implements UserImportJobService {

  private final UserImportService userImportService;
  private final UserImportJobRepository jobRepository;
  private final RoleGroupRepository roleGroupRepository;
  private final UserImportJobHistoryItemRepository jobHistoryItemRepository;
  private final TransactionTemplate transactionTemplate;
  private final StepBuilderFactory stepBuilderFactory;
  private final JobBuilderFactory jobBuilders;
  private final JobTemplate jobTemplate;
  private EntityGraph jobWithMappingEntityGraph = EntityGraphUtils.fromName("jobWithMapping");

  public UserImportJobServiceImpl(TaskScheduler taskScheduler,
                                  CronDefinition cronDefinition,
                                  UserImportJobRepository jobRepository,
                                  UserImportService userImportService,
                                  RoleGroupRepository roleGroupRepository,
                                  UserImportJobHistoryItemRepository jobHistoryItemRepository,
                                  @Qualifier("transactionTemplateRequiresNew")
                                  TransactionTemplate transactionTemplate,
                                  StepBuilderFactory stepBuilderFactory,
                                  JobBuilderFactory jobBuilders,
                                  JobTemplate jobTemplate) {

    super(taskScheduler, cronDefinition, jobRepository);
    this.userImportService = userImportService;
    this.jobRepository = jobRepository;
    this.roleGroupRepository = roleGroupRepository;
    this.jobHistoryItemRepository = jobHistoryItemRepository;
    this.transactionTemplate = transactionTemplate;
    this.stepBuilderFactory = stepBuilderFactory;
    this.jobBuilders = jobBuilders;
    this.jobTemplate = jobTemplate;
  }

  @PostConstruct
  public void initializeJobs() {

    transactionTemplate.execute(transactionStatus -> {
      reassignAllJobs();
      return null;
    });
  }

  @Override
  protected void saveAdditionalFields(UserImportJob job) {
    if (job.getRoleGroupMapping() != null && !job.getRoleGroupMapping().isEmpty()) {
      job.getRoleGroupMapping().forEach(mapping -> mapping.setUserImportJob(job));
      roleGroupRepository.save(job.getRoleGroupMapping());
    }
  }

  @Override
  protected List<UserImportJob> getActiveJobs() {

    return jobRepository.findAllByEnabledTrueAndIsClosedFalse(jobWithMappingEntityGraph);
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
    exists.setPreserveRoles(updated.getPreserveRoles());
  }

  @Override
  protected ScheduledTask<UserImportJob> buildScheduledTask(UserImportJob userImportJob) {

    return new UserImportScheduledTask(userImportJob);
  }

  @Override
  public List<UserImportJob> getJobs() {

    return jobRepository.findUserImportJobsBy().map(this::assignNextExecution).collect(Collectors.toList());
  }

  @Override
  public Optional<UserImportJob> getJob(Long id) {

    return Optional.ofNullable(jobRepository.findOne(id)).map(this::assignNextExecution);
  }

  @Override
  public Stream<UserImportJobHistoryItem> getJobHistoryItems(Long id) {

    return jobHistoryItemRepository.findByUserImportId(id);
  }

  @Override
  public Optional<UserImportJobHistoryItem> getLatestHistoryItem(Long id) {

    return jobHistoryItemRepository.findFirstByUserImportIdOrderByEndTimeDesc(id);
  }

  Step userImportStep() {

    UserImportTasklet userImportTasklet = new UserImportTasklet(transactionTemplate, userImportService);
    return stepBuilderFactory.get("importUsers")
            .tasklet(userImportTasklet)
            .build();
  }

  Job buildJobForUserImportTasklet(UserImportJob job) {

    FindUsersToImportTasklet findUsersTasklet = new FindUsersToImportTasklet(transactionTemplate, userImportService);
    Step findUsersStep = stepBuilderFactory.get("findUsersForImport")
            .tasklet(findUsersTasklet)
            .build();

    if (job.getUserRoles() != null) {
        // when user roles are already defined then we do not need to look for them
        return jobBuilders.get(Constants.USERS_IMPORT)
                .start(userImportStep())
                .build();
    } else {
        return jobBuilders.get(Constants.USERS_IMPORT)
                .start(findUsersStep)
                .next(userImportStep())
                .build();
    }
  }

  private class UserImportScheduledTask extends ScheduledTask<UserImportJob> {

    UserImportScheduledTask(UserImportJob job) {
      super(job);
    }

    @Override
    public void run() {
      JobParameters jobParameters = new JobParametersBuilder()
              .addString(Constants.Params.JOB_NAME, String.format("Users import for %s", getProviderName(job.getProviderType())))
              .addString(Constants.Params.JOB_AUTHOR, SYSTEM_USER)
              .addString(Constants.Params.USER_IMPORT_ID, String.valueOf(job.getId()))
              .toJobParameters();

      Job batchJob = buildJobForUserImportTasklet(job);
      jobTemplate.launch(batchJob, jobParameters);
    }
  }

  private String getProviderName(LdapProviderType providerType) {
    switch (providerType){
      case ACTIVE_DIRECTORY:
        return "Active Directory";
      case LDAP:
        return "LDAP Server";
      default:
        return "Unknown";
    }
  }
}
