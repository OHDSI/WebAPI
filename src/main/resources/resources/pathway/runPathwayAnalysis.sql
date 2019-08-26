DELETE
FROM @target_database_schema.pathway_analysis_events
WHERE pathway_analysis_generation_id = @generation_id AND target_cohort_id = @pathway_target_cohort_id;

/*
* Filter out events which do not fall into a person's target period
* e.g. of event_cohorts:
* SELECT 1 AS cohort_definition_id, 1 AS cohort_index UNION ALL ...
*/

select id, event_cohort_index, subject_id, CAST(cohort_start_date AS DATETIME) AS cohort_start_date, CAST(cohort_end_date AS DATETIME) AS cohort_end_date
INTO #raw_events
FROM (
	SELECT ROW_NUMBER() OVER (ORDER BY e.cohort_start_date) AS id,
	  ec.cohort_index AS event_cohort_index,
	  e.subject_id,
	  e.cohort_start_date,
	  dateadd(d, 1, e.cohort_end_date) as cohort_end_date
	FROM @target_cohort_table e
	  JOIN ( @event_cohort_id_index_map ) ec ON e.cohort_definition_id = ec.cohort_definition_id
	  JOIN @target_cohort_table t ON t.cohort_start_date <= e.cohort_start_date AND e.cohort_start_date <= t.cohort_end_date AND t.subject_id = e.subject_id
	WHERE t.cohort_definition_id = @pathway_target_cohort_id
) RE;

/*
* Find closely located dates, which need to be collapsed, based on collapse_window
*/

WITH person_dates AS (
  SELECT subject_id, cohort_start_date cohort_date FROM #raw_events
  UNION
  SELECT subject_id, cohort_end_date cohort_date FROM #raw_events
),
marked_dates AS (
  SELECT ROW_NUMBER() OVER (ORDER BY subject_id ASC, cohort_date ASC) ordinal,
    subject_id,
    cohort_date,
    CASE WHEN (datediff(d,LAG(cohort_date) OVER (ORDER BY subject_id ASC, cohort_date ASC), cohort_date) < @combo_window AND subject_id = LAG(subject_id) OVER (ORDER BY subject_id ASC, cohort_date ASC)) THEN 1 ELSE 0 END to_be_collapsed
  FROM person_dates
),
grouped_dates AS (
  SELECT ordinal, subject_id, cohort_date, to_be_collapsed, ordinal - SUM(to_be_collapsed) OVER ( PARTITION BY subject_id ORDER BY cohort_date ASC ROWS UNBOUNDED PRECEDING) group_idx
  FROM marked_dates
),
replacements AS (
  SELECT orig.subject_id, orig.cohort_date, FIRST_VALUE(cohort_date) OVER (PARTITION BY group_idx ORDER BY ordinal ASC ROWS UNBOUNDED PRECEDING) as replacement_date
  FROM grouped_dates orig
)
SELECT subject_id, cohort_date, replacement_date
INTO #date_replacements
FROM replacements
WHERE cohort_date <> replacement_date;

/*
* Collapse dates
*/

SELECT
  e.id,
  e.event_cohort_index,
  e.subject_id,
  e.cohort_start_date,
  case
  /*
  The collapsed dates (or the raw event cohort dates) may have intervals where start == end, so these should be expanded to cover a minimum of 1 day
  */
    when e.cohort_start_date = e.cohort_end_date then CAST(dateadd(d,1,e.cohort_end_date) AS DATETIME) /* cast is required for BigQuery */
    else e.cohort_end_date
  end cohort_end_date
