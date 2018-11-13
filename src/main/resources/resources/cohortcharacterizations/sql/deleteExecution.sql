delete from @ohdsiSchema.batch_step_execution_context
  where
    step_execution_id in (
      select step_execution_id
        from @ohdsiSchema.batch_step_execution
        where job_execution_id = @execution_id
      );

delete from @ohdsiSchema.batch_step_execution where job_execution_id = @execution_id;

delete from @ohdsiSchema.batch_job_execution_context where job_execution_id = @execution_id;

delete from @ohdsiSchema.batch_job_execution_params where job_execution_id = @execution_id;

delete from @ohdsiSchema.batch_job_execution where job_execution_id = @execution_id;