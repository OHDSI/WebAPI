select distinct concept_id, concept_name
from @results_schema.heracles_results_dist
join @vocabulary_schema.concept on concept_id = cast((CASE WHEN stratum_3 <> '' THEN stratum_3 ELSE '0' END) as INTEGER)
where analysis_id = @analysis_id and cohort_definition_id = @cohort_definition_id