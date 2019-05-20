select
	s.source_name,
	out.execution_id
from
	ohdsi.estimation_analysis_generation gen
inner join ohdsi.output_files out on
	gen.analysis_execution_id = out.execution_id
inner join ohdsi.source s on
	s.source_id = gen.source_id
group by
	s.source_name,
	out.execution_id
union
select
	s.source_name,
	out.execution_id
from
	ohdsi.prediction_analysis_generation gen
inner join ohdsi.output_files out on
	gen.analysis_execution_id = out.execution_id
inner join ohdsi.source s on
	s.source_id = gen.source_id
group by
	s.source_name,
	out.execution_id;