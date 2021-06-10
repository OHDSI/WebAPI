-- 1831                Number of events by duration from cohort start to all occurrences of procedure occurrence, by procedure_concept_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
  1831 as analysis_id,
  po1.procedure_concept_id as stratum_1,
  case when c1.cohort_start_date = po1.procedure_date then 0
  when c1.cohort_start_date < po1.procedure_date then floor(DATEDIFF(dd, c1.cohort_start_date, po1.procedure_date)/30)+1
  when c1.cohort_start_date > po1.procedure_date then floor(DATEDIFF(dd, c1.cohort_start_date, po1.procedure_date)/30)-1
  end as stratum_2,
  cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct p1.person_id) as count_value
into #results_1831
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.procedure_occurrence po1
on p1.person_id = po1.person_id
--{@procedure_concept_ids != ''}?{
where po1.procedure_concept_id in (@procedure_concept_ids)
--}
group by c1.cohort_definition_id,
po1.procedure_concept_id,
case when c1.cohort_start_date = po1.procedure_date then 0
when c1.cohort_start_date < po1.procedure_date then floor(DATEDIFF(dd, c1.cohort_start_date, po1.procedure_date)/30)+1
when c1.cohort_start_date > po1.procedure_date then floor(DATEDIFF(dd, c1.cohort_start_date, po1.procedure_date)/30)-1
end
;