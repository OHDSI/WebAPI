select distinct concept_id, concept_name
from @results_schema.heracles_results
join @vocabulary_schema.concept on concept_id = cast((CASE WHEN stratum_3 <> '' THEN stratum_3 ELSE '0' END) as INTEGER)
where analysis_id in (@analysis_id_list) and cohort_definition_id = @cohort_definition_id and (stratum_2 = CAST(@drug_concept_id AS VARCHAR(255)) or CAST(@drug_concept_id AS VARCHAR(255)) is null)
;
