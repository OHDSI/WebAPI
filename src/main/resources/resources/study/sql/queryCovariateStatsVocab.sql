WITH main_table AS (
    SELECT 
        ar1.covariate_id
        , ar1.covariate_name 
        , ar1.analysis_id
        , ar1.analysis_name 
        , ar1.domain_id
        , ar1.time_window
        , ar1.concept_id
        , c1.concept_name
        , sr1.count_value
        , sr1.stat_value
				, sr1.z_score
    FROM @study_results_schema.cohort_summary_results sr1 
    INNER JOIN @study_results_schema.cohort_summary_analysis_ref ar1 ON sr1.covariate_id = ar1.covariate_id
    INNER JOIN (select analysis_id from @study_results_schema.cohort_summary_analysis_ref where covariate_id = @covariate_id) a1 ON ar1.analysis_id = a1.analysis_id
    INNER JOIN @study_results_schema.concept c1 ON c1.concept_id = ar1.concept_id
    WHERE sr1.cohort_definition_id = @cohort_definition_id 
      AND sr1.source_id = @source_id
),
ancestry_ref AS (
    SELECT 
        ca1.ancestor_concept_id as concept_id
        , 'Ancestor' as ancestry_status
        , min_levels_of_separation
    FROM @study_results_schema.cohort_summary_analysis_ref ar
    INNER JOIN @study_results_schema.concept_ancestor ca1 ON ca1.descendant_concept_id = ar.concept_id
    WHERE ar.covariate_id = @covariate_id

    UNION

    SELECT 
        ca1.descendant_concept_id as concept_id
        , 'Descendant' as ancestry_status
        ,-1*min_levels_of_separation as min_levels_of_separation
    FROM @study_results_schema.cohort_summary_analysis_ref ar
    INNER JOIN @study_results_schema.concept_ancestor ca1 ON ca1.ancestor_concept_id = ar.concept_id
    WHERE ar.covariate_id = @covariate_id
    and min_levels_of_separation > 0
)
select 
    mt.*
    , ar.ancestry_status
    , ar.min_levels_of_separation
from main_table mt
left join ancestry_ref ar on mt.concept_id = ar.concept_id
WHERE ar.ancestry_status is not null
ORDER BY mt.stat_value DESC
;