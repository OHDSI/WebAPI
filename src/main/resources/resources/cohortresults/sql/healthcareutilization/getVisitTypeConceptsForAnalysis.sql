with filtered_heracles_results_dist as (
    select * from @results_schema.heracles_results_dist where stratum_3 <> ''
)
select distinct concept_id, concept_name
from filtered_heracles_results_dist
join @vocabulary_schema.concept on concept_id = cast(stratum_3 as INTEGER)
where analysis_id = @analysis_id and cohort_definition_id = @cohort_definition_id