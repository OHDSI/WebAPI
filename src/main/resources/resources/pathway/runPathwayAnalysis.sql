DROP TABLE #raw_events;

/*
* Filter out events which do not fall into a person's target period
*/
WITH event_cohorts AS (
  @event_cohort_id_index_map
)
select id, event_cohort_id, event_cohort_index, subject_id, cohort_start_date, cohort_end_date
INTO #raw_events
FROM (
	SELECT ROW_NUMBER() OVER (ORDER BY e.cohort_start_date) as id,
	  ec.id event_cohort_id,
	  ec.cohort_index event_cohort_index,
	  e.subject_id,
	  e.cohort_start_date,
	  e.cohort_end_date
	FROM @target_database_schema.@target_cohort_table e
	  JOIN event_cohorts ec ON e.cohort_definition_id = ec.cohort_definition_id
	  JOIN @target_database_schema.@target_cohort_table t ON t.cohort_start_date <= e.cohort_start_date AND e.cohort_end_date <= t.cohort_end_date AND t.subject_id = e.subject_id
	WHERE t.cohort_definition_id IN (@pathway_target_cohort_id_list)
) RE;

-- SELECT * FROM #raw_events;

-- DROP TABLE #date_replacements;

-- Find closely located dates, which need to be collapsed, based on collapse_window
-- cknoll1: replaced date subtraction with datediff()
WITH person_dates AS (
  SELECT subject_id, cohort_start_date cohort_date FROM #raw_events
  UNION
  SELECT subject_id, cohort_end_date cohort_date FROM #raw_events
),
marked_dates AS (
  SELECT ROW_NUMBER() OVER (ORDER BY subject_id ASC, cohort_date ASC) ordinal,
    subject_id,
    cohort_date,
    CASE WHEN (datediff(d,LAG(cohort_date) OVER (ORDER BY subject_id ASC, cohort_date ASC), cohort_date) < 3 AND subject_id = LAG(subject_id) OVER (ORDER BY subject_id ASC, cohort_date ASC)) THEN 1 ELSE 0 END to_be_collapsed
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

-- SELECT * FROM #date_replacements;

-- DROP TABLE #collapsed_dates_events;

-- Collapse dates
SELECT
  event.id,
  event.event_cohort_id,
  event.event_cohort_index,
  event.subject_id,
  COALESCE(start_dr.replacement_date, event.cohort_start_date) cohort_start_date,
  COALESCE(end_dr.replacement_date, event.cohort_end_date) cohort_end_date
INTO #collapsed_dates_events
FROM #raw_events event
  LEFT JOIN #date_replacements start_dr ON start_dr.subject_id = event.subject_id AND start_dr.cohort_date = event.cohort_start_date
  LEFT JOIN #date_replacements end_dr ON end_dr.subject_id = event.subject_id AND end_dr.cohort_date = event.cohort_end_date
ORDER BY event.cohort_start_date, event.cohort_end_date;

SELECT subject_id, event_cohort_id, event_cohort_index, cohort_start_date, cohort_end_date
FROM #collapsed_dates_events
ORDER BY subject_id, cohort_start_date, cohort_end_date;

DROP TABLE #split_overlapping_events;

-- Split partially overlapping events into a set of events which either do not overlap or fully overlap (for later GROUP BY start_date, end_date)
--
-- e.g.
--    |A------|
--        |B-----|
-- into
--
--    |A--|A--|
--        |B--|B--|

SELECT
	CASE WHEN ordinal < 3 THEN first.id ELSE second.id END as id,
	CASE WHEN ordinal < 3 THEN first.event_cohort_id ELSE second.event_cohort_id END event_cohort_id,
	CASE WHEN ordinal < 3 THEN first.event_cohort_id ELSE second.event_cohort_id END event_cohort_index,
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
WITH events AS (
  SELECT *
  FROM #collapsed_dates_events cde
  WHERE NOT EXISTS(SELECT id FROM #split_overlapping_events WHERE #split_overlapping_events.id = cde.id)

  UNION ALL

  SELECT *
  FROM #split_overlapping_events
)
SELECT subject_id, event_cohort_id, cohort_start_date, cohort_end_date
FROM events
ORDER BY subject_id, cohort_start_date, cohort_end_date;
*/

-- Group fully overlapping events into combinations (e.g. two separate events A and B with same start and end dates -> single A+B event)
-- cknoll1: we'll use bitwise addition with SUM() to combine events instead of LIST_AGG(), replacing 'name' column with 'combo_id'

-- DROP TABLE #combo_events;

WITH events AS (
  SELECT *
  FROM #collapsed_dates_events cde
  WHERE NOT EXISTS(SELECT id FROM #split_overlapping_events WHERE #split_overlapping_events.id = cde.id)

  UNION ALL

  SELECT *
  FROM #split_overlapping_events
)
SELECT SUM(POWER(2,ec.cohort_index)) as combo_id, subject_id, cohort_start_date, cohort_end_date
INTO #combo_events
FROM events e
JOIN #event_cohorts ec ON ec.id = e.event_cohort_id
GROUP BY subject_id, cohort_start_date, cohort_end_date;


/*
SELECT subject_id, combo_id, cohort_start_date, cohort_end_date
FROM #combo_events
ORDER BY subject_id, cohort_start_date, cohort_end_date;
*/

-- Remove repetetive events (e.g. A-A-A into A)
-- cknoll1: from here forward: combo_id is replacing 'name'
WITH marked_repetitive_events AS (
  SELECT
    *,
    CASE WHEN (combo_id = LAG(combo_id) OVER (PARTITION BY subject_id ORDER BY subject_id, cohort_start_date ASC))
      THEN 1
      ELSE 0
    END repetitive_event
  FROM #combo_events
)
SELECT
  combo_id,
  subject_id,
  cohort_start_date,
  cohort_end_date
INTO #non_repetetive_combo_events
FROM marked_repetitive_events
WHERE repetitive_event = 0;

-- From here we proceed as normal, but creating a chain of combo_ids instead of 'name'
-- using whatever

-- Now, Some magic:  give me the cohort names that belong in each combo event:

select * from #event_cohorts;

select nre.combo_id, nre.subject_id, nre.cohort_start_date, ec.cohort_name
FROM  #non_repetetive_combo_events nre
JOIN #event_cohorts ec on nre.combo_id & POWER(2,ec.cohort_index) > 0
ORDER BY subject_id, cohort_start_date;

-- This would be left to the client to parse out the treatment pathway into the names of the members of the combo:
select * from #non_repetetive_combo_events ORDER BY subject_id, cohort_start_date;

-- 2->1->6->1->3->2
-- In javascript, if you have an array of event cohorts with form [ {name, cohort_index) ...] you can get the cohorts in the combo_id like:
-- cohort_events.filter((item) -> pow(2,item.cohort_index) & combo_id > 0); // returns cohorts which match the combo_id