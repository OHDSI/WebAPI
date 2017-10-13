SELECT
  c1.concept_id         AS condition_concept_id,
  c1.concept_name       AS condition_concept_name,
  c2.concept_group_id   AS concept_id,
  c2.concept_group_name AS concept_name,
  sum(ar1.count_value)  AS count_value
FROM @results_database_schema.ACHILLES_results ar1
INNER JOIN
@vocab_database_schema.concept c1
ON CAST(ar1.stratum_1 AS INT) = c1.concept_id
INNER JOIN
(
SELECT concept_id,
CASE WHEN concept_name LIKE 'Inpatient%' THEN 10
WHEN concept_name LIKE 'Outpatient%' THEN 20
ELSE concept_id END
+
CASE WHEN (concept_name LIKE 'Inpatient%' OR concept_name LIKE 'Outpatient%' ) AND (concept_name LIKE '%primary%' OR concept_name LIKE '%1st position%') THEN 1
WHEN (concept_name LIKE 'Inpatient%' OR concept_name LIKE 'Outpatient%' ) AND (concept_name NOT LIKE '%primary%' AND concept_name NOT LIKE '%1st position%') THEN 2
ELSE 0 END AS concept_group_id,
CASE WHEN concept_name LIKE 'Inpatient%' THEN 'Claim- Inpatient: '
WHEN concept_name LIKE 'Outpatient%' THEN 'Claim- Outpatient: '
ELSE concept_name END
+
''
+
CASE WHEN (concept_name LIKE 'Inpatient%' OR concept_name LIKE 'Outpatient%' ) AND (concept_name LIKE '%primary%' OR concept_name LIKE '%1st position%') THEN 'Primary diagnosis'
WHEN (concept_name LIKE 'Inpatient%' OR concept_name LIKE 'Outpatient%' ) AND (concept_name NOT LIKE '%primary%' AND concept_name NOT LIKE '%1st position%') THEN 'Secondary diagnosis'
ELSE '' END AS concept_group_name
FROM @vocab_database_schema.concept
WHERE lower(vocabulary_id) = 'condition type'

) c2
ON CAST(ar1.stratum_2 AS INT) = c2.concept_id
WHERE ar1.analysis_id = 405
AND CAST(ar1.stratum_1 AS INT) = @conceptId
GROUP BY c1.concept_id,
c1.concept_name,
c2.concept_group_id,
c2.concept_group_name