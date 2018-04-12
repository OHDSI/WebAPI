with filtered_heracles_results as (
    select * from @results_schema.heracles_results where stratum_3 <> ''
)
select distinct concept_id, concept_name
from filtered_heracles_results
join @vocabulary_schema.concept on concept_id = cast(stratum_3 as INTEGER)
where analysis_id in (@analysis_id_list) and cohort_definition_id = @cohort_definition_id and (stratum_2 = @drug_concept_id or @drug_concept_id is null)
;
