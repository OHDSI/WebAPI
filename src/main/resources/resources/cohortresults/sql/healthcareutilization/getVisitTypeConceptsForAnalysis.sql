select distinct concept_id, concept_name 
from @results_schema.heracles_results_dist 
join @vocabulary_schema.concept on cast(concept_id as varchar(19)) = stratum_3
where analysis_id = @analysis_id and cohort_definition_id = @cohort_definition_id
