-- 1318                Number of measurement records below/within/above normal range, by measurement_concept_id and unit_concept_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, count_value)
select cohort_definition_id,
  1318 as analysis_id,
  measurement_concept_id as stratum_1,
  unit_concept_id as stratum_2,
  case when o1.value_as_number < o1.range_low then 'Below Range Low'
  when o1.value_as_number >= o1.range_low and o1.value_as_number <= o1.range_high then 'Within Range'
  when o1.value_as_number > o1.range_high then 'Above Range High'
  else 'Other' end as stratum_3,
  cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct o1.measurement_id) as count_value
into #results_1318
from
@CDM_schema.measurement o1
inner join #HERACLES_cohort c1
on o1.person_id = c1.subject_id
where o1.value_as_number is not null
and o1.unit_concept_id is not null
and o1.range_low is not null
and o1.range_high is not null
--{@observation_concept_ids != ''}?{
and o1.measurement_concept_id in (@observation_concept_ids)
--}
--{@cohort_period_only == 'true'}?{
and o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
group by cohort_definition_id,
measurement_concept_id,
unit_concept_id,
case when o1.value_as_number < o1.range_low then 'Below Range Low'
when o1.value_as_number >= o1.range_low and o1.value_as_number <= o1.range_high then 'Within Range'
when o1.value_as_number > o1.range_high then 'Above Range High'
else 'Other' end
;