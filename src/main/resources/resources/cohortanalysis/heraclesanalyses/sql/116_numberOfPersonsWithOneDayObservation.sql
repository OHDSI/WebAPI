-- 116   Number of persons with at least one day of observation in each year by gender and age decile
-- Note: using temp table instead of nested query because this gives vastly improved performance in Oracle

WITH op_date_range AS (
    select
      MIN(observation_period_start_date) as op_start_date,
      MAX(observation_period_end_date) as op_end_date
    FROM @CDM_schema.person p1
      inner join #HERACLES_cohort_subject c1
                  on p1.person_id = c1.subject_id
    inner join
    @CDM_schema.observation_period op1
    on p1.person_id = op1.person_id
)
SELECT year as obs_year
INTO #temp_dates_3
FROM #tmp_years years, op_date_range WHERE years.year BETWEEN YEAR(op_date_range.op_start_date) AND YEAR(op_date_range.op_end_date)
;

--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, count_value)
select c1.cohort_definition_id,
  116 as analysis_id,
  t1.obs_year as stratum_1,
  p1.gender_concept_id as stratum_2,
  floor((t1.obs_year - p1.year_of_birth)/10) as stratum_3,
  cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct p1.PERSON_ID) as count_value
into #results_116
FROM @CDM_schema.person p1
inner join #HERACLES_cohort_subject c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.observation_period op1
on p1.person_id = op1.person_id
,
#temp_dates_3 t1
where year(op1.observation_period_START_DATE) <= t1.obs_year
and year(op1.observation_period_END_DATE) >= t1.obs_year
group by c1.cohort_definition_id,
t1.obs_year,
p1.gender_concept_id,
floor((t1.obs_year - p1.year_of_birth)/10)
;

TRUNCATE TABLE #temp_dates_3;
DROP TABLE #temp_dates_3;