-- 711   Number of drug exposure records with end date < start date
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  711 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(de1.drug_exposure_id) as count_value
into #results_711
from
@CDM_schema.drug_exposure de1
inner join #HERACLES_cohort_subject c1
on de1.person_id = c1.subject_id
where de1.drug_exposure_end_date < de1.drug_exposure_start_date
group by c1.cohort_definition_id
;