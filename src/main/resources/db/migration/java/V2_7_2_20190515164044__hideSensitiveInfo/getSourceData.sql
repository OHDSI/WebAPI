select
	s.source_name,
	out.execution_id
from
	@webapi_schema.estimation_analysis_generation gen
inner join @webapi_schema.output_files out on
	gen.analysis_execution_id = out.execution_id
inner join @webapi_schema.source s on
	s.source_id = gen.source_id
group by
	s.source_name,
	out.execution_id
union
select
	s.source_name,
	out.execution_id
from
	@webapi_schema.prediction_analysis_generation gen
inner join @webapi_schema.output_files out on
	gen.analysis_execution_id = out.execution_id
inner join @webapi_schema.source s on
	s.source_id = gen.source_id
group by
	s.source_name,
	out.execution_id;