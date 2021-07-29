-- 1008                Number of condition eras with invalid person
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  1008 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(ce1.condition_era_id) as count_value
into #results_1008
from
@CDM_schema.condition_era ce1
inner join #HERACLES_cohort_subject c1
on ce1.person_id = c1.subject_id
left join @CDM_schema.person p1
on p1.person_id = ce1.person_id
where p1.person_id is null
group by c1.cohort_definition_id
;