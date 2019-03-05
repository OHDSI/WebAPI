DELETE
FROM @target_database_schema.pathway_analysis_events
WHERE pathway_analysis_generation_id = @generation_id AND target_cohort_id = @pathway_target_cohort_id;

/*
* Filter out events which do not fall into a person's target period
* e.g. of event_cohorts:
* SELECT 1 AS cohort_definition_id, 1 AS cohort_index UNION ALL ...
*/

IF OBJECT_ID('tempdb..#raw_events', 'U') IS NOT NULL
DROP TABLE #raw_events;

SELECT id, event_cohort_index, subject_id, cohort_start_date, cohort_end_date
INTO #raw_events
FROM (
	SELECT ROW_NUMBER() OVER (ORDER BY e.cohort_start_date) AS id,
	  ec.cohort_index AS event_cohort_index,
	  e.subject_id,
	  e.cohort_start_date,
	  e.cohort_end_date
	FROM @target_cohort_table e
	  JOIN ( @event_cohort_id_index_map ) ec ON e.cohort_definition_id = ec.cohort_definition_id
	  JOIN @target_cohort_table t ON t.cohort_start_date <= e.cohort_start_date AND e.cohort_end_date <= t.cohort_end_date AND t.subject_id = e.subject_id
	WHERE t.cohort_definition_id = @pathway_target_cohort_id
) RE;

/*
Split partially overlapping events into a set of events which either do not overlap or fully overlap (for later GROUP BY start_date, end_date)

e.g.
  |A------|
      |B-----|
into

  |A--|A--|
      |B--|B--|
*/

IF OBJECT_ID('tempdb..#combo_events', 'U') IS NOT NULL
DROP TABLE #combo_events;

WITH 
cohort_dates AS (
	SELECT DISTINCT subject_id, cohort_date 
	FROM (
		  SELECT subject_id, cohort_start_date cohort_date FROM #raw_events 
		  UNION 
		  SELECT subject_id, DATEADD(day, 1, cohort_end_date) cohort_date FROM #raw_events
		  ) all_dates
	ORDER BY subject_id, cohort_date
),
time_periods AS (
	SELECT subject_id, cohort_date, DATEADD(day, -1, LEAD(cohort_date,1) over (PARTITION BY subject_id ORDER BY cohort_date ASC)) next_cohort_date
	FROM cohort_dates 
	GROUP BY subject_id, cohort_date
),
events AS (
	SELECT tp.subject_id, event_cohort_index, cohort_date cohort_start_date, next_cohort_date cohort_end_date  
	FROM time_periods tp
	LEFT JOIN #raw_events e ON e.subject_id = tp.subject_id
	WHERE (e.cohort_start_date <= tp.cohort_date AND e.cohort_end_date >= tp.next_cohort_date)
) 

SELECT SUM(POWER(2, e.event_cohort_index)) as combo_id,  subject_id , cohort_start_date, cohort_end_date
INTO #combo_events
FROM events e
GROUP BY subject_id, cohort_start_date, cohort_end_date;

/*
* Remove short time periods
*/

IF OBJECT_ID('tempdb..#combo_events_filtered', 'U') IS NOT NULL
DROP TABLE #combo_events_filtered;

SELECT *
INTO #combo_events_filtered
FROM #combo_events e
WHERE DATEDIFF (day , e.cohort_start_date, e.cohort_end_date) >= @combo_window;


/*
* Remove repetitive events (e.g. A-A-A into A)
*/

IF OBJECT_ID('tempdb..#non_repetitive_events', 'U') IS NOT NULL
DROP TABLE #non_repetitive_events;

SELECT
  ROW_NUMBER() OVER (PARTITION BY subject_id ORDER BY cohort_start_date) ordinal,
  combo_id,
  subject_id,
  cohort_start_date,
  cohort_end_date
INTO #non_repetitive_events
FROM (
  SELECT
    *,
    CASE WHEN (combo_id = LAG(combo_id) OVER (PARTITION BY subject_id ORDER BY subject_id, cohort_start_date ASC))
      THEN 1
      ELSE 0
    END repetitive_event, 
		case when ROW_NUMBER() OVER (PARTITION BY subject_id, combo_id ORDER BY cohort_start_date) > 1 then 1 else 0 end is_repeat
  FROM #combo_events_filtered
) AS marked_repetitive_events
WHERE repetitive_event = 0 {@allow_repeats == 'false'}?{ AND is_repeat = 0 };

/*
* Persist results
*/

INSERT INTO @target_database_schema.pathway_analysis_events (pathway_analysis_generation_id, target_cohort_id, subject_id, ordinal, combo_id, cohort_start_date, cohort_end_date)
SELECT
  @generation_id as pathway_analysis_generation_id,
  @pathway_target_cohort_id as target_cohort_id,
  subject_id,
	ordinal,
  combo_id,
  cohort_start_date,
  cohort_end_date
FROM #non_repetitive_events
WHERE 1 = 1 {@max_depth != ''}?{ AND ordinal <= @max_depth };

INSERT INTO @target_database_schema.pathway_analysis_stats (pathway_analysis_generation_id, target_cohort_id, target_cohort_count, pathways_count)
SELECT
  @generation_id as pathway_analysis_generation_id,
  @pathway_target_cohort_id as target_cohort_id,
  target_count.cnt AS target_cohort_count,
  pathway_count.cnt AS pathways_count
FROM (
  SELECT COUNT(*) cnt
  FROM @target_database_schema.@target_cohort_table
  WHERE cohort_definition_id = @pathway_target_cohort_id
) target_count,
(
  SELECT COUNT(DISTINCT subject_id) cnt
  FROM @target_database_schema.pathway_analysis_events
  WHERE pathway_analysis_generation_id = @generation_id
  AND target_cohort_id = @pathway_target_cohort_id
) pathway_count;