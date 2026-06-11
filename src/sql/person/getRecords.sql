select 'drug' as "domain", drug_concept_id concept_id, concept_name, drug_exposure_start_date start_date, drug_exposure_end_date end_date
from @tableQualifier.drug_exposure d
join @tableQualifier.concept c on d.drug_concept_id = c.concept_id
where person_id = @personId

union all

select 'drugera' as "domain", drug_concept_id concept_id, concept_name, drug_era_start_date start_date, drug_era_end_date end_date 
from @tableQualifier.drug_era 
join @tableQualifier.concept c on c.concept_id = drug_era.drug_concept_id
where person_id = @personId  

union all 

select 'condition' as "domain", condition_concept_id concept_id, concept_name, condition_start_date start_date, condition_end_date end_date
from @tableQualifier.condition_occurrence co
join @tableQualifier.concept c on co.condition_concept_id = c.concept_id
where person_id = @personId

union all

select 'conditionera' as "domain", condition_concept_id concept_id, concept_name, condition_era_start_date start_date, condition_era_end_date end_date 
from @tableQualifier.condition_era
join @tableQualifier.concept c on c.concept_id = condition_era.condition_concept_id
where person_id = @personId  

union  all

select 'observation' as "domain", observation_concept_id concept_id, concept_name, observation_date start_date, observation_date end_date 
from @tableQualifier.observation
join @tableQualifier.concept c on c.concept_id = observation.observation_concept_id
where person_id = @personId  

union all

select 'visit' as "domain", visit_concept_id concept_id, concept_name, visit_start_date start_date, visit_end_date end_date 
from @tableQualifier.visit_occurrence
join @tableQualifier.concept c on c.concept_id = visit_occurrence.visit_concept_id
where person_id = @personId 

union all

select 'death' as "domain", death_type_concept_id concept_id, concept_name, death_date start_date, death_date end_date
from @tableQualifier.death d
join @tableQualifier.concept c on d.death_type_concept_id = c.concept_id
where person_id = @personId

union  all

select 'measurement' as "domain", measurement_concept_id concept_id, concept_name, measurement_date start_date, measurement_date end_date
from @tableQualifier.measurement m
join @tableQualifier.concept c on m.measurement_concept_id = c.concept_id
where person_id = @personId

union  all

select 'device' as "domain", device_concept_id concept_id, concept_name, device_exposure_start_date start_date, device_exposure_end_date end_date 
from @tableQualifier.device_exposure de
join @tableQualifier.concept c on de.device_concept_id = c.concept_id
where person_id = @personId

union  all

select 'procedure' as "domain", procedure_concept_id concept_id, concept_name, procedure_date start_date, procedure_date end_date 
from @tableQualifier.procedure_occurrence po
join @tableQualifier.concept c on po.procedure_concept_id = c.concept_id
where person_id = @personId

union all

select 'specimen' as "domain", specimen_concept_id concept_id, concept_name, specimen_date start_date, specimen_date end_date 
from @tableQualifier.specimen s
join @tableQualifier.concept c on s.specimen_concept_id = c.concept_id
where person_id = @personId


