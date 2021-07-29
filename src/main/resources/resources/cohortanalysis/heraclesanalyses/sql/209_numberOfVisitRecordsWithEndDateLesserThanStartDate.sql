--209   Number of visit records with end date < start date
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  209 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(vo1.visit_occurrence_id) as count_value
into #results_209
from
@CDM_schema.visit_occurrence vo1
inner join #HERACLES_cohort_subject c1
on vo1.person_id = c1.subject_id
where visit_end_date < visit_start_date
group by c1.cohort_definition_id
;