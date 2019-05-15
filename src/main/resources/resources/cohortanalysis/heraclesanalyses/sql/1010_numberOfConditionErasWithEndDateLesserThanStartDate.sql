-- 1010                Number of condition eras with end date < start date
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  1010 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(ce1.condition_era_id) as count_value
into #results_1010
from
@CDM_schema.condition_era ce1
inner join #HERACLES_cohort_subject c1
on ce1.person_id = c1.subject_id
where ce1.condition_era_end_date < ce1.condition_era_start_date
group by c1.cohort_definition_id
;