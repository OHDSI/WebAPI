package org.ohdsi.webapi.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.testing.junit4.OpenTelemetryRule;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.core.task.SyncTaskExecutor;

/**
 * A batch job must not join the trace of the request that launched it: it runs in its own root
 * span, linked to the launching span, with SQL and everything else inside the job as children.
 */
public class TracingJobLauncherTest {

  private static final long EXECUTION_ID = 42L;

  @Rule
  public OpenTelemetryRule otel = OpenTelemetryRule.create();

  private Tracer tracer;
  private TracingJobLauncher launcher;
  private final JobParameters parameters = new JobParametersBuilder().addLong("cohort_definition_id", 7L).toJobParameters();

  @Before
  public void setUp() throws Exception {
    tracer = otel.getOpenTelemetry().getTracer("test");

    JobRepository repository = mock(JobRepository.class);
    when(repository.getLastJobExecution(anyString(), any())).thenReturn(null);
    when(repository.createJobExecution(anyString(), any()))
        .thenAnswer(inv -> new JobExecution(new JobInstance(1L, inv.getArgument(0)), EXECUTION_ID, inv.getArgument(1)));

    launcher = new TracingJobLauncher(otel.getOpenTelemetry().getTracer(TracingJobLauncher.INSTRUMENTATION_SCOPE));
    launcher.setJobRepository(repository);
    // Synchronous on purpose: the job then runs with the request's context still current,
    // which is exactly what the agent's executor instrumentation reproduces on a pool thread.
    launcher.setTaskExecutor(new SyncTaskExecutor());
    launcher.afterPropertiesSet();
  }

  @Test
  public void jobRunsInItsOwnTraceLinkedToTheLaunchingRequest() throws Exception {
    SpanContext[] insideJob = new SpanContext[1];
    SpanData request = launchFromRequest(job("generateCohort", execution -> {
      insideJob[0] = Span.current().getSpanContext();
      tracer.spanBuilder("SELECT cohort").startSpan().end();
      execution.setStatus(BatchStatus.COMPLETED);
    }));

    SpanData jobSpan = span("BatchJob generateCohort");
    SpanData sql = span("SELECT cohort");

    assertFalse("job span must be a root", jobSpan.getParentSpanContext().isValid());
    assertNotEquals(request.getTraceId(), jobSpan.getTraceId());
    assertEquals(1, jobSpan.getLinks().size());
    assertEquals(request.getSpanContext(), jobSpan.getLinks().get(0).getSpanContext());

    assertEquals("work inside the job is current under the job span", jobSpan.getSpanContext(), insideJob[0]);
    assertEquals(jobSpan.getSpanId(), sql.getParentSpanId());
    assertEquals(jobSpan.getTraceId(), sql.getTraceId());
  }

  @Test
  public void recordsJobIdentityAndOutcome() throws Exception {
    launchFromRequest(job("generateCohort", execution -> execution.setStatus(BatchStatus.COMPLETED)));

    SpanData jobSpan = span("BatchJob generateCohort");
    assertEquals("spring_batch", jobSpan.getAttributes().get(TracingJobLauncher.JOB_SYSTEM));
    assertEquals("generateCohort", jobSpan.getAttributes().get(TracingJobLauncher.JOB_NAME));
    assertEquals(Long.valueOf(EXECUTION_ID), jobSpan.getAttributes().get(TracingJobLauncher.JOB_EXECUTION_ID));
    assertEquals("COMPLETED", jobSpan.getAttributes().get(TracingJobLauncher.JOB_STATUS));
    assertEquals(StatusCode.UNSET, jobSpan.getStatus().getStatusCode());
  }

  @Test
  public void launchingSpanRecordsTheExecutionIdSoTheJobTraceCanBeFound() throws Exception {
    SpanData request = launchFromRequest(job("generateCohort", execution -> execution.setStatus(BatchStatus.COMPLETED)));
    assertEquals(Long.valueOf(EXECUTION_ID), request.getAttributes().get(TracingJobLauncher.JOB_EXECUTION_ID));
  }

