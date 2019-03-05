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

select id, event_cohort_index, subject_id, CAST(cohort_start_date AS DATETIME) AS cohort_start_date, CAST(cohort_end_date AS DATETIME) AS cohort_end_date
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
  SELECT ordinal, subject_id, cohort_date, to_be_collapsed, ordinal - SUM(to_be_collapsed) OVER ( PARTITION BY subject_id ORDER BY cohort_date ASC ROWS UNBOUNDED PRECEDING) group_idx
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

IF OBJECT_ID('tempdb..#split_overlay_events', 'U') IS NOT NULL
DROP TABLE #split_overlay_events;

SELECT soe.id, soe.event_cohort_index, soe.subject_id, soe.cohort_start_date, soe.cohort_end_date
INTO #split_overlay_events
FROM (
  SELECT
    CASE WHEN ordinal < 3 THEN f.id ELSE s.id END as id,
    CASE WHEN ordinal < 3 THEN f.event_cohort_index ELSE s.event_cohort_index END event_cohort_index,
    CASE WHEN ordinal < 3 THEN f.subject_id ELSE s.subject_id END subject_id,

    CASE ordinal
      WHEN 1 THEN
      f.cohort_start_date
      WHEN 2 THEN
      s.cohort_start_date
      WHEN 3 THEN
      s.cohort_start_date
      WHEN 4 THEN
      f.cohort_end_date
      END as cohort_start_date,

    CASE ordinal
      WHEN 1 THEN
      s.cohort_start_date
      WHEN 2 THEN
      f.cohort_end_date
      WHEN 3 THEN
      f.cohort_end_date
      WHEN 4 THEN
      s.cohort_end_date
      END as cohort_end_date
  FROM #collapsed_dates_events f
  JOIN #collapsed_dates_events s ON f.subject_id = s.subject_id
      AND f.cohort_start_date < s.cohort_start_date
      AND f.cohort_end_date < s.cohort_end_date
      AND f.cohort_end_date > s.cohort_start_date
  CROSS JOIN (SELECT 1 ordinal UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) multiplier
) soe;

/*
* Group fully overlapping events into combinations (e.g. two separate events A and B with same start and end dates -> single A+B event)
* We'll use bitwise addition with SUM() to combine events instead of LIST_AGG(), replacing 'name' column with 'combo_id'.
*/

IF OBJECT_ID('tempdb..#combo_events', 'U') IS NOT NULL
DROP TABLE #combo_events;

WITH events AS (
  SELECT *
  FROM #collapsed_dates_events cde
  WHERE NOT EXISTS(SELECT id FROM #split_overlay_events soe WHERE soe.id = cde.id)

  UNION ALL

  SELECT *
  FROM #split_overlay_events
)
SELECT CAST(SUM(DISTINCT POWER(2, e.event_cohort_index)) as INT) as combo_id, subject_id, cohort_start_date, cohort_end_date
INTO #combo_events
FROM events e
GROUP BY subject_id, cohort_start_date, cohort_end_date;

/*
* Remove repetitive events (e.g. A-A-A into A)
*/

IF OBJECT_ID('tempdb..#non_repetitive_events', 'U') IS NOT NULL
DROP TABLE #non_repetitive_events;

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
FROM #non_repetitive_events
WHERE 1 = 1 {@max_depth != ''}?{ AND ordinal <= @max_depth };

INSERT INTO @target_database_schema.pathway_analysis_stats (pathway_analysis_generation_id, target_cohort_id, target_cohort_count, pathways_count)
SELECT
  @generation_id as pathway_analysis_generation_id,
  @pathway_target_cohort_id as target_cohort_id,
  target_count.cnt AS target_cohort_count,
  pathway_count.cnt AS pathways_count
FROM (
  SELECT CAST(COUNT(*) AS INT) cnt
  FROM @target_cohort_table
  WHERE cohort_definition_id = @pathway_target_cohort_id
) target_count,
(
  SELECT CAST(COUNT(DISTINCT subject_id) AS INT) cnt
  FROM @target_database_schema.pathway_analysis_events
  WHERE pathway_analysis_generation_id = @generation_id
  AND target_cohort_id = @pathway_target_cohort_id
) pathway_count;