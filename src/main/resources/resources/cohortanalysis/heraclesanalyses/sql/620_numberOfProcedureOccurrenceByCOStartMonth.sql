-- 620   Number of procedure occurrence records by condition occurrence start month
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  620 as analysis_id,
  YEAR(procedure_date)*100 + month(procedure_date) as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct po1.procedure_occurrence_id) as count_value
into #results_620
from
@CDM_schema.procedure_occurrence po1
inner join #HERACLES_cohort c1
on po1.person_id = c1.subject_id
--{@procedure_concept_ids != '' | @cohort_period_only == 'true'}?{
WHERE
--{@cohort_period_only == 'true'}?{
po1.procedure_date>=c1.cohort_start_date and po1.procedure_date<=c1.cohort_end_date
--}
--{@procedure_concept_ids != '' & @cohort_period_only == 'true'}?{
AND
--}
--{@procedure_concept_ids != ''}?{
po1.procedure_concept_id in (@procedure_concept_ids)
--}
--}
group by c1.cohort_definition_id,
YEAR(procedure_date)*100 + month(procedure_date)
;