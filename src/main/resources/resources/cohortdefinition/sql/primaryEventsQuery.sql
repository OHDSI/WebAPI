create table #PrimaryCriteriaEvents
(
	PERSON_ID bigint,
	START_DATE datetime,
	END_DATE datetime,
	OP_START_DATE datetime,
	OP_END_DATE datetime
)
;

INSERT INTO #PrimaryCriteriaEvents (PERSON_ID, START_DATE, END_DATE, OP_START_DATE, OP_END_DATE)
select P.PERSON_ID, P.START_DATE, P.END_DATE, OP.observation_period_start_date, OP.observation_period_end_date
FROM
(
  @criteriaQueries
) P
JOIN @CDM_schema.observation_period OP on P.PERSON_ID = OP.PERSON_ID and P.observation_period_id = op.observation_period_id
WHERE @observationFilter
;
