-- Derives the number of people in each cohort/strata from stored preset prevalence rows (avg_value = count_value / N).
-- Returns every candidate N with the number of rows that support it; the caller keeps the most frequent one.
select r.cohort_definition_id,
       r.strata_id,
       CAST(ROUND(r.count_value / r.avg_value, 0) AS BIGINT) as person_count,
       COUNT(*) as row_count
from @results_database_schema.cc_results r
where r.cc_generation_id = @cohort_characterization_generation_id
  and r.type = 'PREVALENCE'
  and r.fa_type = 'PRESET'
  and r.count_value > 0
  and r.avg_value > 0
group by r.cohort_definition_id, r.strata_id, CAST(ROUND(r.count_value / r.avg_value, 0) AS BIGINT)
