select distinct concept_id, concept_name
from @results_schema.heracles_results_dist
join @vocabulary_schema.concept on concept_id = cast((CASE WHEN stratum_2 <> '' THEN stratum_2 ELSE '0' END) as INTEGER)
where analysis_id = @analysis_id and cohort_definition_id = @cohort_definition_id
