select c1.concept_id as concept_id,
	num.stratum_2 as x_calendar_month,
	round(1000*(1.0*num.count_value/denom.count_value),5) as y_prevalence_1000pp
from 
	(select * from ACHILLES_results where analysis_id = 1002) num
	inner join
	(select * from ACHILLES_results where analysis_id = 117) denom
	on num.stratum_2 = denom.stratum_1  --calendar year
	inner join
	@cdmSchema.dbo.concept c1 on CAST(num.stratum_1 as INT) = c1.concept_id