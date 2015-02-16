/*
CONDITION_OCCURRENCE

--drilldown of when first condition occurs relative to index
--graph:  scatterplot
--analysis_id: 1820
--x:  time (30-day increments)
--y:  %   
*/
select hr1.cohort_definition_id,
	'First' as record_type,
	c1.concept_id,
	c1.concept_name,
	hr1.duration,
	hr1.count_value,
	case when t1.count_value > 0 then 1.0*hr1.count_value / t1.count_value else 0 end as pct_persons
from
(select cohort_definition_id,
	cast(stratum_1 as integer) as concept_id,
	cast(stratum_2 as integer)*30 as duration,
	count_value
from @resultsSchema.dbo.heracles_results
where analysis_id in (1820) 
and cohort_definition_id in (@cohortDefinitionId)
) hr1
inner join
(
	select cohort_definition_id,
		-1* cast(stratum_1 as integer)*30 as duration,
		sum(count_value) over (partition by cohort_definition_id order by -1* cast(stratum_1 as integer)*30 asc) as count_value
	from
	@resultsSchema.dbo.heracles_results
	where analysis_id in (1805)
	and cohort_definition_id in (@cohortDefinitionId)
	and cast(stratum_1 as integer) > 0

	union

	select hr1.cohort_definition_id,
		cast(hr1.stratum_1 as integer)*30 as duration,
		t1.count_value - sum(hr1.count_value) over (partition by hr1.cohort_definition_id order by cast(hr1.stratum_1 as integer)*30 asc) as count_value
	from
	@resultsSchema.dbo.heracles_results hr1
	inner join
	(select cohort_definition_id, sum(count_value) as count_value 
	from @resultsSchema.dbo.heracles_results 
	where analysis_id = 1806
	and cohort_definition_id in (@cohortDefinitionId)
	group by cohort_definition_id) t1
	on hr1.cohort_definition_id = t1.cohort_definition_id
	where hr1.analysis_id in (1806)
	and hr1.cohort_definition_id in (@cohortDefinitionId)

) t1
on hr1.cohort_definition_id = t1.cohort_definition_id
and hr1.duration = t1.duration
inner join
(select cohort_definition_id,
	cast(stratum_1 as integer) as concept_id,
	sum(count_value) as count_value
from @resultsSchema.dbo.heracles_results
where analysis_id in (1820) 
and cohort_definition_id in (@cohortDefinitionId)
group by cohort_definition_id,
	cast(stratum_1 as integer)
having sum(count_value) > @minCovariatePersonCount
) ct1
on hr1.cohort_definition_id = ct1.cohort_definition_id
and hr1.concept_id = ct1.concept_id
inner join
@cdmSchema.dbo.concept c1
on hr1.concept_id = c1.concept_id
where t1.count_value > @minIntervalPersonCount

union


select hr1.cohort_definition_id,
	'All' as record_type,
	c1.concept_id,
	c1.concept_name,
	hr1.duration,
	hr1.count_value,
	case when t1.count_value > 0 then 1.0*hr1.count_value / t1.count_value else 0 end as pct_persons
from
(select cohort_definition_id,
	cast(stratum_1 as integer) as concept_id,
	cast(stratum_2 as integer)*30 as duration,
	count_value
from @resultsSchema.dbo.heracles_results
where analysis_id in (1821) 
and cohort_definition_id in (@cohortDefinitionId)
) hr1
inner join
(
	select cohort_definition_id,
		-1* cast(stratum_1 as integer)*30 as duration,
		sum(count_value) over (partition by cohort_definition_id order by -1* cast(stratum_1 as integer)*30 asc) as count_value
	from
	@resultsSchema.dbo.heracles_results
	where analysis_id in (1805)
	and cohort_definition_id in (@cohortDefinitionId)
	and cast(stratum_1 as integer) > 0

	union

	select hr1.cohort_definition_id,
		cast(hr1.stratum_1 as integer)*30 as duration,
		t1.count_value - sum(hr1.count_value) over (partition by hr1.cohort_definition_id order by cast(hr1.stratum_1 as integer)*30 asc) as count_value
	from
	@resultsSchema.dbo.heracles_results hr1
	inner join
	(select cohort_definition_id, sum(count_value) as count_value 
	from @resultsSchema.dbo.heracles_results 
	where analysis_id = 1806
	and cohort_definition_id in (@cohortDefinitionId)
	group by cohort_definition_id) t1
	on hr1.cohort_definition_id = t1.cohort_definition_id
	where hr1.analysis_id in (1806)
	and hr1.cohort_definition_id in (@cohortDefinitionId)

) t1
on hr1.cohort_definition_id = t1.cohort_definition_id
and hr1.duration = t1.duration
inner join
(select cohort_definition_id,
	cast(stratum_1 as integer) as concept_id,
	sum(count_value) as count_value
from @resultsSchema.dbo.heracles_results
where analysis_id in (1821) 
and cohort_definition_id in (@cohortDefinitionId)
group by cohort_definition_id,
	cast(stratum_1 as integer)
having sum(count_value) > @minCovariatePersonCount
) ct1
on hr1.cohort_definition_id = ct1.cohort_definition_id
and hr1.concept_id = ct1.concept_id
inner join
@cdmSchema.dbo.concept c1
on hr1.concept_id = c1.concept_id
where t1.count_value > @minIntervalPersonCount