SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (@eventQuery) Q
JOIN @cdm_database_schema.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
