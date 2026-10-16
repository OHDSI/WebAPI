package org.ohdsi.webapi.service;

import org.junit.Test;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.job.SearchableJobExecutionDao;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class JobServiceTest {

  @Test
  public void cancelJobExecution_rehydratesCandidateAndStopsTaskletByExecutionId() throws Exception {
    JobExplorer jobExplorer = mock(JobExplorer.class);
    SearchableJobExecutionDao jobExecutionDao = mock(SearchableJobExecutionDao.class);
    JobRepository jobRepository = mock(JobRepository.class);
    JobService jobService = new JobService(jobExplorer, jobExecutionDao, jobRepository, mock(JobTemplate.class));
    JobExecution candidate = mock(JobExecution.class);
    JobExecution hydratedExecution = mock(JobExecution.class);
    StepExecution stepExecution = mock(StepExecution.class);
    TaskletStep taskletStep = mock(TaskletStep.class);
    StoppableTasklet tasklet = mock(StoppableTasklet.class);
    Job job = mock(Job.class, withSettings().extraInterfaces(StepLocator.class));

    when(candidate.getId()).thenReturn(10L);
    when(jobExecutionDao.getRunningJobExecutions()).thenReturn(Collections.singleton(candidate));
    when(jobExplorer.getJobExecution(10L)).thenReturn(hydratedExecution);
    when(hydratedExecution.getId()).thenReturn(10L);
    when(hydratedExecution.getJobId()).thenReturn(1L);
    when(hydratedExecution.getStepExecutions()).thenReturn(Collections.singleton(stepExecution));
    when(stepExecution.getStatus()).thenReturn(BatchStatus.STARTED);
    when(stepExecution.getStepName()).thenReturn("step");
    when(((StepLocator) job).getStep("step")).thenReturn(taskletStep);
    when(taskletStep.getTasklet()).thenReturn(tasklet);
    addRunningJob(jobService, 10L, job);

    assertTrue(jobService.cancelJobExecution(execution -> execution == hydratedExecution));

    verify(jobExplorer).getJobExecution(10L);
    verify(tasklet).stop();
    verify(jobRepository).update(hydratedExecution);
  }

  @Test
  public void cancelJobExecution_marksPendingExecutionStoppingWithoutLocalJob() {
    JobExplorer jobExplorer = mock(JobExplorer.class);
    SearchableJobExecutionDao jobExecutionDao = mock(SearchableJobExecutionDao.class);
    JobRepository jobRepository = mock(JobRepository.class);
    JobService jobService = new JobService(jobExplorer, jobExecutionDao, jobRepository, mock(JobTemplate.class));
    JobExecution candidate = mock(JobExecution.class);
    JobExecution hydratedExecution = mock(JobExecution.class);

    when(candidate.getId()).thenReturn(10L);
    when(jobExecutionDao.getRunningJobExecutions()).thenReturn(Collections.singleton(candidate));
    when(jobExplorer.getJobExecution(10L)).thenReturn(hydratedExecution);
    when(hydratedExecution.getId()).thenReturn(10L);
    when(hydratedExecution.getStepExecutions()).thenReturn(Collections.emptySet());

    assertTrue(jobService.cancelJobExecution(execution -> execution == hydratedExecution));

    verify(jobRepository).update(hydratedExecution);
  }

  @Test
  public void cancelJobExecution_returnsFalseWhenNoRunningExecutionMatches() {
    SearchableJobExecutionDao jobExecutionDao = mock(SearchableJobExecutionDao.class);
    when(jobExecutionDao.getRunningJobExecutions()).thenReturn(Collections.emptyList());
    JobService jobService = new JobService(mock(JobExplorer.class), jobExecutionDao,
        mock(JobRepository.class), mock(JobTemplate.class));

    assertFalse(jobService.cancelJobExecution(execution -> true));
  }

  @Test
  public void stopJob_releasesStepContextWhenTaskletStopThrows() {
    JobRepository jobRepository = mock(JobRepository.class);
    JobService jobService = new JobService(mock(JobExplorer.class), mock(SearchableJobExecutionDao.class),
        jobRepository, mock(JobTemplate.class));
    JobExecution execution = mock(JobExecution.class);
    StepExecution stepExecution = mock(StepExecution.class);
    TaskletStep taskletStep = mock(TaskletStep.class);
    StoppableTasklet tasklet = mock(StoppableTasklet.class);
    Job job = mock(Job.class, withSettings().extraInterfaces(StepLocator.class));

    when(execution.getStepExecutions()).thenReturn(Collections.singleton(stepExecution));
    when(stepExecution.getStatus()).thenReturn(BatchStatus.STARTED);
    when(stepExecution.getStepName()).thenReturn("step");
    when(((StepLocator) job).getStep("step")).thenReturn(taskletStep);
    when(taskletStep.getTasklet()).thenReturn(tasklet);
    doThrow(new IllegalStateException("stop failed")).when(tasklet).stop();

    try {
      jobService.stopJob(execution, job);
      fail("Expected tasklet stop failure to propagate");
    } catch (IllegalStateException expected) {
      assertNull(StepSynchronizationManager.getContext());
    }
  }

  @SuppressWarnings("unchecked")
  private void addRunningJob(JobService jobService, Long executionId, Job job) throws Exception {
    Field jobMap = JobService.class.getDeclaredField("jobMap");
    jobMap.setAccessible(true);
    ((Map<Long, Job>) jobMap.get(jobService)).put(executionId, job);
  }
}