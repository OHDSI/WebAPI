(select aa1.analysis_name as attribute_name,
  ar1.stratum_1 as attribute_value
from @results_database_schema.ACHILLES_analysis aa1
inner join
@results_database_schema.ACHILLES_results ar1
on aa1.analysis_id = ar1.analysis_id
where aa1.analysis_id = 0

union

select aa1.analysis_name as attribute_name,
cast(ar1.count_value as varchar) as attribute_value
from @results_database_schema.ACHILLES_analysis aa1
inner join
@results_database_schema.ACHILLES_results ar1
on aa1.analysis_id = ar1.analysis_id
where aa1.analysis_id = 1
)
order by attribute_name desc