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

select id, event_cohort_index, subject_id, cohort_start_date, cohort_end_date
INTO #raw_events
FROM (
	SELECT ROW_NUMBER() OVER (ORDER BY e.cohort_start_date) AS id,
	  ec.cohort_index AS event_cohort_index,
	  e.subject_id,
	  e.cohort_start_date,
	  e.cohort_end_date
	FROM @target_database_schema.@target_cohort_table e
	  JOIN ( @event_cohort_id_index_map ) ec ON e.cohort_definition_id = ec.cohort_definition_id
	  JOIN @target_database_schema.@target_cohort_table t ON t.cohort_start_date <= e.cohort_start_date AND e.cohort_end_date <= t.cohort_end_date AND t.subject_id = e.subject_id
	WHERE t.cohort_definition_id = @pathway_target_cohort_id
) RE;

/*
* Find closely located dates, which need to be collapsed, based on collapse_window
*/

IF OBJECT_ID('tempdb..#date_replacements', 'U') IS NOT NULL
DROP TABLE #date_replacements;

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
  SELECT *, ordinal - SUM(to_be_collapsed) OVER ( PARTITION BY subject_id ORDER BY cohort_date ASC ROWS UNBOUNDED PRECEDING) group_idx
  FROM marked_dates
),
replacements AS (
  SELECT orig.subject_id, orig.cohort_date, FIRST_VALUE(cohort_date) OVER (PARTITION BY group_idx ORDER BY ordinal ASC ROWS UNBOUNDED PRECEDING) as replacement_date
  FROM grouped_dates orig
)
SELECT *
INTO #date_replacements
FROM replacements
WHERE cohort_date <> replacement_date;

/*
* Collapse dates
*/

IF OBJECT_ID('tempdb..#collapsed_dates_events', 'U') IS NOT NULL
DROP TABLE #collapsed_dates_events;

SELECT
  event.id,
  event.event_cohort_index,
  event.subject_id,
  COALESCE(start_dr.replacement_date, event.cohort_start_date) cohort_start_date,
  COALESCE(end_dr.replacement_date, event.cohort_end_date) cohort_end_date
INTO #collapsed_dates_events
FROM #raw_events event
  LEFT JOIN #date_replacements start_dr ON start_dr.subject_id = event.subject_id AND start_dr.cohort_date = event.cohort_start_date
  LEFT JOIN #date_replacements end_dr ON end_dr.subject_id = event.subject_id AND end_dr.cohort_date = event.cohort_end_date
;

/*
Split partially overlapping events into a set of events which either do not overlap or fully overlap (for later GROUP BY start_date, end_date)

e.g.
  |A------|
      |B-----|
into

  |A--|A--|
      |B--|B--|
*/

IF OBJECT_ID('tempdb..#split_overlapping_events', 'U') IS NOT NULL
DROP TABLE #split_overlapping_events;

SELECT
	CASE WHEN ordinal < 3 THEN first.id ELSE second.id END as id,
	CASE WHEN ordinal < 3 THEN first.event_cohort_index ELSE second.event_cohort_index END event_cohort_index,
	CASE WHEN ordinal < 3 THEN first.subject_id ELSE second.subject_id END subject_id,

	CASE ordinal
		WHEN 1 THEN
		first.cohort_start_date
		WHEN 2 THEN
		second.cohort_start_date
		WHEN 3 THEN
		second.cohort_start_date
		WHEN 4 THEN
		first.cohort_end_date
		END as cohort_start_date,

	CASE ordinal
		WHEN 1 THEN
		second.cohort_start_date
		WHEN 2 THEN
		first.cohort_end_date
		WHEN 3 THEN
		first.cohort_end_date
		WHEN 4 THEN
		second.cohort_end_date
		END as cohort_end_date
INTO #split_overlapping_events
FROM #collapsed_dates_events first
JOIN #collapsed_dates_events second ON first.subject_id = second.subject_id
    AND first.cohort_start_date < second.cohort_start_date
    AND first.cohort_end_date < second.cohort_end_date
    AND first.cohort_end_date > second.cohort_start_date
CROSS JOIN (SELECT 1 ordinal UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) multiplier;

/*
* Group fully overlapping events into combinations (e.g. two separate events A and B with same start and end dates -> single A+B event)
* We'll use bitwise addition with SUM() to combine events instead of LIST_AGG(), replacing 'name' column with 'combo_id'.
*/

IF OBJECT_ID('tempdb..#combo_events', 'U') IS NOT NULL
DROP TABLE #combo_events;

WITH events AS (
  SELECT *
  FROM #collapsed_dates_events cde
  WHERE NOT EXISTS(SELECT id FROM #split_overlapping_events WHERE #split_overlapping_events.id = cde.id)

  UNION ALL

  SELECT *
  FROM #split_overlapping_events
)
SELECT SUM(DISTINCT POWER(2, e.event_cohort_index)) as combo_id, subject_id, cohort_start_date, cohort_end_date
INTO #combo_events
FROM events e
GROUP BY subject_id, cohort_start_date, cohort_end_date;

/*
* Remove repetitive events (e.g. A-A-A into A)
*/

IF OBJECT_ID('tempdb..#non_repetetive_events', 'U') IS NOT NULL
DROP TABLE #non_repetetive_events;

SELECT
  ROW_NUMBER() OVER (PARTITION BY subject_id ORDER BY cohort_start_date) ordinal,
  combo_id,
  subject_id,
  cohort_start_date,
  cohort_end_date
INTO #non_repetetive_events
FROM (
  SELECT
    *,
    CASE WHEN (combo_id = LAG(combo_id) OVER (PARTITION BY subject_id ORDER BY subject_id, cohort_start_date ASC))
      THEN 1
      ELSE 0
    END repetitive_event, 
		case when ROW_NUMBER() OVER (PARTITION BY subject_id, combo_id ORDER BY cohort_start_date) > 1 then 1 else 0 end is_repeat
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
FROM #non_repetetive_events
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