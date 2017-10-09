WITH main_table AS (
	select
		f.covariate_id,
		fr.covariate_name, 
		ar.analysis_id,
		ar.analysis_name, 
		ar.domain_id,
		ar.start_day,
		ar.end_day,
		fr.concept_id,
		c.concept_name,
		f.sum_value as count_value, 
		f.average_value as stat_value
	from @cdm_results_schema.cohort_features f
	join @cdm_results_schema.cohort_features_ref fr on fr.covariate_id = f.covariate_id and fr.cohort_definition_id = f.cohort_definition_id
	JOIN @cdm_results_schema.cohort_features_analysis_ref ar on ar.analysis_id = fr.analysis_id and ar.cohort_definition_id = fr.cohort_definition_id
	LEFT JOIN @cdm_database_schema.concept c on c.concept_id = fr.concept_id
	where f.cohort_definition_id = @cohort_definition_id 
),
ancestry_ref AS (
    SELECT 
        ca.ancestor_concept_id as concept_id
        , 'Ancestor' as ancestry_status
        , min_levels_of_separation
    FROM @cdm_results_schema.cohort_features_ref fr
    INNER JOIN @cdm_database_schema.concept_ancestor ca ON ca.descendant_concept_id = fr.concept_id
    WHERE fr.covariate_id = @covariate_id and fr.cohort_definition_id = @cohort_definition_id

    UNION ALL

    SELECT 
        ca.descendant_concept_id as concept_id
        , 'Descendant' as ancestry_status
        ,-1*min_levels_of_separation as min_levels_of_separation
    FROM @cdm_results_schema.cohort_features_ref fr
    INNER JOIN @cdm_database_schema.concept_ancestor ca ON ca.ancestor_concept_id = fr.concept_id
    WHERE fr.covariate_id = @covariate_id and fr.cohort_definition_id = @cohort_definition_id
    and min_levels_of_separation > 0
)
select 
    mt.*
    , ar.ancestry_status
    , ar.min_levels_of_separation
from main_table mt
join ancestry_ref ar on mt.concept_id = ar.concept_id
ORDER BY mt.stat_value DESC
;