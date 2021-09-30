-- 613   Number of procedure occurrence records with invalid visit_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  613 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(po1.procedure_occurrence_id) as count_value
into #results_613
from
@CDM_schema.procedure_occurrence po1
inner join #HERACLES_cohort_subject c1
on po1.person_id = c1.subject_id
left join @CDM_schema.visit_occurrence vo1
on po1.visit_occurrence_id = vo1.visit_occurrence_id
where po1.visit_occurrence_id is not null
and vo1.visit_occurrence_id is null
group by c1.cohort_definition_id
;