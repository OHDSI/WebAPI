	select stratum_1 as CONCEPT_ID, max(count_value) as NUM_RECORDS
  from @OHDSI_schema.ACHILLES_results
  where analysis_id in (2, 4, 5, 201, 301, 401, 501, 505, 601, 701, 801, 901, 1001,1201)
	and stratum_1 in (@conceptIdentifiers)
  and stratum_1 <> '0'
  group by stratum_1
  union
  select stratum_2 as CONCEPT_ID, sum(count_value) as NUM_RECORDS
  from @OHDSI_schema.ACHILLES_results
  where analysis_id in (405, 605, 705, 805, 807)
	and stratum_2 in (@conceptIdentifiers)
  and stratum_2 <> '0'
  group by stratum_2

