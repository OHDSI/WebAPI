delete from @results_schema.heracles_results where cohort_definition_id IN (@cohort_definition_id) and analysis_id IN (@list_of_analysis_ids);
delete from @results_schema.heracles_results_dist where cohort_definition_id IN (@cohort_definition_id) and analysis_id IN (@list_of_analysis_ids);

--7. generate results for analysis_results

select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date
into #HERACLES_cohort
from @results_schema.cohort
where cohort_definition_id in (@cohort_definition_id)
;
create index ix_cohort_subject on #HERACLES_cohort (subject_id, cohort_start_date);

select distinct subject_id, cohort_definition_id
into #HERACLES_cohort_subject
from #HERACLES_cohort
;
create index ix_cohort_subject_subject on #HERACLES_cohort_subject (subject_id, cohort_definition_id);

select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date
into #cohort_first
from (
select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date, row_number() over (partition by cohort_definition_id, subject_id order by cohort_start_date) as rn
FROM #HERACLES_cohort
) F
where F.rn = 1
;

create index ix_cohort_first_subject on #cohort_first (subject_id, cohort_start_date);

SELECT hp.period_id, hp.period_start_date, hp.period_end_date
into #periods_baseline
FROM @results_schema.heracles_periods hp
WHERE not (
hp.period_end_date <= (SELECT dateadd(d, -365, min(cohort_start_date)) FROM #HERACLES_cohort) or hp.period_start_date > (SELECT max(cohort_start_date) FROM #HERACLES_cohort)
) AND hp.period_type in (@periods); -- only returns overlapping periods

create index ix_periods_baseline_start on #periods_baseline (period_start_date);
create index ix_periods_baseline_end on #periods_baseline (period_end_date);

SELECT hp.period_id, hp.period_start_date, hp.period_end_date
into #periods_atrisk
FROM @results_schema.heracles_periods hp
WHERE not (
hp.period_end_date <= (SELECT min(cohort_start_date) FROM #HERACLES_cohort) or hp.period_start_date > (SELECT max(cohort_end_date) FROM #HERACLES_cohort)
) AND hp.period_type in (@periods); -- only returns overlapping periods

create index ix_periods_atrisk_start on #periods_atrisk (period_start_date);
create index ix_periods_atrisk_end on #periods_atrisk (period_end_date);

--{109 IN (@list_of_analysis_ids) | 110 IN (@list_of_analysis_ids) | 116 IN (@list_of_analysis_ids) | 117 IN (@list_of_analysis_ids)}?{

IF OBJECT_ID('tempdb..#tmp_years', 'U') IS NOT NULL
DROP TABLE  #tmp_years;

IF OBJECT_ID('tempdb..#tmp_months', 'U') IS NOT NULL
DROP TABLE  #tmp_months;

WITH x AS (SELECT 0 AS n UNION ALL SELECT 1 AS n UNION ALL SELECT 2 AS n UNION ALL SELECT 3 AS n UNION ALL SELECT 4 AS n UNION ALL SELECT 5 AS n UNION ALL SELECT 6 AS n UNION ALL SELECT 7 AS n UNION ALL SELECT 8 AS n UNION ALL SELECT 9 AS n),
    years AS (SELECT ones.n + 10*tens.n + 100*hundreds.n + 1000*thousands.n as year
              FROM x ones,     x tens,      x hundreds,       x thousands)
SELECT year
INTO #tmp_years FROM years WHERE year BETWEEN 1900 AND 2201;

WITH months AS (SELECT 1 AS month UNION ALL SELECT 2 AS month UNION ALL SELECT 3 AS month UNION ALL SELECT 4 AS month UNION ALL SELECT 5 AS month UNION ALL SELECT 6 AS month UNION ALL SELECT 7 AS month UNION ALL SELECT 8 AS month UNION ALL SELECT 9 AS month UNION ALL SELECT 10 AS month UNION ALL SELECT 11 AS month UNION ALL SELECT 12 AS month)
SELECT month
INTO #tmp_months FROM months;

--}