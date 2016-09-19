select row_number() over (order by person_id, start_date) as event_id, person_id, start_date, end_date, op_start_date, op_end_date
INTO #primary_events
FROM
(
  select c.subject_id as person_id, c.cohort_start_date as start_date, c.cohort_end_date as end_date, 
    op.observation_period_start_date as op_start_date, op.observation_period_end_date as op_end_date
  FROM @cohortTable C
  JOIN @cdm_database_schema.observation_period OP on C.subject_id = OP.person_id and C.cohort_start_date between OP.observation_period_start_date and OP.observation_period_end_date
  WHERE cohort_definition_id = @indexCohortId
) P
;
