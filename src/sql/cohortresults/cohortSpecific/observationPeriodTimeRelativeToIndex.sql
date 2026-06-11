--observation period time, relative to index
select hr1.cohort_definition_id,
		hr1.duration,
		hr1.count_value,
		1.0*hr1.count_value/t1.count_value as pct_persons
from
(
select cohort_definition_id,
		-1* CAST(CASE WHEN analysis_id = 1805 THEN stratum_1 ELSE null END AS INT)*30 as duration,
		sum(count_value) over (partition by cohort_definition_id
		order by -1* CAST(CASE WHEN analysis_id = 1805 THEN stratum_1 ELSE null END AS INT)*30 asc) as count_value
	from
	@ohdsi_database_schema.heracles_results
	where analysis_id = 1805
	and cohort_definition_id = @cohortDefinitionId
	and CAST(CASE WHEN analysis_id = 1805 THEN stratum_1 ELSE null END AS INT) > 0

	union

	select hr1.cohort_definition_id,
	  CAST(CASE WHEN hr1.analysis_id = 1806 THEN hr1.stratum_1 ELSE null END AS INT)*30 as duration,
		t1.count_value - sum(hr1.count_value) over (partition by hr1.cohort_definition_id
		order by CAST(CASE WHEN hr1.analysis_id = 1806 THEN hr1.stratum_1 ELSE null END AS INT)*30 asc) as count_value
	from @ohdsi_database_schema.heracles_results hr1
	inner join (
    select cohort_definition_id, sum(count_value) as count_value 
    from @ohdsi_database_schema.heracles_results 
    where analysis_id = 1806
      and cohort_definition_id = @cohortDefinitionId
    group by cohort_definition_id
  ) t1 on hr1.cohort_definition_id = t1.cohort_definition_id
	where hr1.analysis_id = 1806
	and hr1.cohort_definition_id = @cohortDefinitionId
) hr1,
(select count_value from @ohdsi_database_schema.heracles_results where analysis_id = 1 and cohort_definition_id = @cohortDefinitionId) t1
