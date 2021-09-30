-- 1870                Number of persons by duration from cohort start to first occurrence of drug era, by drug_concept_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
  1870 as analysis_id,
  de1.drug_concept_id as stratum_1,
  case when c1.cohort_start_date = de1.first_date then 0
  when c1.cohort_start_date < de1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.first_date)/30)+1
  when c1.cohort_start_date > de1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.first_date)/30)-1
  end as stratum_2,
  cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct p1.person_id) as count_value
into #results_1870
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
inner join
(
select de0.person_id, de0.drug_concept_id, min(de0.drug_era_start_date) as first_date
from @CDM_schema.drug_era de0
inner join #HERACLES_cohort_subject c0
on de0.person_id = c0.subject_id
--{@drug_concept_ids != ''}?{
where de0.drug_concept_id in (@drug_concept_ids)
--}
group by de0.person_id, de0.drug_concept_id
) de1
on p1.person_id = de1.person_id
group by c1.cohort_definition_id,
de1.drug_concept_id,
case when c1.cohort_start_date = de1.first_date then 0
when c1.cohort_start_date < de1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.first_date)/30)+1
when c1.cohort_start_date > de1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.first_date)/30)-1
end
;