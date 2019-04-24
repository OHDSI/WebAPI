select pg.id as generation_id, ag.design
from @webapi_schema.pathway_analysis_generation pg
join @webapi_schema.analysis_generation_info ag on pg.id = ag.job_execution_id
where status = 'COMPLETED'
  and source_id = @source_id;
