SELECT
  stratum_1        AS CONCEPT_ID,
  max(count_value) AS NUM_RECORDS
FROM @OHDSI_schema.ACHILLES_results
WHERE analysis_id IN (2, 4, 5, 201, 301, 401, 501, 505, 601, 701, 801, 901, 1001, 1201)
AND stratum_1 IN (@conceptIdentifiers)
AND stratum_1 <> '0'
GROUP BY stratum_1
UNION
SELECT
  stratum_2        AS CONCEPT_ID,
  sum(count_value) AS NUM_RECORDS
FROM @OHDSI_schema.ACHILLES_results
WHERE analysis_id IN (405, 605, 705, 805, 807)
AND stratum_2 IN (@conceptIdentifiers)
AND stratum_2 <> '0'
GROUP BY stratum_2