FROM
  (SELECT
    event.id,
    event.event_cohort_index,
    event.subject_id,
    COALESCE(start_dr.replacement_date, event.cohort_start_date) cohort_start_date,
    COALESCE(end_dr.replacement_date, event.cohort_end_date) cohort_end_date
FROM #raw_events event
  LEFT JOIN #date_replacements start_dr ON start_dr.subject_id = event.subject_id AND start_dr.cohort_date = event.cohort_start_date
  LEFT JOIN #date_replacements end_dr ON end_dr.subject_id = event.subject_id AND end_dr.cohort_date = event.cohort_end_date) e
INTO #collapsed_dates_events
;

/*
Split partially overlapping events into a set of events which either do not overlap or fully overlap (for later GROUP BY start_date, end_date)

e.g.
  |A------|
      |B-----|
into

  |A--|A--|
      |B--|B--|

or 
  |A--------------|
      |B-----|
into   
  |A--|A-----|A---|
      |B-----|
*/

WITH 
cohort_dates AS (
	SELECT DISTINCT subject_id, cohort_date 
	FROM (
		  SELECT subject_id, cohort_start_date cohort_date FROM #collapsed_dates_events 
		  UNION 
		  SELECT subject_id,cohort_end_date cohort_date FROM #collapsed_dates_events
		  ) all_dates
),
time_periods AS (
	SELECT subject_id, cohort_date, LEAD(cohort_date,1) over (PARTITION BY subject_id ORDER BY cohort_date ASC) next_cohort_date
	FROM cohort_dates 
	GROUP BY subject_id, cohort_date

),
events AS (
	SELECT tp.subject_id, event_cohort_index, cohort_date cohort_start_date, next_cohort_date cohort_end_date  
	FROM time_periods tp
	LEFT JOIN #collapsed_dates_events e ON e.subject_id = tp.subject_id
	WHERE (e.cohort_start_date <= tp.cohort_date AND e.cohort_end_date >= tp.next_cohort_date)
) 
SELECT SUM(POWER(2, e.event_cohort_index)) as combo_id,  subject_id , cohort_start_date, cohort_end_date
into #combo_events
FROM events e
GROUP BY subject_id, cohort_start_date, cohort_end_date;

/*
* Remove repetitive events (e.g. A-A-A into A)
*/

SELECT
  CAST(ROW_NUMBER() OVER (PARTITION BY subject_id ORDER BY cohort_start_date) AS INT) ordinal,
  CAST(combo_id AS BIGINT) combo_id,
  subject_id,
  cohort_start_date,
  cohort_end_date
INTO #non_repetitive_events
FROM (
  SELECT
    combo_id, subject_id, cohort_start_date, cohort_end_date,
    CASE WHEN (combo_id = LAG(combo_id) OVER (PARTITION BY subject_id ORDER BY subject_id, cohort_start_date ASC))
      THEN 1
      ELSE 0
    END repetitive_event, 
		case when ROW_NUMBER() OVER (PARTITION BY subject_id, CAST(combo_id AS INT) ORDER BY cohort_start_date) > 1 then 1 else 0 end is_repeat
  FROM #combo_events
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
  CAST(@pathway_target_cohort_id AS INT) AS target_cohort_id,
  CAST(target_count.cnt AS BIGINT) AS target_cohort_count,
  CAST(pathway_count.cnt AS BIGINT) AS pathways_count
FROM (
  SELECT CAST(COUNT_BIG(*) as BIGINT) cnt
  FROM @target_cohort_table
  WHERE cohort_definition_id = @pathway_target_cohort_id
) target_count,
(
  SELECT CAST(COUNT_BIG(DISTINCT subject_id) as BIGINT) cnt
  FROM @target_database_schema.pathway_analysis_events
  WHERE pathway_analysis_generation_id = @generation_id
  AND target_cohort_id = @pathway_target_cohort_id
) pathway_count;

TRUNCATE TABLE #non_repetitive_events;
DROP TABLE #non_repetitive_events;

TRUNCATE TABLE #combo_events;
DROP TABLE #combo_events;

TRUNCATE TABLE #collapsed_dates_events;
DROP TABLE #collapsed_dates_events;

TRUNCATE TABLE #date_replacements;
DROP TABLE #date_replacements;

TRUNCATE TABLE #raw_events;
DROP TABLE #raw_events;

{@isHive == 'true'}?{DROP TABLE #person_dates; DROP TABLE #marked_dates; DROP TABLE #grouped_dates; DROP TABLE #replacements;
DROP TABLE #cohort_dates; DROP TABLE #time_periods; DROP TABLE #events;}