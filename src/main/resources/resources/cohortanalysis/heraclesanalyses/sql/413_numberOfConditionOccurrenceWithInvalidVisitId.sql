-- 413   Number of condition occurrence records with invalid visit_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  413 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(co1.condition_occurrence_id) as count_value
into #results_413
from
@CDM_schema.condition_occurrence co1
inner join #HERACLES_cohort_subject c1
on co1.person_id = c1.subject_id
left join @CDM_schema.visit_occurrence vo1
on co1.visit_occurrence_id = vo1.visit_occurrence_id
where co1.visit_occurrence_id is not null
and vo1.visit_occurrence_id is null
group by c1.cohort_definition_id
;