with available_concepts as (
	-- eventually we could apply filters here
	select * 
	from @tableQualifier.concept
	-- where concept_id in ( some set of concept ids returned from a concept set expression )
), eras as (
  select 'drug' era_type, drug_concept_id concept_id, concept_name, drug_era_start_date start_date, drug_era_end_date end_date 
  from @tableQualifier.drug_era 
  join available_concepts on available_concepts.concept_id = drug_era.drug_concept_id
  where person_id = @personId  

  union

  select 'condition' era_type, condition_concept_id concept_id, concept_name, condition_era_start_date start_date, condition_era_end_date end_date 
  from @tableQualifier.condition_era
  join available_concepts on available_concepts.concept_id = condition_era.condition_concept_id
  where person_id = @personId  

  union 

  select 'observation' era_type, observation_concept_id concept_id, concept_name, observation_date start_date, observation_date end_date 
  from @tableQualifier.observation
  join available_concepts on available_concepts.concept_id = observation.observation_concept_id
  where person_id = @personId  

  union

  select 'visit' era_type, visit_concept_id concept_id, concept_name, visit_start_date start_date, visit_end_date end_date 
  from @tableQualifier.visit_occurrence
  join available_concepts on available_concepts.concept_id = visit_occurrence.visit_concept_id
  where person_id = @personId 
)
select * 
from eras
order by start_date asc