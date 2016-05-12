select c1.concept_id as concept_id,
	c1.concept_name as concept_name,
	num.stratum_2 as x_calendar_month,
	round(1000*(1.0*num.count_value/denom.count_value),5) as y_prevalence_1000pp
from 
	(select * from @results_database_schema.ACHILLES_results where analysis_id = 702) num
	inner join
	(select * from @results_database_schema.ACHILLES_results where analysis_id = 117) denom
	on num.stratum_2 = denom.stratum_1  --calendar year
	inner join
	@vocab_database_schema.concept c1
	on num.stratum_1 = CAST(c1.concept_id as VARCHAR)
