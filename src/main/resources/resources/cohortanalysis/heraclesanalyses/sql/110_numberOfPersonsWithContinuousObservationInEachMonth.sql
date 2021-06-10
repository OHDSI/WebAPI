-- 110   Number of persons with continuous observation in each month
-- Note: using temp table instead of nested query because this gives vastly improved performance in Oracle

WITH op_date_range AS (
    SELECT MIN(observation_period_start_date) AS op_start_date,
           MAX(observation_period_end_date) AS op_end_date
    FROM @CDM_schema.person p1
      inner join #HERACLES_cohort_subject c1
                  on p1.person_id = c1.subject_id
    inner join
    @CDM_schema.observation_period op1
    on p1.person_id = op1.person_id
)
SELECT DISTINCT
  years.year*100 + months.month AS obs_month,
  DATEFROMPARTS(years.year, months.month, 1) AS obs_month_start,
  DATEADD(dd,-1,DATEADD(mm,1,DATEFROMPARTS(years.year, months.month, 1))) AS obs_month_end
INTO
#temp_dates_2
FROM #tmp_years years, #tmp_months months, op_date_range WHERE year BETWEEN YEAR(op_date_range.op_start_date) AND YEAR(op_date_range.op_end_date)
;


--INSERT INTO @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  110 AS analysis_id,
  obs_month AS stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(DISTINCT p1.person_id) AS count_value
into #results_110
FROM @CDM_schema.person p1
inner join #HERACLES_cohort_subject c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.observation_period op1
on p1.person_id = op1.person_id,
#temp_dates_2
WHERE
observation_period_start_date <= obs_month_start
AND
observation_period_end_date >= obs_month_end
GROUP BY
c1.cohort_definition_id, obs_month
;

TRUNCATE TABLE #temp_dates_2;
DROP TABLE #temp_dates_2;