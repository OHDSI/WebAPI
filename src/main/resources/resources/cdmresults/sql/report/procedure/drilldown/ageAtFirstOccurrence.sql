SELECT
  c1.concept_id     AS "conceptId",
  c2.concept_name   AS "category",
  ard1.min_value    AS "minValue",
  ard1.p10_value    AS "p10Value",
  ard1.p25_value    AS "p25Value",
  ard1.median_value AS "medianValue",
  ard1.p75_value    AS "p75Value",
  ard1.p90_value    AS "p90Value",
  ard1.max_value    AS "maxValue"
FROM @results_database_schema.ACHILLES_results_dist ard1
INNER JOIN
@vocab_database_schema.concept c1
ON ard1.stratum_1 = CAST(c1.concept_id AS VARCHAR )
INNER JOIN
@vocab_database_schema.concept c2
ON ard1.stratum_2 = CAST(c2.concept_id AS VARCHAR )
WHERE ard1.analysis_id = 606
AND c1.concept_id = @conceptId