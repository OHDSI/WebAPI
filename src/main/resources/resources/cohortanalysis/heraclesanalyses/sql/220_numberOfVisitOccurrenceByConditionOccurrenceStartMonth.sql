-- 220   Number of visit occurrence records by condition occurrence start month
select c1.cohort_definition_id,
  220 as analysis_id,
  YEAR(visit_start_date)*100 + month(visit_start_date) as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct visit_occurrence_id) as count_value
into #results_220
from
@CDM_schema.visit_occurrence vo1
inner join #HERACLES_cohort c1
on vo1.person_id = c1.subject_id
{@cohort_period_only == 'true'}?{
WHERE vo1.visit_start_date>=c1.cohort_start_date and vo1.visit_end_date<=c1.cohort_end_date
}
group by c1.cohort_definition_id, YEAR(visit_start_date)*100 + month(visit_start_date)
;
