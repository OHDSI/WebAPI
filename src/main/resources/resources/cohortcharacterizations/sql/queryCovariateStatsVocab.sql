WITH main_table AS (
    select
      fr.covariate_id,
      fr.covariate_name,
      fr.analysis_id,
      fr.analysis_name,
      fr.concept_id,
      fr.count_value,
      fr.avg_value as stat_value,
      fr.strata_id,
      fr.strata_name,
      fr.fa_type
    from @cdm_results_schema.cc_results fr
    where fr.cc_generation_id = @cc_generation_id and fr.analysis_id = @analysis_id and fr.cohort_definition_id = @cohort_id
),
    ancestry_ref AS (
    SELECT
        ca.ancestor_concept_id as concept_id
      , c.concept_name
      , 'Ancestor' as ancestry_status
      , min_levels_of_separation
    FROM @cdm_results_schema.cc_results fr
      INNER JOIN @cdm_database_schema.concept_ancestor ca ON ca.descendant_concept_id = fr.concept_id
      JOIN @cdm_database_schema.concept c on c.concept_id = ca.ancestor_concept_id
    WHERE fr.covariate_id = @covariate_id and fr.cc_generation_id = @cc_generation_id and fr.cohort_definition_id = @cohort_id

    UNION ALL

    SELECT
        ca.descendant_concept_id as concept_id
      , c.concept_name
      , 'Descendant' as ancestry_status
      ,-1*min_levels_of_separation as min_levels_of_separation
    FROM @cdm_results_schema.cc_results fr
      INNER JOIN @cdm_database_schema.concept_ancestor ca ON ca.ancestor_concept_id = fr.concept_id
      JOIN @cdm_database_schema.concept c on c.concept_id = ca.descendant_concept_id
    WHERE fr.covariate_id = @covariate_id and fr.cc_generation_id = @cc_generation_id and fr.cohort_definition_id = @cohort_id
          and min_levels_of_separation > 0
  )
select distinct
  mt.covariate_id
  , mt.covariate_name
  , mt.analysis_id
  , mt.analysis_name
  , mt.concept_id
  , mt.count_value
  , mt.stat_value
  , mt.strata_id
  , mt.strata_name
  , mt.fa_type
  , ar.concept_name
  , ar.ancestry_status
  , ar.min_levels_of_separation
from main_table mt join ancestry_ref ar on mt.concept_id = ar.concept_id
ORDER BY mt.stat_value DESC
;