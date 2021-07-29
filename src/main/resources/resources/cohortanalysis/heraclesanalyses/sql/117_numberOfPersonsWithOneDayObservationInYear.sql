-- 117   Number of persons with at least one day of observation in each year by gender and age decile
-- Note: using temp table instead of nested query because this gives vastly improved performance in Oracle

WITH op_date_range AS (
    select
      MIN(observation_period_start_date) AS op_start_date,
      MAX(observation_period_end_date) AS op_end_date
    FROM @CDM_schema.person p1
      inner join #HERACLES_cohort_subject c1
                  on p1.person_id = c1.subject_id
    inner join
    @CDM_schema.observation_period op1
    on p1.person_id = op1.person_id)
SELECT years.year * 100 + months.month AS obs_month
INTO
#temp_dates_4
FROM #tmp_years years, #tmp_months months, op_date_range
WHERE years.year BETWEEN YEAR(op_date_range.op_start_date) AND YEAR(op_date_range.op_end_date)
;

--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  117 as analysis_id,
  t1.obs_month as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct op1.PERSON_ID) as count_value
into #results_117
FROM @CDM_schema.person p1
inner join #HERACLES_cohort_subject c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.observation_period op1
on p1.person_id = op1.person_id,
#temp_dates_4 t1
where YEAR(observation_period_start_date)*100 + MONTH(observation_period_start_date) <= t1.obs_month
and YEAR(observation_period_end_date)*100 + MONTH(observation_period_end_date) >= t1.obs_month
group by c1.cohort_definition_id, t1.obs_month
;

TRUNCATE TABLE #temp_dates_4;
DROP TABLE #temp_dates_4;