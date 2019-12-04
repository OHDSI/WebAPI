-- 505   Number of death records, by death_type_concept_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  505 as analysis_id,
  death_type_concept_id as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct PERSON_ID) as count_value
into #results_505
from
@CDM_schema.death d1
inner join #HERACLES_cohort c1
on d1.person_id = c1.subject_id
--{@cohort_period_only == 'true'}?{
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}
group by c1.cohort_definition_id,
death_type_concept_id
;