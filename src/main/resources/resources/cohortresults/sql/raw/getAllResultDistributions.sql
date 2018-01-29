SELECT
  ROW_NUMBER() OVER (ORDER BY tmp.analysis_id) AS rn,
  *
FROM ( select *
        from @tableQualifier.heracles_results_dist
        where cohort_definition_id = @cohortDefinitionId ) tmp
ORDER BY tmp.analysis_id
LIMIT 100;