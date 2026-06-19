package org.ohdsi.webapi.security.provisioning.service;

import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain2.NamedEntityGraph;
import com.cronutils.model.definition.CronDefinition;
import org.ohdsi.webapi.arachne.scheduler.exception.JobNotFoundException;
import org.ohdsi.webapi.arachne.scheduler.model.ScheduledTask;
import org.ohdsi.webapi.arachne.scheduler.service.BaseJobServiceImpl;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.security.provisioning.JobAlreadyExistException;
import org.ohdsi.webapi.security.provisioning.RoleGroupUtils;
import org.ohdsi.webapi.security.provisioning.model.JobHistoryItemDTO;
import org.ohdsi.webapi.security.provisioning.model.LdapProviderType;
import org.ohdsi.webapi.security.provisioning.model.RoleGroupEntity;
import org.ohdsi.webapi.security.provisioning.model.RoleGroupRepository;
import org.ohdsi.webapi.security.provisioning.model.UserImportJob;
import org.ohdsi.webapi.security.provisioning.model.UserImportJobDTO;
import org.ohdsi.webapi.security.provisioning.model.UserImportJobHistoryItem;
import org.ohdsi.webapi.security.provisioning.model.UserImportJobHistoryItemRepository;
import org.ohdsi.webapi.security.provisioning.model.UserImportJobRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ohdsi.webapi.Constants.JOB_IS_ALREADY_SCHEDULED;
import static org.ohdsi.webapi.Constants.SYSTEM_USER;

@RestController
@RequestMapping("/user/import/job")
@Transactional
@PreAuthorize("isPermitted('admin:security')")
public class UserImportJobServiceImpl extends BaseJobServiceImpl<UserImportJob> implements UserImportJobService {

  private final UserImportService userImportService;
  private final UserImportJobRepository jobRepository;
  private final RoleGroupRepository roleGroupRepository;
  private final UserImportJobHistoryItemRepository jobHistoryItemRepository;
  private final TransactionTemplate transactionTemplate;
  private final JobRepository jobRepositoryBatch;
  private final PlatformTransactionManager transactionManager;
  private final JobTemplate jobTemplate;
  private final GenericConversionService conversionService;

  public UserImportJobServiceImpl(TaskScheduler taskScheduler,
                                  CronDefinition cronDefinition,
                                  UserImportJobRepository jobRepository,
                                  UserImportService userImportService,
                                  RoleGroupRepository roleGroupRepository,
                                  UserImportJobHistoryItemRepository jobHistoryItemRepository,
                                  @Qualifier("transactionTemplateRequiresNew")
                                  TransactionTemplate transactionTemplate,
                                  JobRepository jobRepositoryBatch,
                                  PlatformTransactionManager transactionManager,
                                  JobTemplate jobTemplate,
                                  @Qualifier("conversionService") GenericConversionService conversionService) {

    super(taskScheduler, cronDefinition, jobRepository);
    this.userImportService = userImportService;
    this.jobRepository = jobRepository;
    this.roleGroupRepository = roleGroupRepository;
    this.jobHistoryItemRepository = jobHistoryItemRepository;
    this.transactionTemplate = transactionTemplate;
    this.jobRepositoryBatch = jobRepositoryBatch;
    this.transactionManager = transactionManager;
    this.jobTemplate = jobTemplate;
    this.conversionService = conversionService;
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
      roleGroupRepository.saveAll(job.getRoleGroupMapping());
    }
  }

  @Override
  protected List<UserImportJob> getActiveJobs() {

    return jobRepository.findAllByEnabledTrueAndIsClosedFalse();
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
      roleGroupRepository.deleteAll(deleted);
    }
    if (!created.isEmpty()) {
      existMapping.addAll(roleGroupRepository.saveAll(created));
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

    return Optional.ofNullable(jobRepository.findById(id).orElse(null)).map(this::assignNextExecution);
  }

  @Override
  public Stream<UserImportJobHistoryItem> getJobHistoryItems(Long id) {

    return jobHistoryItemRepository.findByUserImportId(id);
  }

  @Override
  public Optional<UserImportJobHistoryItem> getLatestHistoryItem(Long id) {

    return jobHistoryItemRepository.findFirstByUserImportIdOrderByEndTimeDesc(id);
  }

  // ==================== REST Endpoints ====================

  /**
   * Create a user import job
   */
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public UserImportJobDTO createJobEndpoint(@RequestBody UserImportJobDTO jobDTO) {
    UserImportJob job = conversionService.convert(jobDTO, UserImportJob.class);
    try {
      UserImportJob created = createJob(job);
      return conversionService.convert(created, UserImportJobDTO.class);
    } catch (JobAlreadyExistException e) {
      throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
              String.format(JOB_IS_ALREADY_SCHEDULED, job.getProviderType()));
    }
  }

  /**
   * Update a user import job
   */
  @PutMapping(
      value = "/{id}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public UserImportJobDTO updateJobEndpoint(
          @PathVariable("id") Long jobId,
          @RequestBody UserImportJobDTO jobDTO) {
    UserImportJob job = conversionService.convert(jobDTO, UserImportJob.class);
    try {
      job.setId(jobId);
      UserImportJob updated = updateJob(job);
      return conversionService.convert(updated, UserImportJobDTO.class);
    } catch (JobNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Get the user import job list
   */
  @GetMapping(
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @Transactional
  public List<UserImportJobDTO> listJobsEndpoint() {
    return getJobs().stream()
            .map(job -> conversionService.convert(job, UserImportJobDTO.class))
            .peek(job -> getLatestHistoryItem(job.getId())
                    .ifPresent(item -> job.setLastExecuted(item.getEndTime())))
            .collect(Collectors.toList());
  }

  /**
   * Get user import job by ID
   */
  @GetMapping(
      value = "/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public UserImportJobDTO getJobEndpoint(@PathVariable("id") Long id) {
    return getJob(id)
            .map(job -> conversionService.convert(job, UserImportJobDTO.class))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  /**
   * Delete user import job by ID
   */
  @DeleteMapping(
      value = "/{id}"
  )
  public void deleteJobEndpoint(@PathVariable("id") Long id) {
    UserImportJob job = getJob(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    delete(job);
  }

  /**
   * Get the user import job history
   */
  @GetMapping(
      value = "/{id}/history",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public List<JobHistoryItemDTO> getImportHistoryEndpoint(@PathVariable("id") Long id) {
    return getJobHistoryItems(id)
            .map(item -> conversionService.convert(item, JobHistoryItemDTO.class))
            .collect(Collectors.toList());
  }

  // ==================== Internal Methods ====================

  Step userImportStep() {

    UserImportTasklet userImportTasklet = new UserImportTasklet(transactionTemplate, userImportService);
    return new StepBuilder("importUsers", jobRepositoryBatch)
            .tasklet(userImportTasklet, transactionManager)
            .build();
  }

  Job buildJobForUserImportTasklet(UserImportJob job) {

    FindUsersToImportTasklet findUsersTasklet = new FindUsersToImportTasklet(transactionTemplate, userImportService);
    Step findUsersStep = new StepBuilder("findUsersForImport", jobRepositoryBatch)
            .tasklet(findUsersTasklet, transactionManager)
            .build();

    if (job.getUserRoles() != null) {
        // when user roles are already defined then we do not need to look for them
        return new JobBuilder(Constants.USERS_IMPORT, jobRepositoryBatch)
                .start(userImportStep())
                .build();
    } else {
        return new JobBuilder(Constants.USERS_IMPORT, jobRepositoryBatch)
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
