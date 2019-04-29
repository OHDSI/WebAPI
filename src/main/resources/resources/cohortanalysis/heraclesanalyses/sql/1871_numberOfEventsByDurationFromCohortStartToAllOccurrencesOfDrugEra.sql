-- 1871                Number of events by duration from cohort start to all occurrences of drug era, by drug_concept_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
  1871 as analysis_id,
  de1.drug_concept_id as stratum_1,
  case when c1.cohort_start_date = de1.drug_era_start_date then 0
  when c1.cohort_start_date < de1.drug_era_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.drug_era_start_date)/30)+1
  when c1.cohort_start_date > de1.drug_era_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.drug_era_start_date)/30)-1
  end as stratum_2,
  cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct p1.person_id) as count_value
into #results_1871
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.drug_era de1
on p1.person_id = de1.person_id
--{@drug_concept_ids != ''}?{
where de1.drug_concept_id in (@drug_concept_ids)
--}
group by c1.cohort_definition_id,
de1.drug_concept_id,
case when c1.cohort_start_date = de1.drug_era_start_date then 0
when c1.cohort_start_date < de1.drug_era_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.drug_era_start_date)/30)+1
when c1.cohort_start_date > de1.drug_era_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.drug_era_start_date)/30)-1
end
;