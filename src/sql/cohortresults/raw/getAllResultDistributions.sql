select * from (
	select ROW_NUMBER() OVER (ORDER BY tmp.analysis_id) as rn, *
	FROM ( select *
		from @tableQualifier.heracles_results_dist
		where cohort_definition_id = @cohortDefinitionId 
	) tmp
) tmp2
where rn <= 100;
