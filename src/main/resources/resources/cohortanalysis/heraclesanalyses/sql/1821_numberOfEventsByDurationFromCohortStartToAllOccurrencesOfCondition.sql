-- 1821                Number of events by duration from cohort start to all occurrences of condition occurrence, by condition_concept_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
  1821 as analysis_id,
  co1.condition_concept_id as stratum_1,
  case when c1.cohort_start_date = co1.condition_start_date then 0
  when c1.cohort_start_date < co1.condition_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, co1.condition_start_date)/30)+1
  when c1.cohort_start_date > co1.condition_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, co1.condition_start_date)/30)-1
  end as stratum_2,
  cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct p1.person_id) as count_value
into #results_1821
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.condition_occurrence co1
on p1.person_id = co1.person_id
--{@condition_concept_ids != ''}?{
where co1.condition_concept_id in (@condition_concept_ids)
--}
group by c1.cohort_definition_id,
co1.condition_concept_id,
case when c1.cohort_start_date = co1.condition_start_date then 0
when c1.cohort_start_date < co1.condition_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, co1.condition_start_date)/30)+1
when c1.cohort_start_date > co1.condition_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, co1.condition_start_date)/30)-1
end
;