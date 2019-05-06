-- 502   Number of persons by condition occurrence start month
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  502 as analysis_id,
  YEAR(death_date)*100 + month(death_date) as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct PERSON_ID) as count_value
into #results_502
from
@CDM_schema.death d1
inner join #HERACLES_cohort c1
on d1.person_id = c1.subject_id
--{@cohort_period_only == 'true'}?{
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}
group by c1.cohort_definition_id,
YEAR(death_date)*100 + month(death_date)
;