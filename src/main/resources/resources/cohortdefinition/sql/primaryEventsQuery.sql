create table #PrimaryCriteriaEvents
(
	person_id bigint,
	start_date datetime,
	end_date datetime,
	op_start_date datetime,
	op_end_date datetime
)
;

INSERT INTO #PrimaryCriteriaEvents (person_id, start_date, end_date, op_start_date, op_end_date)
select P.person_id, P.start_date, P.end_date, OP.observation_period_start_date, OP.observation_period_end_date
FROM
(
  select P.person_id, P.start_date, P.end_date, ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY start_date @EventSort) ordinal
  FROM 
  (
  @criteriaQueries
  ) P
) P
JOIN @CDM_schema.observation_period OP on P.person_id = OP.person_id and P.start_date between OP.observation_period_start_date and op.observation_period_end_date
WHERE @primaryEventsFilter
;
