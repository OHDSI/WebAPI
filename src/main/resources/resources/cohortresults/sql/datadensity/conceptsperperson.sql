select 'Condition occurrence' as Category,
	hrd1.min_value as min_value,
	hrd1.p10_value as p10_value,
	hrd1.p25_value as p25_value,
	hrd1.median_value as median_value,
	hrd1.p75_value as p75_value,
	hrd1.p90_value as p90_value,
	hrd1.max_value as max_value,
	0 as concept_id
from @ohdsi_database_schema.heracles_results_dist hrd1
where hrd1.analysis_id = 403
and cohort_definition_id in (@cohortDefinitionId)

union

select 'Procedure occurrence' as Category,
	hrd1.min_value as min_value,
	hrd1.p10_value as p10_value,
	hrd1.p25_value as p25_value,
	hrd1.median_value as median_value,
	hrd1.p75_value as p75_value,
	hrd1.p90_value as p90_value,
	hrd1.max_value as max_value,
	0 as concept_id
from @ohdsi_database_schema.heracles_results_dist hrd1
where hrd1.analysis_id = 603
and cohort_definition_id in (@cohortDefinitionId)

union

select 'Drug exposure' as Category,
	hrd1.min_value as min_value,
	hrd1.p10_value as p10_value,
	hrd1.p25_value as p25_value,
	hrd1.median_value as median_value,
	hrd1.p75_value as p75_value,
	hrd1.p90_value as p90_value,
	hrd1.max_value as max_value,
	0 as concept_id
from @ohdsi_database_schema.heracles_results_dist hrd1
where hrd1.analysis_id = 703
and cohort_definition_id in (@cohortDefinitionId)

union

select 'Observation' as Category,
	hrd1.min_value as min_value,
	hrd1.p10_value as p10_value,
	hrd1.p25_value as p25_value,
	hrd1.median_value as median_value,
	hrd1.p75_value as p75_value,
	hrd1.p90_value as p90_value,
	hrd1.max_value as max_value,
	0 as concept_id
from @ohdsi_database_schema.heracles_results_dist hrd1
where hrd1.analysis_id = 803
and cohort_definition_id in (@cohortDefinitionId)

union

select 'Drug era' as Category,
	hrd1.min_value as min_value,
	hrd1.p10_value as p10_value,
	hrd1.p25_value as p25_value,
	hrd1.median_value as median_value,
	hrd1.p75_value as p75_value,
	hrd1.p90_value as p90_value,
	hrd1.max_value as max_value,
	0 as concept_id
from @ohdsi_database_schema.heracles_results_dist hrd1
where hrd1.analysis_id = 903
and cohort_definition_id in (@cohortDefinitionId)
union

select 'Condition era' as Category,
	hrd1.min_value as min_value,
	hrd1.p10_value as p10_value,
	hrd1.p25_value as p25_value,
	hrd1.median_value as median_value,
	hrd1.p75_value as p75_value,
	hrd1.p90_value as p90_value,
	hrd1.max_value as max_value,
	0 as concept_id
from @ohdsi_database_schema.heracles_results_dist hrd1
where hrd1.analysis_id = 1003
and cohort_definition_id in (@cohortDefinitionId)