  @Test
  public void failedJobIsAnErrorWithoutForwardingTheExitMessage() throws Exception {
    launchFromRequest(job("generateCohort", execution -> {
      execution.setStatus(BatchStatus.FAILED);
      execution.setExitStatus(execution.getExitStatus().addExitDescription("Key (person_id)=(123) already exists"));
    }));

    SpanData jobSpan = span("BatchJob generateCohort");
    assertEquals(StatusCode.ERROR, jobSpan.getStatus().getStatusCode());
    assertEquals("", jobSpan.getStatus().getDescription());
    assertEquals("FAILED", jobSpan.getAttributes().get(TracingJobLauncher.JOB_STATUS));
  }

  @Test
  public void stoppedJobIsNotAnError() throws Exception {
    launchFromRequest(job("generateCohort", execution -> execution.setStatus(BatchStatus.STOPPED)));
    assertEquals(StatusCode.UNSET, span("BatchJob generateCohort").getStatus().getStatusCode());
  }

  @Test
  public void exceptionFromTheJobIsRecordedAndRethrown() throws Exception {
    // TaskExecutorJobLauncher rethrows what the job throws, after marking the execution failed.
    try {
      launchFromRequest(job("generateCohort", execution -> {
        throw new IllegalStateException("boom");
      }));
    } catch (IllegalStateException expected) {
      // fall through to the assertions
    }

    SpanData jobSpan = span("BatchJob generateCohort");
    assertEquals(StatusCode.ERROR, jobSpan.getStatus().getStatusCode());
    assertTrue(jobSpan.getEvents().stream().anyMatch(e -> e.getName().equals("exception")));
  }

  @Test
  public void jobLaunchedOutsideARequestIsARootWithoutLinks() throws Exception {
    launcher.run(job("warmCache", execution -> execution.setStatus(BatchStatus.COMPLETED)), parameters);

    SpanData jobSpan = span("BatchJob warmCache");
    assertFalse(jobSpan.getParentSpanContext().isValid());
    assertTrue(jobSpan.getLinks().isEmpty());
  }

  @Test
  public void wrapperDelegatesTheJobContract() {
    // Job's default methods return a new validator per call, so pin real instances.
    JobParametersValidator validator = mock(JobParametersValidator.class);
    JobParametersIncrementer incrementer = mock(JobParametersIncrementer.class);
    Job delegate = mock(Job.class);
    when(delegate.getName()).thenReturn("generateCohort");
    when(delegate.isRestartable()).thenReturn(false);
    when(delegate.getJobParametersValidator()).thenReturn(validator);
    when(delegate.getJobParametersIncrementer()).thenReturn(incrementer);
    Job traced = new TracingJobLauncher.TracedJob(delegate, SpanContext.getInvalid(), tracer);

    assertEquals("generateCohort", traced.getName());
    assertFalse(traced.isRestartable());
    assertSame(validator, traced.getJobParametersValidator());
    assertSame(incrementer, traced.getJobParametersIncrementer());
    assertEquals(delegate.toString(), traced.toString());
  }

  private SpanData launchFromRequest(Job job) throws Exception {
    Span request = tracer.spanBuilder("POST /WebAPI/cohortdefinition/{id}/generate/{sourceKey}").startSpan();
    try (Scope ignored = request.makeCurrent()) {
      launcher.run(job, parameters);
    } finally {
      request.end();
    }
    return span("POST /WebAPI/cohortdefinition/{id}/generate/{sourceKey}");
  }

  private SpanData span(String name) {
    List<SpanData> spans = otel.getSpans();
    return spans.stream()
        .filter(s -> s.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no span named " + name + " in " + spans));
  }

  private static Job job(String name, Consumer<JobExecution> body) {
    return new Job() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public void execute(JobExecution execution) {
        body.accept(execution);
      }

      @Override
      public String toString() {
        return "job[" + name + "]";
      }
    };
  }
}
