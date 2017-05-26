WITH main_table AS (
    SELECT
            d.target_cohort_definition_id
            , t.cohort_definition_name target_cohort_name
            , d.outcome_cohort_definition_id
            , o.cohort_definition_name outcome_cohort_name
            , d.outcome_concept_id
            , c.concept_name 
            , d.incidence
            , d.estimate
            , ISNULL(s.seriousness, 99999) seriousness
            , CASE WHEN p.outcome_cohort_definition_id IS NULL THEN 0 ELSE 1 END on_label
            , CASE WHEN nc.nc_cohort_definition_id IS NULL THEN 0 ELSE 1 END nc
    FROM @study_results_schema.dashboard d
    INNER JOIN @study_results_schema.cohort_definition t ON d.target_cohort_definition_id = t.cohort_definition_id
    INNER JOIN @study_results_schema.cohort_definition o ON d.outcome_cohort_definition_id = o.cohort_definition_id
    INNER JOIN @study_results_schema.concept c ON c.concept_id = d.outcome_concept_id
    LEFT JOIN @study_results_schema.cohort_seriousness s ON s.cohort_definition_id = o.cohort_definition_id
    LEFT JOIN @study_results_schema.product_label p ON p.target_cohort_definition_id = d.target_cohort_definition_id
    AND p.outcome_cohort_definition_id = d.outcome_cohort_definition_id
    LEFT JOIN (
        SELECT DISTINCT target_cohort_definition_id, nc_cohort_definition_id
        FROM @study_results_schema.negative_controls
    ) nc ON nc.target_cohort_definition_id = d.target_cohort_definition_id 
            AND nc.nc_cohort_definition_id = d.outcome_cohort_definition_id
    WHERE d.target_cohort_definition_id @cohort_list_equality
      AND d.study_id = @study_id
),
ancestry_ref AS (
    SELECT 
        ca1.ancestor_concept_id as concept_id
        , 'Ancestor' as ancestry_status
        , min_levels_of_separation
    FROM @study_results_schema.dashboard d
    INNER JOIN @study_results_schema.concept_ancestor ca1 ON ca1.descendant_concept_id = d.outcome_concept_id
    WHERE d.outcome_concept_id = @outcome_concept_id

    UNION

    SELECT 
        ca1.descendant_concept_id as concept_id
        , 'Descendant' as ancestry_status
        ,-1*min_levels_of_separation as min_levels_of_separation
    FROM @study_results_schema.dashboard d
    INNER JOIN @study_results_schema.concept_ancestor ca1 ON ca1.ancestor_concept_id = d.outcome_concept_id
    WHERE d.outcome_concept_id = @outcome_concept_id
    and min_levels_of_separation > 0
)
select 
    mt.*
    , ar.ancestry_status
    , ar.min_levels_of_separation
from main_table mt
left join ancestry_ref ar on mt.outcome_concept_id = ar.concept_id
WHERE ar.ancestry_status is not null
ORDER BY ar.min_levels_of_separation DESC
;