select ROWCOUNT() OVER (ORDER BY tmp.analysis_id) as rn , *
FROM ( select *
        from @tableQualifier.heracles_results_dist
        where cohort_definition_id = @cohortDefinitionId ) tmp
Where rn <= 100;


