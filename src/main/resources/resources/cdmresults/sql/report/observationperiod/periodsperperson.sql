select row_number() over (order by ar1.stratum_1) as concept_id, 
	ar1.stratum_1 as concept_name, 
	ar1.count_value as count_value
from @results_database_schema.ACHILLES_results ar1
where ar1.analysis_id = 113