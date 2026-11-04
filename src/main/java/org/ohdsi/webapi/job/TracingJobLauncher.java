package org.ohdsi.webapi.job;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

/**
 * Runs every batch job in its own trace.
 *
 * <p>Jobs execute on {@code batchTaskExecutor}. When an OpenTelemetry Java agent is attached, it
 * carries the submitting HTTP request's context into that pool, so the SQL of a cohort generation
 * would otherwise join the request's trace and stretch it over minutes or hours. Instead, each job
 * starts a new root span, linked to the span that launched it, and the launching span records the
 * job execution id. Short request traces and per-job traces are also what make tail sampling
 * (keep errors and slow traces) practical in a collector.
 *
 * <p>Only the OpenTelemetry API is used, which is a no-op unless an agent or SDK is present, so
 * deployments without telemetry are unaffected. The agent's own Spring Batch instrumentation does
 * not support Spring Batch 5 (opentelemetry-java-instrumentation#7306).
 */
public class TracingJobLauncher extends TaskExecutorJobLauncher {

  static final String INSTRUMENTATION_SCOPE = "org.ohdsi.webapi.job";

  static final AttributeKey<String> JOB_SYSTEM = AttributeKey.stringKey("job.system");
  static final AttributeKey<String> JOB_NAME = AttributeKey.stringKey("spring_batch.job.name");
  static final AttributeKey<Long> JOB_EXECUTION_ID = AttributeKey.longKey("spring_batch.job.execution.id");
  static final AttributeKey<String> JOB_STATUS = AttributeKey.stringKey("spring_batch.job.status");

  private final Tracer tracer;

  public TracingJobLauncher() {
    this(GlobalOpenTelemetry.getTracer(INSTRUMENTATION_SCOPE));
  }

  TracingJobLauncher(Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
  public JobExecution run(Job job, JobParameters jobParameters)
      throws JobExecutionAlreadyRunningException, JobRestartException,
      JobInstanceAlreadyCompleteException, JobParametersInvalidException {
    Span launching = Span.current();
    JobExecution execution = super.run(new TracedJob(job, launching.getSpanContext(), tracer), jobParameters);
    if (execution.getId() != null) {
      launching.setAttribute(JOB_EXECUTION_ID, execution.getId());
    }
    return execution;
  }

  /**
   * Delegates everything to the real job, and wraps {@link #execute} in the job's root span. It is
   * created on the launching thread, which is where the launching span context is captured; the
   * span itself starts on the executor thread.
   */
  static final class TracedJob implements Job {

    private final Job delegate;
    private final SpanContext launchedBy;
    private final Tracer tracer;

    TracedJob(Job delegate, SpanContext launchedBy, Tracer tracer) {
      this.delegate = delegate;
      this.launchedBy = launchedBy;
      this.tracer = tracer;
    }

    @Override
    public String getName() {
      return delegate.getName();
    }

    @Override
    public boolean isRestartable() {
      return delegate.isRestartable();
    }

    @Override
    public JobParametersIncrementer getJobParametersIncrementer() {
      return delegate.getJobParametersIncrementer();
    }

    @Override
    public JobParametersValidator getJobParametersValidator() {
      return delegate.getJobParametersValidator();
    }

    @Override
    public void execute(JobExecution execution) {
      SpanBuilder builder = tracer.spanBuilder("BatchJob " + delegate.getName())
          .setNoParent()
          .setSpanKind(SpanKind.INTERNAL)
          .setAttribute(JOB_SYSTEM, "spring_batch")
          .setAttribute(JOB_NAME, delegate.getName());
      if (execution.getId() != null) {
        builder.setAttribute(JOB_EXECUTION_ID, execution.getId());
      }
      if (launchedBy.isValid()) {
        builder.addLink(launchedBy);
      }
      Span span = builder.startSpan();
      try (Scope ignored = span.makeCurrent()) {
        delegate.execute(execution);
      } catch (RuntimeException | Error e) {
        span.recordException(e);
        span.setStatus(StatusCode.ERROR);
        throw e;
      } finally {
        BatchStatus status = execution.getStatus();
        if (status != null) {
          span.setAttribute(JOB_STATUS, status.name());
          // No description: exit messages can carry values from the failing SQL.
          if (status.isUnsuccessful()) {
            span.setStatus(StatusCode.ERROR);
          }
        }
        span.end();
      }
    }

    @Override
    public String toString() {
      return delegate.toString();
    }
  }
}
