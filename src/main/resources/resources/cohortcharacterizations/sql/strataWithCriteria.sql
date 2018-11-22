WITH qualified_events AS (
    SELECT ROW_NUMBER() OVER (partition by E.subject_id order by E.cohort_start_date) AS event_id, E.subject_id AS person_id, E.cohort_start_date AS start_date, E.cohort_end_date AS end_date, OP.observation_period_start_date AS op_start_date, OP.observation_period_end_date AS op_end_date
    FROM @temp_database_schema.@targetTable E
      JOIN @cdm_database_schema.observation_period OP ON E.subject_id = OP.person_id AND E.cohort_start_date >= OP.observation_period_start_date AND E.cohort_start_date <= OP.observation_period_end_date
    WHERE cohort_definition_id = @cohortId
)
INSERT INTO @strataCohortTable(cohort_definition_id, strata_id, subject_id, cohort_start_date, cohort_end_date)
SELECT @cohortId AS cohort_definition_id, @strataId AS strata_id, q.person_id as subject_id, q.start_date AS cohort_start_date, q.end_date AS cohort_end_date
  FROM qualified_events q
  JOIN (SELECT person_id FROM (@strataQuery) st GROUP BY person_id) sti ON sti.person_id = q.person_id
;
