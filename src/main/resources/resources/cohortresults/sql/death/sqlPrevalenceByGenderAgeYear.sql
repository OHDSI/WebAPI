select cast(cast(num.stratum_3 as int)*10 as varchar) + '-' + cast((cast(num.stratum_3 as int)+1)*10-1 as varchar) as trellis_name, --age decile
	c2.concept_name as series_name,  --gender
	num.stratum_1 as x_calendar_year,   -- calendar year, note, there could be blanks
	ROUND(1000*(1.0*num.count_value/denom.count_value),5) as y_prevalence_1000pp  --prevalence, per 1000 persons
from 
	(select * from @resultsSchema.dbo.heracles_results where analysis_id = 504 and cohort_definition_id in (@cohortDefinitionId)) num
	inner join
	(select * from @resultsSchema.dbo.heracles_results where analysis_id = 116 and cohort_definition_id in (@cohortDefinitionId)) denom on num.stratum_1 = denom.stratum_1  --calendar year
		and num.stratum_2 = denom.stratum_2 --gender
		and num.stratum_3 = denom.stratum_3 --age decile
	inner join @cdmSchema.dbo.concept c2 on CAST(num.stratum_2 as INT) = c2.concept_id
where c2.concept_id in (8507, 8532)
ORDER BY CAST(num.stratum_1 as INT)
