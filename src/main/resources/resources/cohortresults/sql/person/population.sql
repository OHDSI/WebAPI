(select ha1.analysis_name as attribute_name, 
  hr1.stratum_1 as attribute_value
from @ohdsi_database_schema.heracles_analysis ha1
inner join
@ohdsi_database_schema.heracles_results hr1
on ha1.analysis_id = hr1.analysis_id
where ha1.analysis_id = 0
and cohort_definition_id in (@cohortDefinitionId)
union

select ha1.analysis_name as attribute_name, 
cast(hr1.count_value as varchar) as attribute_value
from @ohdsi_database_schema.heracles_analysis ha1
inner join
@ohdsi_database_schema.heracles_results hr1
on ha1.analysis_id = hr1.analysis_id
where ha1.analysis_id = 1
and cohort_definition_id in (@cohortDefinitionId)
)
order by attribute_name desc
